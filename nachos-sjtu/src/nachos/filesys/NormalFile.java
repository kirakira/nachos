package nachos.filesys;

import nachos.machine.Lib;

public class NormalFile extends INode {
    private boolean primary;
    private int validCount;
    private int size;
    private int linkCount;
    private int next;
    private int[] links;

    private static final int maxLinkCount = 1000;

    private NormalFile() {
        super(2);
        links = new int[maxLinkCount];
    }

    public static NormalFile load(int block) {
        NormalFile nf = new NormalFile();
        nf.block = block;

        byte[] buffer = new byte[DiskHelper.getBlockSize()];
        DiskHelper.getInstance().readBlock(block, 1, buffer);

        Lib.assertTrue(nf.getType() == Lib.bytesToInt(buffer, 0), "Normal file inode type doesn't match");

        if (Lib.bytesToInt(buffer, 4) == 0)
            nf.primary = false;
        else
            nf.primary = true;

        nf.validCount = Lib.bytesToInt(buffer, 8);
        Lib.assertTrue(nf.validCount >= 0 && nf.validCount <= maxLinkCount, "Normal file valid count is out of range");

        nf.size = Lib.bytesToInt(buffer, 12);
        
        nf.linkCount = Lib.bytesToInt(buffer, 16);

        nf.next = Lib.bytesToInt(buffer, 20);

        for (int i = 0; i < nf.validCount; ++i)
            nf.links[i] = Lib.bytesToInt(buffer, 24 + i * 4);

        return nf;
    }

    public static NormalFile create(int block, boolean primary, int linkCount) {
        NormalFile nf = new NormalFile();
        nf.block = block;

        nf.primary = primary;
        nf.validCount = 0;
        nf.size = 0;
        nf.linkCount = linkCount;
        nf.next = -1;

        nf.save();
        return nf;
    }

    public void save() {
        byte[] data = new byte[DiskHelper.getBlockSize()];
        Lib.bytesFromInt(data, 0, getType());
        Lib.bytesFromInt(data, 4, primary ? 1 : 0);
        Lib.bytesFromInt(data, 8, validCount);
        Lib.bytesFromInt(data, 12, size);
        Lib.bytesFromInt(data, 16, linkCount);
        Lib.bytesFromInt(data, 20, next);
        for (int i = 0; i < validCount; ++i)
            Lib.bytesFromInt(data, 24 + i * 4, links[i]);

        DiskHelper.getInstance().writeBlock(block, 1, data);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int newSize) {
        size = newSize;
    }

    public int getValidCount() {
        return validCount;
    }

    public NormalFile loadNext() {
        Lib.assertTrue(next != -1);

        return NormalFile.load(next);
    }

    public int getLink(int i) {
        return links[i];
    }

    public void read(int i, byte[] buffer, int offset) {
        Lib.assertTrue(i < validCount);

        DiskHelper.getInstance().readBlock(links[i], 1, buffer, offset);
    }

    public void write(int i, byte[] buffer, int offset) {
        Lib.assertTrue(i < validCount);

        DiskHelper.getInstance().writeBlock(links[i], 1, buffer, offset);
    }

    public boolean addLink(int block) {
        if (validCount < maxLinkCount) {
            links[validCount++] = block;
            return true;
        } else
            return false;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public void increaseLinkCount() {
        ++linkCount;
    }

    public void decreaseLinkCount() {
        Lib.assertTrue(linkCount > 0);
        --linkCount;
    }

    public int getLinkCount() {
        return linkCount;
    }

    public boolean hasNext() {
        return next != -1;
    }
}
