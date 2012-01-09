package nachos.filesys;

import nachos.machine.Lib;

public class Entry {
    public int type;
    public int block;
    public String name;

    public final static int DIRECTORY = 0;
    public final static int NORMAL_FILE = 1;
    public final static int SYMBOLIC_LINK = 2;

    public Entry(int type, int block) {
        this(type, block, null);
    }

    public Entry(int type, int block, String name) {
        this.type = type;
        this.block = block;
        this.name = name;
    }

    public INode load() {
        if (type == DIRECTORY)
            return Directory.load(block);
        else if (type == NORMAL_FILE)
            return NormalFile.load(block);
        else if (type == SYMBOLIC_LINK)
            return SymbolicLink.load(block);
        else {
            Lib.assertTrue(false, "Invalid entry type");
            return null;
        }
    }
}
