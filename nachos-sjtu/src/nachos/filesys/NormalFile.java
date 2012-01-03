package nachos.filesys;

import nachos.machine.Lib;

public class NormalFile extends INode {
    private boolean primary;
    private int validCount;
    private int size;
    private int linkCount;
    private int next;
    private int[] links;

    private NormalFile() {
        super(2);
        links = new int[1000];
    }

    public static NormalFile load(int block) {
        NormalFile nf = new NormalFile();
        nf.block = block;

        byte[] buffer = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(block, 1, buffer);

        Lib.assertTrue(getType() == Lib.bytesToInt(buffer, 0), "Normal file inode type doesn't match");

        if (Lib.bytesToInt(buffer, 4) == 0)
            nf.primary = false;
        else
            nf.primary = true;

        nf.validCount = Lib.bytesToInt(buffer, 8);
        Lib.assertTrue(validCount >= 0 && validCount <= 1000, "Normal file valid count is out of range");

        nf.size = Lib.bytesToInt(buffer, 12);
        
        nf.linkCount = Lib.bytesToInt(buffer, 16);

        nf.next = Lib.bytesToInt(buffer, 20);

        for (int i = 0; i < validCount; ++i)
            nf.links[i] = Lib.bytesToInt(buffer, 24 + i * 4);

        return nf;
    }

    public void save(int block) {
        byte[] data = new int[DiskHelper.getBlockSize()];
        Lib.bytesFromInt(data, 0, getType());
        Lib.bytesFromInt(data, 4, primary ? 1 : 0);
        Lib.bytesFromInt(data, 8, validCount);
        Lib.bytesFromInt(data, 12, size);
        Lib.bytesFromInt(data, 16, linkCount);
        Lib.bytesFromInt(data, 20, next);
        for (int i = 0; i < validCount; ++i)
            nf.links[i] = Lib.bytesToInt(buffer, 24 + i * 4);

        DiskHelper.getInstance().writeBlock(block, 1, data);
    }
}
