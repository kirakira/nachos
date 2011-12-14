package nachos.vm;

import nachos.machine.Machine;
import nachos.machine.TranslationEntry;

import java.util.*;

class PageTable {
    private static PageTable instance = null;

    private Map<IntPair, TranslationEntry> pageTable;
    private TranslationEntry[] coreMap;

    private PageTable() {
        pageTable = new HashMap<IntPair, TranslationEntry>();
        coreMap = new TranslationEntry[Machine.processor().getNumPhysPages()];
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
        return new TranslationEntry(e1.vpn, e1.ppn, e1.valid,
                e1.readOnly, e1.used || e2.used, e1.dirty || e2.dirty);
    }

    private void setEntryRaw(int pid, TranslationEntry entry, boolean combineEntries) {
        IntPair vip = new IntPair(pid, entry.vpn),
                pip = new IntPair(pid, entry.ppn);
        if (!pageTable.containsKey(vip))
            return;

        TranslationEntry old = pageTable.get(vip);
        TranslationEntry supplant = combineEntries ?
            combine(entry, old) : new TranslationEntry(entry);

        if (pageTable.get(vip).valid)
            coreMap[pageTable.get(vip).ppn] = null;
        if (entry.valid)
            coreMap[entry.ppn] = supplant;

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

        pageTable.put(ip, new TranslationEntry(entry));
        if (entry.valid)
            coreMap[entry.ppn] = new TranslationEntry(entry);

        return true;
    }

    public TranslationEntry removeEntry(int pid, int vpn) {
        TranslationEntry entry = pageTable.remove(new IntPair(pid, vpn));
        if (entry != null)
            coreMap[entry.ppn] = null;
        return entry;
    }

    public TranslationEntry pickVictim() {
        Random rand = new Random();
        TranslationEntry ret = null;
        do {
            ret = coreMap[rand.nextInt(coreMap.length)];
        } while (ret.valid == false);

        return ret;
    }
}
