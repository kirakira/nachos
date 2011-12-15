package nachos.vm;

import nachos.machine.*;
import nachos.machine.TranslationEntry;

import java.util.*;

class TranslationEntryWithPid {
    public TranslationEntry entry;
    public int pid;

    public TranslationEntryWithPid(TranslationEntry entry, int pid) {
        this.entry = entry;
        this.pid = pid;
    }
}

public class PageTable {
    private static PageTable instance = null;

    private Map<IntPair, TranslationEntry> pageTable;
    private TranslationEntryWithPid[] coreMap;

    private PageTable() {
        pageTable = new HashMap<IntPair, TranslationEntry>();
        coreMap = new TranslationEntryWithPid[Machine.processor().getNumPhysPages()];
    }

    public static PageTable getInstance() {
        if (instance == null)
            instance = new PageTable();
        return instance;
    }

    public TranslationEntry getEntry(int pid, int vpn) {
        IntPair ip = new IntPair(pid, vpn);
        if (pageTable.containsKey(ip))
            return new TranslationEntry(pageTable.get(ip));
        else
            return null;
    }

    private TranslationEntry combine(TranslationEntry e1, TranslationEntry e2) {
        TranslationEntry ret = new TranslationEntry(e1);
        if (e1.used || e2.used)
            ret.used = true;
        if (e1.dirty || e2.dirty)
            ret.dirty = true;
        return ret;
    }

    private void setEntryRaw(int pid, TranslationEntry entry, boolean combineEntries) {
        IntPair vip = new IntPair(pid, entry.vpn);
        if (!pageTable.containsKey(vip))
            return;

        TranslationEntry old = pageTable.get(vip);
        TranslationEntry supplant = combineEntries ?
            new TranslationEntry(combine(entry, old)) : new TranslationEntry(entry);

        if (old.valid) {
            Lib.assertTrue(coreMap[old.ppn] != null, "coreMap inconsistent");
            coreMap[old.ppn] = null;
            Lib.debug(dbgVM, "\tcleared coreMap[" + old.ppn + "]");
        }
        if (entry.valid) {
            Lib.assertTrue(coreMap[entry.ppn] == null, "coreMap inconsistent");
            coreMap[entry.ppn] = new TranslationEntryWithPid(supplant, pid);
        }

        pageTable.put(vip, supplant);
    }

    public void setEntry(int pid, TranslationEntry entry) {
        setEntryRaw(pid, entry, false);
    }

    public void combineEntry(int pid, TranslationEntry entry) {
        setEntryRaw(pid, entry, true);
    }

    public boolean addEntry(int pid, TranslationEntry entry) {
        IntPair ip = new IntPair(pid, entry.vpn);
        if (pageTable.containsKey(ip))
            return false;

        entry = new TranslationEntry(entry);
        pageTable.put(ip, entry);
        if (entry.valid)
            coreMap[entry.ppn] = new TranslationEntryWithPid(entry, pid);

        return true;
    }

    public TranslationEntry removeEntry(int pid, int vpn) {
        TranslationEntry entry = pageTable.remove(new IntPair(pid, vpn));
        if (entry != null && entry.valid)
            coreMap[entry.ppn] = null;

        return entry;
    }

    public TranslationEntryWithPid pickVictim() {
        TranslationEntryWithPid ret = null;
        do {
            int index = Lib.random(coreMap.length);
            ret = coreMap[index];
        } while (ret == null || ret.entry.valid == false);

        return ret;
    }

    public void output(boolean onlyCoreMap) {
        if (!onlyCoreMap) {
            Lib.debug(dbgVM, "\tpid\tvpn\tvalid\tppn");
            for (Map.Entry<IntPair, TranslationEntry> entry: pageTable.entrySet()) {
                int pid = entry.getKey().int1;
                TranslationEntry e = entry.getValue();
                Lib.debug(dbgVM, "\t" + new Integer(pid).toString() + "\t"
                        + new Integer(e.vpn).toString() + "\t"
                        + new Boolean(e.valid).toString() + "\t" + new Integer(e.ppn).toString());
            }
        }
        Lib.debug(dbgVM, "\tppn\tpid\tvpn");
        for (int i = 0; i < coreMap.length; ++i) {
            Lib.debug(dbgVM, "\t" + new Integer(i).toString() + "\t" +
                    (coreMap[i] == null ? "N/A\tN/A" : new Integer(coreMap[i].pid).toString()
                        + "\t" + new Integer(coreMap[i].entry.vpn).toString()));
        }
    }

    protected static final char dbgVM = 'v';
}
