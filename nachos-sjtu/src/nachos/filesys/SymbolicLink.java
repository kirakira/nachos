package nachos.filesys;

import nachos.machine.Lib;

public class SymbolicLink {
    private String target;

    private SymbolicLink() {
        super(3);
    }

    public static SymbolicLink load(int block) {
        SymbolicLink sl = new SymbolicLink();
        sl.block = block;

        byte[] buffer = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(block, 1, buffer);

        Lib.assertTrue(getType() == Lib.bytesToInt(buffer, 0), "Symbolic link inode type doesn't match");

        int len = Lib.bytesToInt(buffer, 4);
        sl.target = Utility.bytesToString(len, buffer, 8);

        return sl;
    }

    public void save() {
        byte[] data = new int[DiskHelper.getBlockSize()];
        Lib.bytesFromInt(data, 0, getType());
        Lib.bytesFromInt(data, 4, target.length());
        Utility.stringToBytes(target, target.length(), data, 8);

        DiskHelper.getInstance().writeBlock(block, 1, data);
    }
}
