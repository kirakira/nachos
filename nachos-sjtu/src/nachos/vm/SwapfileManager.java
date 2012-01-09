package nachos.vm;

import nachos.threads.ThreadedKernel;
import nachos.machine.*;

import java.util.*;

class SwapEntry {
    public int pid;
    public int vpn;
}

public class SwapfileManager {
    private static SwapfileManager instance = null;

    private Map<IntPair, Integer> swapTable;
    private Queue<Integer> holes;

    private OpenFile swapFile;
    private int pageSize;

    private String swapFileName;

    private SwapfileManager(String swapFileName) throws FatalError {
        this.swapFileName = swapFileName;

        pageSize = Machine.processor().pageSize;
        swapTable = new HashMap<IntPair, Integer>();
        holes = new LinkedList<Integer>();

        swapFile = ThreadedKernel.fileSystem.open(swapFileName, true);
        if (swapFile == null)
            throw new FatalError("cannot open swap file: " + swapFileName);
    }

    public static SwapfileManager getInstance(String swapFileName) throws FatalError {
        if (instance == null)
            instance = new SwapfileManager(swapFileName);
        return instance;
    }

    public void close() {
        swapFile.close();
        ThreadedKernel.fileSystem.remove(swapFileName);
    }

    public int findEntry(int pid, int vpn) {
        Integer ret = swapTable.get(new IntPair(pid, vpn));
        if (ret == null)
            return -1;
        else
            return ret.intValue();
    }

    public int addEntry(int pid, int vpn) {
        int pos = findEntry(pid, vpn);
        if (pos != -1)
            return pos;

        if (holes.size() == 0)
            holes.add(new Integer(swapTable.size()));

        Integer i = holes.poll();
        swapTable.put(new IntPair(pid, vpn), i);
        return i.intValue();
    }

    public int writeToSwapfile(int pid, int vpn, byte[] page, int offset) {
        int pos = addEntry(pid, vpn);
        swapFile.write(pos * pageSize, page, offset, pageSize);
        return pos;
    }

    public byte[] readFromSwapfile(int pid, int vpn) {
        int pos = findEntry(pid, vpn);
        if (pos == -1)
            return null;

        byte[] ret = new byte[pageSize];
        if (swapFile.read(pos * pageSize, ret, 0, pageSize) == -1) {
            Lib.debug(dbgVM, "returning a new page from swapfile");
            return new byte[pageSize];
        } else
            return ret;
    }

    public void removeEntry(int pid, int vpn) {
        if (findEntry(pid, vpn) == -1)
            return;
        holes.add(swapTable.remove(new IntPair(pid, vpn)));
    }

    protected final static char dbgVM = 'v';
}
