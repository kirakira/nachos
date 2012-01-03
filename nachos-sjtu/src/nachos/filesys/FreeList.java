package nachos.filesys;

import java.util.Arrays;
import nachos.machine.Lib;

public class FreeList {
    private int block;

    private byte[] data;

    private FreeList() {
    }

    private int leaves() {
        return data.length << 2;
    }

    public static FreeList load(int block, int count) {
        FreeList fl = new FreeList();
        fl.block = block;

        data = new int[DiskHelper.getBlockSize() * count];
        DiskHelper.getInstance().readBlock(block, count, data);

        return fl;
    }

    private byte bitSet(byte x, int pos) {
        pos = 7 - pos;
        return (byte) (x | (1 << pos));
    }
    
    private void bitReset(byte x, int pos) {
        pos = 7 - pos;
        return (byte) (x & ~(1 << pos));
    }

    private byte bitGet(byte x, int pos) {
        pos = 7 - pos;
        return (x >> pos) & 1;
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
            p = (p >> 1);
            l = (p << 1);
            r = l + 1;
            if (get(l) != 0 && get(r) != 0)
                set(p);
            else
                reset(p);
        } while (p > 0);
    }

    private void setUse(int i) {
        int p = i + leaves(), l, r;
        set(p);

        update(p);
    }

    private void resetUse(int i) {
        int p = i + leaves(), l, r;
        reset(p);

        update(p);
    }

    private int getFree() {
        int i = 0, l = leaves();
        while (i < l) {
            if (get(i) != 0)
                return -1;

            i = (i << 1);
            if (get(i) != 0) {
                i = i + 1;
                Lib.assertTrue(get(i) == 0);
            }
        }

        return i - l;
    }

    public int occupy() {
        int r = getFree();
        if (r != -1) {
            setUse(r);
            save();
        }
        return r;
    }

    public void free(int i) {
        resetUse(i);
        save();
    }

    public static FreeList create(int block, int head) {
        FreeList fl = new FreeList();
        fl.block = block;

        int nBlocks = DiskHelper.getDiskSize() / DiskHelper.getBlockSize();
        int count = 1;
        while (count * DiskHelper.getBlockSize() * 8 < nBlocks)
            count *= 2;

        fl.data = new int[count * 2 * DiskHelper.getBlockSize()];
        Arrays.fill(fl.data, 0);

        for (int i = 0; i < head; ++i)
            fl.setUse(i);
        for (int i = nBlocks; i < leaves(); ++i)
            fl.setUse(i);

        fl.save();
    }

    public void save() {
        int count = data.length / DiskHelper.getBlockSize();
        DiskHelper.getInstance().writeBlock(block, count, data);
    }
}
