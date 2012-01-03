package nachos.filesys;

import nachos.machine.Lib;

public class Directory extends INode {
    private boolean primary;
    private int validCount;
    private int parent;
    private int next;
    private String[] subDirs;
    private int[] links;

    private Directory() {
        super(1);
        subDirs = new String[7];
        links = new int[7];
    }

    public static Directory load(int block) {
        Directory dir = new Directory();
        dir.block = block;

        byte[] buffer = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(block, 1, buffer);

        Lib.assertTrue(getType() == Lib.bytesToInt(buffer, 0), "Directory inode type doesn't match");

        if (Lib.bytesToInt(buffer, 4) == 0)
            dir.primary = false;
        else
            dir.primary = true;

        dir.validCount = Lib.bytesToInt(buffer, 8);
        dir.Lib.assertTrue(validCount >= 0 && validCount <= 7, "Directory valid count is out of range");

        dir.parent = Lib.bytesToInt(buffer, 12);

        dir.next = Lib.bytesToInt(buffer, 16);

        for (int i = 0; i < validCount; ++i) {
            dir.subDirs[i] = Utility.bytesToString(255, buffer, 20 + i * 516);
            dir.links[i] = Lib.bytesToInt(buffer, 20 + 512 + i * 516);
        }

        return dir;
    }

    public void save() {
        byte[] data = new int[DiskHelper.getBlockSize()];
        Lib.bytesFromInt(data, 0, getType());
        Lib.bytesFromInt(data, 4, primary ? 1 : 0);
        Lib.bytesFromInt(data, 8, validCount);
        Lib.bytesFromInt(data, 12, parent);
        Lib.bytesFromInt(data, 16, next);
        for (int i = 0; i < validCount; ++i) {
            Utility.stringToBytes(subDirs[i], 255, data, 20 + 516 * i);
            Lib.bytesFromInt(data, 20 +  512 + 516 * i, links[i]);
        }

        DiskHelper.getInstance().writeBlock(block, 1, data);
    }

    public static Directory create(int block, int parent) {
        Directory dir = new Directory();
        dir.block = block;

        dir.primary = true;
        dir.validCount = 0;
        dir.parent = parent;
        dir.next = -1;

        dir.save();

        return dir;
    }
}
