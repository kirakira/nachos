package nachos.filesys;

import nachos.machine.Lib;

public class SuperBlock {
    private static final int magicNumber = 0x102b272e;

    int rootDir;
    int freeList;

    private SuperBlock() {
    }

    public static void create(int rootDir, int freeList) {
        byte[] data = new byte[DiskHelper.getBlockSize()];
        
        Lib.bytesFromInt(data, 0, magicNumber);
        Lib.bytesFromInt(data, 4, rootDir);
        Lib.bytesFromInt(data, 8, freeList);

        DiskHelper.getInstance().writeBlock(0, 1, data);
    }

    public static SuperBlock load() {
        byte[] data = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(0, 1, data);
        
        if (magicNumber != Lib.bytesToInt(data, 0))
            return null;

        SuperBlock ret = new SuperBlock();
        ret.rootDir = Lib.bytesToInt(data, 4);
        ret.freeList = Lib.bytesToInt(data, 8);

        return ret;
    }
}
