package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.threads.Lock;

import java.util.*;
import java.io.EOFException;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
    /**
     * Allocate a new process.
     */

    public VMProcess() {
        super();
        lazySections = new HashMap<Integer, IntPair>();
        pages = new LinkedList<Integer>();
    }

    protected TranslationEntry lookUpPageTable(int vpn) {
        return PageTable.getInstance().getEntry(pid ,vpn);
    }

    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        pageLock.acquire();
        swap(VMKernel.vpn(vaddr));
        pageLock.release();

        int ret = super.readVirtualMemory(vaddr, data, offset, length);

        if (ret > 0) {
            pageLock.acquire();

            TranslationEntry entry = translate(vaddr);
            Lib.assertTrue(entry != null);
            Lib.assertTrue(entry.valid);

            entry.used = true;
            PageTable.getInstance().setEntry(pid, entry);

            pageLock.release();
        }

        return ret;
    }

    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        pageLock.acquire();
        swap(VMKernel.vpn(vaddr));
        pageLock.release();

        int ret = super.writeVirtualMemory(vaddr, data, offset, length);

        if (ret > 0) {
            pageLock.acquire();

            TranslationEntry entry = translate(vaddr);
            Lib.assertTrue(entry != null);
            Lib.assertTrue(entry.valid);

            entry.dirty = true;
            PageTable.getInstance().setEntry(pid, entry);

            pageLock.release();
        }

        return ret;
    }

    protected int getFreePage() {
        int ppn = VMKernel.newPage();

        if (ppn == -1) {
            TranslationEntry victim = PageTable.getInstance().pickVictim();

            ppn = victim.ppn;
            swapOut(victim.vpn);
        }

        return ppn;
    }

    protected boolean allocate(int vpn, int desiredPages, boolean readOnly) {
        if (lazySections.containsKey(new Integer(vpn)))
            return false;
        if (lookUpPageTable(vpn) != null)
            return false;

        for (int i = 0; i < desiredPages; ++i) {
            PageTable.getInstance().addEntry(pid, new TranslationEntry(vpn + i, 0, false, readOnly, false, false));
            SwapfileManager.getInstance().addEntry(pid, vpn + i);
            pages.add(new Integer(vpn + i));
        }

        numPages += desiredPages;

        return true;
    }

    protected int pickTLBVictim() {
        for (int i = 0; i < Machine.processor().getTLBSize(); ++i)
            if (Machine.processor().readTLBEntry(i).valid == false)
                return i;

        Random rand = new Random();
        return rand.nextInt(Machine.processor().getTLBSize());
    }

    protected void replaceTLBEntry(int index, TranslationEntry supplant) {
        TranslationEntry entry = Machine.processor().readTLBEntry(index);
        if (entry.valid)
            PageTable.getInstance().combineEntry(pid, Machine.processor().readTLBEntry(index));

        Machine.processor().writeTLBEntry(index, supplant);
    }

    protected void loadLazySection(int vpn) {
        IntPair ip = lazySections.remove(new Integer(vpn));
        if (ip == null)
            return;

        CoffSection section = coff.getSection(ip.int1);
        
        Lib.assertTrue(allocate(vpn, 1, section.isReadOnly()), "allocate section failed");
        int ppn = swap(vpn);

        coff.getSection(ip.int1).loadPage(ip.int2, ppn);
    }

    protected boolean handleTLBMiss(int vaddr) {
        TranslationEntry entry = translate(vaddr);
        if (entry == null) {
            if (lazySections.containsKey(new Integer(UserKernel.vpn(vaddr)))) {
                loadLazySection(UserKernel.vpn(vaddr));
                return true;
            } else
                return false;
        } else {
            if (!entry.valid) {
                handlePageFault(vaddr);
                entry = translate(vaddr);
            }

            int victim = pickTLBVictim();
            replaceTLBEntry(victim, entry);

            return true;
        }
    }

    protected int swap(int vpn) {
        TranslationEntry entry = lookUpPageTable(vpn);
        Lib.assertTrue(entry != null, "page not in PageTable");

        if (entry.valid)
            return entry.ppn;

        int ppn = getFreePage();
        swapIn(ppn, vpn);

        return ppn;
    }

    protected void swapOut(int vpn) {
        TranslationEntry entry = lookUpPageTable(vpn);

        Lib.assertTrue(entry.valid, "Victim entry is not valid");

        Processor processor = Machine.processor();
        for (int i = 0; i < processor.getTLBSize(); ++i) {
            TranslationEntry tlbEntry = processor.readTLBEntry(i);
            if (tlbEntry.valid && tlbEntry.vpn == entry.vpn && tlbEntry.ppn == entry.ppn) {
                PageTable.getInstance().combineEntry(pid, tlbEntry);
                entry = lookUpPageTable(vpn);

                tlbEntry.valid = false;
                processor.writeTLBEntry(i, tlbEntry);

                break;
            }
        }

        if (entry.dirty) {
            byte[] memory = Machine.processor().getMemory();
            SwapfileManager.getInstance().writeToSwapfile(pid, entry.vpn, memory, entry.ppn * pageSize);
        }

        TranslationEntry supplant = new TranslationEntry(entry);
        supplant.valid = false;
        PageTable.getInstance().setEntry(pid, supplant);
    }

    protected void swapIn(int ppn, int vpn) {
        TranslationEntry entry = lookUpPageTable(vpn);
        Lib.assertTrue(entry != null, "Target doesn't exist in page table");
        Lib.assertTrue(entry.valid, "Target entry is not valid");

        byte[] page = SwapfileManager.getInstance().readFromSwapfile(pid, vpn);
        byte[] memory = Machine.processor().getMemory();
        System.arraycopy(page, 0, memory, ppn * pageSize, pageSize);

        TranslationEntry supplant = new TranslationEntry(entry);
        supplant.valid = true;
        supplant.ppn = ppn;
        supplant.used = false;
        supplant.dirty = false;
        PageTable.getInstance().setEntry(pid, supplant);
    }

    protected void handlePageFault(int vaddr) {
        swap(UserKernel.vpn(vaddr));
    }

    protected void releaseResource() {
        for (Integer vpn: pages) {
            pageLock.acquire();

            TranslationEntry entry = PageTable.getInstance().removeEntry(pid, vpn.intValue());
            if (entry.valid)
                VMKernel.deletePage(entry.ppn);

            SwapfileManager.getInstance().removeEntry(pid, vpn.intValue());

            pageLock.release();
        }
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
        Processor processor = Machine.processor();

        pageLock.acquire();
        for (int i = 0; i < processor.getTLBSize(); ++i) {
            TranslationEntry entry = processor.readTLBEntry(i);
            if (entry.valid)
                PageTable.getInstance().combineEntry(pid, entry);
        }
        pageLock.release();

        super.saveState();
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        Processor processor = Machine.processor();
        for (int i = 0; i < processor.getTLBSize(); ++i)
            processor.writeTLBEntry(i, new TranslationEntry(0, 0, false, false, false, false));

        super.restoreState();
    }

    protected boolean load(String name, String[] args) {
        Lib.debug(dbgProcess, "VMProcess.load(\"" + name + "\")");

        OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
        if (executable == null) {
            Lib.debug(dbgProcess, "\topen failed");
            return false;
        }

        try {
            coff = new Coff(executable);
        } catch (EOFException e) {
            executable.close();
            Lib.debug(dbgProcess, "\tcoff load failed");
            return false;
        }

        // make sure the sections are contiguous and start at page 0
        int numPages = 0;
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            if (section.getFirstVPN() != numPages) {
                coff.close();
                Lib.debug(dbgProcess, "\tfragmented executable");
                return false;
            }
            numPages += section.getLength();
        }

        // make sure the argv array will fit in one page
        byte[][] argv = new byte[args.length][];
        int argsSize = 0;
        for (int i = 0; i < args.length; i++) {
            argv[i] = args[i].getBytes();
            // 4 bytes for argv[] pointer; then string plus one for null byte
            argsSize += 4 + argv[i].length + 1;
        }
        if (argsSize > pageSize) {
            coff.close();
            Lib.debug(dbgProcess, "\targuments too long");
            return false;
        }

        // program counter initially points at the program entry point
        initialPC = coff.getEntryPoint();

        // next comes the stack; stack pointer initially points to top of it
        pageLock.acquire();
        if (!allocate(numPages, stackPages, false)) {
            pageLock.release();
            releaseResource();
            return false;
        }
        pageLock.release();

        initialSP = numPages * pageSize;

        // and finally reserve 1 page for arguments
        pageLock.acquire();
        if (!allocate(numPages, 1, false)) {
            pageLock.release();
            releaseResource();
            return false;
        }
        pageLock.release();

        if (!loadSections())
            return false;

        // store arguments in last page
        int entryOffset = (numPages - 1) * pageSize;
        int stringOffset = entryOffset + args.length * 4;

        this.argc = args.length;
        this.argv = entryOffset;

        for (int i = 0; i < argv.length; i++) {
            byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
            Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
            entryOffset += 4;
            Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
            stringOffset += argv[i].length;
            Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
            stringOffset += 1;
        }

        return true;
    }

    /**
     * Initializes page tables for this process so that the executable can be
     * demand-paged.
     * 
     * @return <tt>true</tt> if successful.
     */
    protected boolean loadSections() {
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            
            for (int i = 0; i < section.getLength(); i++) {
                int vpn = section.getFirstVPN() + i;

                lazySections.put(new Integer(vpn), new IntPair(new Integer(s), new Integer(i)));
            }
        }
        return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        super.unloadSections();
    }

    /**
     * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
     * . The <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     * 
     * @param cause
     *            the user exception that occurred.
     */
    public void handleException(int cause) {
        Processor processor = Machine.processor();

        switch (cause) {
            case Processor.exceptionTLBMiss:
                pageLock.acquire();
                boolean success = handleTLBMiss(processor.readRegister(Processor.regBadVAddr));
                pageLock.release();
                
                if (!success) {
                    Lib.debug(dbgProcess, "Page fault: try to access address " + Processor.regBadVAddr);
                    finish(cause);
                }
                break;

            default:
                super.handleException(cause);
                break;
        }
    }

    protected Map<Integer, IntPair> lazySections = null;
    protected LinkedList<Integer> pages = null;

    protected static Lock pageLock = new Lock();
    protected static final int pageSize = Processor.pageSize;
    protected static final char dbgProcess = 'a';
    protected static final char dbgVM = 'v';
}
