package nachos.filesys;

import nachos.machine.Lib;

public class SymbolicLink extends INode {
    private String target;

    private SymbolicLink() {
        super(3);
    }

    public static SymbolicLink load(int block) {
        SymbolicLink sl = new SymbolicLink();
        sl.block = block;

        byte[] buffer = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(block, 1, buffer);

        Lib.assertTrue(sl.getType() == Lib.bytesToInt(buffer, 0), "Symbolic link inode type doesn't match");

        int len = Lib.bytesToInt(buffer, 4);
        sl.target = new String(buffer, 8, len);

        return sl;
    }

    public static SymbolicLink create(int block, String target) {
        SymbolicLink sl = new SymbolicLink();
        sl.block = block;

        sl.target = target;
        sl.save();

        return sl;
    }

    public String getTarget() {
        return target;
    }

    public void save() {
        byte[] data = new byte[DiskHelper.getBlockSize()];
        Lib.bytesFromInt(data, 0, getType());

        byte[] string = target.getBytes();
        Lib.bytesFromInt(data, 4, string.length);
        System.arraycopy(string, 0, data, 8, string.length);

        DiskHelper.getInstance().writeBlock(block, 1, data);
    }
}
