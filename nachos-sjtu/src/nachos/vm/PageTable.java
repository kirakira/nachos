package nachos.vm;

import java.util.*;

class PageTable {
    private static PageTable instance = null;

    private Map<IntPair, TranslationEntry> pageTable;
    private ArrayList<TranslationEntry> coreMap;

    private PageTable() {
        pageTable = new HashMap<IntPair, TranslationEntry>();
        coreMap = new ArrayList<IntPair, TranslationEntry>();
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

    public void setEntry(int pid, TranslationEntry entry) {
        IntPair vip = new IntPair(pid, entry.vpn),
                pip = new IntPair(pid, entry.ppn);
        if (!pageTable.containsKey(vip))
            return;

        if (pageTable.get(vip).valid)
            coreMap.set(pageTable.get(vip).ppn, null);
        if (entry.valid)
            coreMap.set(entry.ppn, new TranslationEntry(entry));

        pageTable.put(vip, new TranslationEntry(entry));
    }

    public boolean addEntry(int pid, TranslationEntry entry) {
        IntPair ip = new IntPair(pid, entry.vpn);
        if (pageTable.containsKey(ip))
            return false;

        pageTable.put(ip, new TranslationEntry(entry));
        if (entry.valid)
            coreMap.put(new IntPair(pid, entry.ppn), new TranslationEntry(entry));

        return true;
    }

    public void removeEntries(int pid) {
        for (Map.Entry<IntPair, TranslationEntry> entry:
                new HashSet<Map.Entry<IntPair, TranslationEntry>>(pageTable.entrySet())) {
            if (entry.getKey().pid == pid)
                pageTable.remove(entry.getKey);
        }


    }
}
