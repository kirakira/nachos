package nachos.vm;

import java.util.*;

class SwapEntry {
    public int pid;
    public int vpn;
}

class SwapfileManager {
    private static SwapfileManager instance = null;

    private Map<IntPair, Integer> swapTable;
    private Queue<Integer> holes;

    private OpenFile swapFile;
    private int pageSize;
    public static final String swapFileName = "SWAP";

    private SwapfileManager() throws FatalError {
        pageSize = Processor.pageSize;
        swapTable = new HashMap<IntPair, Integer>();
        holes = new LinkedList<Integer>();

        swapFile = ThreadedKernel.fileSystem.open(swapFileName, true);
        if (swapFile == null)
            throw new FatalError("cannot open swap file: " + swapFileName);
    }

    public SwapfileManager getInstance() throws FatalError {
        if (instance == null)
            instance = new SwapfileManager();
        return instance;
    }

    public void close() {
        swapFile.close();
    }

    public int findEntry(int pid, int vpn) {
        Integer ret = swapTable.get(new IntPair(pid, vpn));
        if (ret == null)
            return -1;
        else
            return ret.intValue();
    }

    private int addEntry(int pid, int vpn) {
        int pos = findEntry(pid, vpn);
        if (pos != -1)
            return pos;

        if (holes.size() == 0)
            holes.add(new Integer(pageTable.size()));

        Integer i = holes.poll();
        swapTable.put(new IntPair(pid, vpn), i);
        return i.intValue();
    }

    public int writeToSwapfile(int pid, int vpn, byte[] page) {
        int pos = addEntry(pid, vpn);
        swapFile.write(pos * pageSize, page, 0, pageSize);
        return pos;
    }

    public byte[] readFromSwapfile(int pid, int vpn) {
        int pos = findEntry(pid, vpn);
        if (pos == -1)
            return null;

        byte[] ret = new byte[pageSize];
        swapFile.read(pos * pageSize, ret, 0, pageSize);
        return ret;
    }

    public void remove(int pid, int vpn) {
        if (findEntry(pid, vpn) == -1)
            return;
        holes.add(swapTable.remove(new IntPair(pid, vpn)));
    }
}
