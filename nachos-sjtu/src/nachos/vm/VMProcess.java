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

        savedTLBEntries = new TranslationEntry[Machine.processor().getTLBSize()];
        for (int i = 0; i < Machine.processor().getTLBSize(); ++i)
            savedTLBEntries[i] = new TranslationEntry(0, 0, false, false, false, false);
    }

    protected TranslationEntry lookUpPageTable(int vpn) {
        return PageTable.getInstance().getEntry(pid ,vpn);
    }

    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        acquireLock("readVirtualMemory");

        swap(VMKernel.vpn(vaddr));

        TranslationEntry entry = translate(vaddr);
        Lib.assertTrue(entry != null);
        Lib.assertTrue(entry.valid);

        entry.used = true;
        PageTable.getInstance().setEntry(pid, entry);

        releaseLock();

        return super.readVirtualMemory(vaddr, data, offset, length);
    }

    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
        acquireLock("writeVirtualMemory");

        swap(VMKernel.vpn(vaddr));

        TranslationEntry entry = translate(vaddr);
        Lib.assertTrue(entry != null);
        Lib.assertTrue(entry.valid);

        entry.dirty = true;
        PageTable.getInstance().setEntry(pid, entry);

        releaseLock();

        return super.writeVirtualMemory(vaddr, data, offset, length);
    }

    protected int getFreePage() {
        int ppn = VMKernel.newPage();

        if (ppn == -1) {
            TranslationEntryWithPid victim = PageTable.getInstance().pickVictim();

            ppn = victim.entry.ppn;
            swapOut(victim.pid, victim.entry.vpn);
        }

        return ppn;
    }

    protected boolean allocate(int vpn, int desiredPages, boolean readOnly) {
        Lib.assertTrue(!lazySections.containsKey(new Integer(vpn)), "page " + vpn + " already existed in lazySection");
        Lib.assertTrue(lookUpPageTable(vpn) == null, "page " + vpn + " already existed in PageTable");

        for (int i = 0; i < desiredPages; ++i) {
            PageTable.getInstance().addEntry(pid, new TranslationEntry(vpn + i, 0, false, readOnly, false, false));
            SwapfileManager.getInstance(getSwapFileName()).addEntry(pid, vpn + i);
            pages.add(new Integer(vpn + i));
        }

        numPages += desiredPages;

        return true;
    }

    protected void outputTLB() {
        for (int i = 0; i < Machine.processor().getTLBSize(); ++i) {
            TranslationEntry entry = Machine.processor().readTLBEntry(i);
            Lib.debug(dbgVM, "\t" + new Boolean(entry.valid).toString() + "\t"
                    + new Integer(entry.vpn).toString() + "\t"
                    + new Integer(entry.ppn).toString());
        }
    }

    protected int pickTLBVictim() {
        for (int i = 0; i < Machine.processor().getTLBSize(); ++i)
            if (Machine.processor().readTLBEntry(i).valid == false)
                return i;

        return Lib.random(Machine.processor().getTLBSize());
    }

    protected void replaceTLBEntry(int index, TranslationEntry supplant) {
        TranslationEntry entry = Machine.processor().readTLBEntry(index);
        if (entry.valid)
            PageTable.getInstance().combineEntry(pid, entry);

        Machine.processor().writeTLBEntry(index, supplant);
    }

    protected void loadLazySection(int vpn, int ppn) {
        Lib.debug(dbgVM, "\tload lazy section to virtual page " + vpn + " on phys page " + ppn);
        IntPair ip = lazySections.remove(new Integer(vpn));
        if (ip == null)
            return;

        CoffSection section = coff.getSection(ip.int1);
        coff.getSection(ip.int1).loadPage(ip.int2, ppn);
    }

    protected boolean handleTLBMiss(int vaddr) {
        Lib.debug(dbgVM, "\tstart to handle TLB Miss");
        TranslationEntry entry = translate(vaddr);
        if (entry == null)
            return false;
        else {
            if (!entry.valid) {
                Lib.debug(dbgVM, "\tpage is not in memory");

                handlePageFault(vaddr);
                entry = translate(vaddr);
                Lib.assertTrue(entry.valid, "Page fault not handled");
            } else {
                Lib.debug(dbgVM, "\tpage is in memory");
            }

            int victim = pickTLBVictim();
            replaceTLBEntry(victim, entry);

            return true;
        }
    }

    protected int swap(int vpn) {
        TranslationEntry entry = lookUpPageTable(vpn);
        Lib.assertTrue(entry != null, "page " + vpn + " not in PageTable");

        if (entry.valid)
            return entry.ppn;

        int ppn = getFreePage();
        swapIn(ppn, vpn);

        return ppn;
    }

    protected void swapOut(int pid, int vpn) {
        TranslationEntry entry = PageTable.getInstance().getEntry(pid, vpn);
        Lib.assertTrue(entry != null, "victim not in PageTable");
        Lib.assertTrue(entry.valid, "victim page not in memory");

        Lib.debug(dbgVM, "\tswapping out virtual page " + entry.vpn + " of pid " + pid + " on phys page " + entry.ppn);

        Processor processor = Machine.processor();
        for (int i = 0; i < processor.getTLBSize(); ++i) {
            TranslationEntry tlbEntry = processor.readTLBEntry(i);
            if (tlbEntry.valid && tlbEntry.vpn == entry.vpn && tlbEntry.ppn == entry.ppn) {
                PageTable.getInstance().combineEntry(pid, tlbEntry);
                entry = PageTable.getInstance().getEntry(pid, vpn);

                tlbEntry.valid = false;
                processor.writeTLBEntry(i, tlbEntry);

                break;
            }
        }

        if (entry.dirty) {
            byte[] memory = Machine.processor().getMemory();
            SwapfileManager.getInstance(getSwapFileName()).writeToSwapfile(pid, entry.vpn, memory, entry.ppn * pageSize);
        }

        entry.valid = false;
        PageTable.getInstance().setEntry(pid, entry);
    }

    protected void swapIn(int ppn, int vpn) {
        Lib.debug(dbgVM, "\tswapping in virtual page " + vpn + " to phys page " + ppn);

        TranslationEntry entry = lookUpPageTable(vpn);
        Lib.assertTrue(entry != null, "Target doesn't exist in page table");
        Lib.assertTrue(!entry.valid, "Target entry is valid");

        boolean dirty = false;
        if (lazySections.containsKey(new Integer(vpn))) {
            loadLazySection(vpn, ppn);
            dirty = true;
        } else {
            byte[] page = SwapfileManager.getInstance(getSwapFileName()).readFromSwapfile(pid, vpn);
            byte[] memory = Machine.processor().getMemory();
            System.arraycopy(page, 0, memory, ppn * pageSize, pageSize);

            dirty = false;
        }

        TranslationEntry supplant = new TranslationEntry(entry);
        supplant.valid = true;
        supplant.ppn = ppn;
        supplant.used = dirty;
        supplant.dirty = dirty;
        PageTable.getInstance().setEntry(pid, supplant);
    }

    protected void handlePageFault(int vaddr) {
        swap(UserKernel.vpn(vaddr));
    }

    protected void releaseResource() {
        for (Integer vpn: pages) {
            acquireLock("releaseResource");

            TranslationEntry entry = PageTable.getInstance().removeEntry(pid, vpn.intValue());
            if (entry.valid)
                VMKernel.deletePage(entry.ppn);

            SwapfileManager.getInstance(getSwapFileName()).removeEntry(pid, vpn.intValue());

            releaseLock();
        }
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
        Lib.debug(dbgVM, "save state");

        Processor processor = Machine.processor();

        for (int i = 0; i < processor.getTLBSize(); ++i) {
            savedTLBEntries[i] = processor.readTLBEntry(i);
            if (savedTLBEntries[i].valid)
                PageTable.getInstance().combineEntry(pid, processor.readTLBEntry(i));
        }

        super.saveState();
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        Lib.debug(dbgVM, "restore state, current pid: " + pid);

        Processor processor = Machine.processor();
        for (int i = 0; i < processor.getTLBSize(); ++i) {
            if (savedTLBEntries[i].valid) {
                TranslationEntry entry = lookUpPageTable(savedTLBEntries[i].vpn);
                if (entry != null && entry.valid)
                    processor.writeTLBEntry(i, savedTLBEntries[i]);
                else
                    processor.writeTLBEntry(i, new TranslationEntry(0, 0, false, false, false, false));
            } else
                processor.writeTLBEntry(i, new TranslationEntry(0, 0, false, false, false, false));
        }
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

    protected void acquireLock(String msg) {
        pageLock.acquire();
    }

    protected void releaseLock() {
        pageLock.release();
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
                Lib.debug(dbgVM, "\tTLB Miss exception tirggered, bad addr=" + processor.readRegister(Processor.regBadVAddr) + "("
                        + VMKernel.vpn(processor.readRegister(Processor.regBadVAddr)) + ")");

                acquireLock("handleTLBMiss");
                boolean success = handleTLBMiss(processor.readRegister(Processor.regBadVAddr));
                
                if (!success) {
                    Lib.debug(dbgVM, "Page fault: try to access bad address " + processor.readRegister(Processor.regBadVAddr));
                    releaseLock();
                    finish(cause);
                } else {
                    boolean f = false;
                    for (int i = 0; i < Machine.processor().getTLBSize(); ++i) {
                        TranslationEntry entry = Machine.processor().readTLBEntry(i);
                        if (entry.valid && entry.vpn == VMKernel.vpn(processor.readRegister(Processor.regBadVAddr))) {
                            f = true;
                            break;
                        }
                    }
                    Lib.assertTrue(f, "TLB miss not handled, page " + VMKernel.vpn(processor.readRegister(Processor.regBadVAddr)) + " not in TLB");
                    Lib.debug(dbgVM, "\tTLB Miss exception handled");

                    releaseLock();
                }

                break;

            default:
                super.handleException(cause);
                break;
        }
    }

    protected Map<Integer, IntPair> lazySections = null;
    protected LinkedList<Integer> pages = null;
    protected TranslationEntry[] savedTLBEntries = null;

    protected static Lock pageLock = new Lock();
    protected static final int pageSize = Processor.pageSize;
    protected static final char dbgVM = 'v';
}
