package nachos.filesys;

import java.util.Arrays;
import nachos.machine.Lib;

public class FreeList {
    private int block;

    private byte[] data;

    private boolean dirty;

    private FreeList() {
        dirty = false;
    }

    private int leaves() {
        return data.length << 2;
    }

    public static FreeList load(int block) {
        FreeList fl = new FreeList();
        fl.block = block;

        fl.data = new byte[DiskHelper.getBlockSize() * getBlocksCount()];
        DiskHelper.getInstance().readBlock(block, fl.getBlocksCount(), fl.data);

        return fl;
    }

    private byte bitSet(byte x, int pos) {
        pos = 7 - pos;
        return (byte) (x | (1 << pos));
    }
    
    private byte bitReset(byte x, int pos) {
        pos = 7 - pos;
        return (byte) (x & ~(1 << pos));
    }

    private byte bitGet(byte x, int pos) {
        pos = 7 - pos;
        return (byte) ((x >> pos) & 1);
    }

    private void set(int i) {
        data[i >> 3] = bitSet(data[i >> 3], i & 7);
    }

    private void reset(int i) {
        data[i >> 3] = bitReset(data[i >> 3], i & 7);
    }

    private byte get(int i) {
        return bitGet(data[i >> 3], i & 7);
    }

    private void update(int p) {
        int l, r;

        do {
            p = ((p - 1) >> 1);
            l = (p << 1) + 1;
            r = l + 1;
            if (get(l) != 0 && get(r) != 0)
                set(p);
            else
                reset(p);
        } while (p > 0);
    }

    private void setUse(int i) {
        int p = i + leaves() - 1;
        set(p);
        update(p);
    }

    private void resetUse(int i) {
        int p = i + leaves() - 1;
        reset(p);
        update(p);
    }

    private int getFree() {
        int i = 0, l = leaves();
        while (i < l - 1) {
            if (get(i) != 0)
                return -1;

            i = (i << 1) + 1;
            if (get(i) != 0) {
                i = i + 1;
                Lib.assertTrue(get(i) == 0);
            }
        }

        return i - l + 1;
    }

    private void check() {
        int leaves = leaves();
        for (int i = 0; i < leaves - 1; ++i) {
            int l = i * 2 + 1, r = l + 1;
            if (get(i) != 0) {
                Lib.assertTrue(get(l) != 0 && get(r) != 0, "i=" + i);
            } else {
                Lib.assertTrue(get(l) == 0 || get(r) == 0);
            }
        }
    }

    public int occupy() {
        int r = getFree();
        if (r != -1) {
            dirty = true;
            setUse(r);
        } else
            Lib.debug(dbgFilesys, "Warning: running out of disk space");
        return r;
    }

    public void free(int i) {
        resetUse(i);
        dirty = true;
        Lib.assertTrue(get(i + leaves() - 1) == 0);
    }
    
    public static int getBlocksCount() {
        int nBlocks = (int) (DiskHelper.getDiskSize() / (long) DiskHelper.getBlockSize());
        int count = 1;
        while (count * DiskHelper.getBlockSize() * 8 < nBlocks)
            count *= 2;
        return count * 2;
    }

    public static FreeList create(int block, int head) {
        FreeList fl = new FreeList();
        fl.block = block;

        int count = getBlocksCount();
        fl.data = new byte[count * DiskHelper.getBlockSize()];
        Arrays.fill(fl.data, (byte) 0);
        int nBlocks = (int) (DiskHelper.getDiskSize() / (long) DiskHelper.getBlockSize());

        for (int i = 0; i < head; ++i)
            fl.setUse(i);
        for (int i = block; i < block + count; ++i)
            fl.setUse(i);
        for (int i = nBlocks; i < fl.leaves(); ++i)
            fl.setUse(i);

        fl.save();

        return fl;
    }

    public void save() {
        if (dirty) {
            int count = data.length / DiskHelper.getBlockSize();
            DiskHelper.getInstance().writeBlock(block, count, data);

            dirty = false;
        }
    }

    private static final char dbgFilesys = 'f';
}
