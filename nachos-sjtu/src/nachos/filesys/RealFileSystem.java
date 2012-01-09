package nachos.filesys;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import nachos.machine.FileSystem;
import nachos.machine.Machine;
import nachos.machine.Lib;
import nachos.machine.OpenFile;

/**
 * RealFileSystem provide necessary methods for filesystem syscall.
 * The FileSystem interface already define two basic methods, you should implement your own to adapt to your task.
 * 
 * @author starforever
 */
public class RealFileSystem implements FileSystem {
    private static class CachedNormalFile {
        private boolean dirty;
        private int useCount;
        private NormalFile normalFile;

        public CachedNormalFile(NormalFile nf) {
            normalFile = nf;
            dirty = false;
            useCount = 0;
        }

        public void increaseUseCount() {
            ++useCount;
        }

        public void decreaseUseCount() {
            Lib.assertTrue(useCount >= 1);
            --useCount;
        }

        public boolean isUsing() {
            return useCount > 0;
        }

        public void setDirty() {
            dirty = true;
        }

        public NormalFile normalFile() {
            return normalFile;
        }
        
        public void save() {
            if (dirty) {
                normalFile.save();
                dirty = false;
            }
        }
    }

    public class File extends OpenFile {
        CachedNormalFile cnf;
        boolean open;
        int pointer;

        private File(CachedNormalFile cnf, String fileName) {
            super(RealFileSystem.this, fileName);
            this.cnf = cnf;
            open = true;
            pointer = 0;
        }

        private int getBlockCount() {
            if (length() == 0)
                return 0;
            else
                return ((length() - 1) / DiskHelper.getBlockSize()) + 1;
        }

        private int readBlock(int block, byte[] buffer, int offset) {
            if (block >= getBlockCount())
                return 0;

            NormalFile current = cnf.normalFile();
            while (block >= current.getValidCount()) {
                block -= current.getValidCount();
                current = current.loadNext();
            }

            current.read(block, buffer, offset);

            int r = 0;
            if (block == getBlockCount() - 1)
                r = length () % DiskHelper.getBlockSize();
            if (r == 0)
                r = DiskHelper.getBlockSize();

            return r;
        }

        private int writeBlock(int block, byte[] buffer, int offset) {
            if (block >= getBlockCount())
                return 0;

            NormalFile current = cnf.normalFile();
            while (block >= current.getValidCount()) {
                block -= current.getValidCount();
                current = current.loadNext();
            }

            current.write(block, buffer, offset);

            int r = 0;
            if (block == getBlockCount() - 1)
                r = length() % DiskHelper.getBlockSize();
            if (r == 0)
                r = DiskHelper.getBlockSize();

            return r;
        }

        public int read(int pos, byte[] buf, int offset, int length) {
            Lib.debug(dbgFilesys, "Reading " + getName());

            if (!open)
                return -1;
            if (pos < 0 || length < 0)
                return -1;
            if (length == 0 || length() == 0)
                return 0;

            int firstBlock = pos / DiskHelper.getBlockSize();
            byte[] tmp = new byte[DiskHelper.getBlockSize()];

            int r = 0, i = firstBlock;
            while (r < length) {
                int begin = (i == firstBlock) ? (pos % DiskHelper.getBlockSize()) : 0;
                int read = readBlock(i, tmp, 0);
                int c = Math.min(read - begin, length - r);
                if (c <= 0)
                    break;
                System.arraycopy(tmp, begin, buf, offset + r, c);
                r += c;

                if (read < DiskHelper.getBlockSize())
                    break;
                else
                    ++i;
            }

            Lib.assertTrue(r <= length);

            return r;
        }

        private boolean expand(int newSize) {
            cnf.setDirty();

            NormalFile inode = cnf.normalFile();
            int capacity = getBlockCount() * DiskHelper.getBlockSize();
            if (capacity < newSize) {
                int d = (newSize - capacity - 1) / DiskHelper.getBlockSize() + 1;
                Lib.assertTrue(d > 0);
                
                LinkedList<Integer> places = new LinkedList<Integer>();
                for (int i = 0; i < d; ++i) {
                    int t = freeList.occupy();
                    if (t == -1) {
                        for (Integer p: places)
                            freeList.free(p.intValue());
                        return false;
                    } else
                        places.add(new Integer(t));
                }

                NormalFile current = inode;
                while (!places.isEmpty()) {
                    int block = places.remove().intValue();

                    if (current.addLink(block) == false) {
                        int t = freeList.occupy();
                        if (t == -1) {
                            Lib.debug(dbgFilesys, "Fatal error: not enough disk space to store inode; may cause filesys inconsistent");
                            return false;
                        }
                        NormalFile next = NormalFile.create(t, false, 0);
                        current.setNext(t);
                        if (current != inode)
                            current.save();
                        current = next;

                        Lib.assertTrue(current.addLink(block));
                    }
                }
            }

            inode.setSize(newSize);

            return true;
        }

        public int write(int pos, byte[] buf, int offset, int length) {
            Lib.debug(dbgFilesys, "Writing " + getName());
            if (!open)
                return -1;
            if (pos < 0 || length < 0)
                return -1;
            if (length == 0)
                return 0;

            if (length() < pos + length && !expand(pos + length))
                return -1;

            int firstBlock = pos / DiskHelper.getBlockSize();
            byte[] tmp = new byte[DiskHelper.getBlockSize()];

            int r = 0, i = firstBlock;
            while (r < length) {
                int begin = (i == firstBlock) ? (pos % DiskHelper.getBlockSize()) : 0;
                int c = Math.min(DiskHelper.getBlockSize() - begin, length - r);
                if (c < DiskHelper.getBlockSize())
                    readBlock(i, tmp, 0);
                System.arraycopy(buf, offset + r, tmp, begin, c);
                writeBlock(i, tmp, 0);

                r += c;
                ++i;
            }

            Lib.assertTrue(r <= length);

            return r;
        }

        public int length() {
            return cnf.normalFile().getSize();
        }

        public void close() {
            Lib.debug(dbgFilesys, "Closing " + getName());
            open = false;
            cnf.decreaseUseCount();

            if (!cnf.isUsing()) {
                int block = cnf.normalFile().getBlock();
                if (removingFiles.containsKey(new Integer(block)))
                    doRemoveFile(removingFiles.get(new Integer(block)));
            }
        }

        public void seek(int pos) {
            pointer = pos;
        }

        public int tell() {
            return pointer;
        }

        public int read(byte[] buf, int offset, int length) {
            int r = read(pointer, buf, offset, length);
            if (r != -1)
                pointer += r;
            return r;
        }

        public int write(byte[] buf, int offset, int length) {
            int r = write(pointer, buf, offset, length);
            if (r != -1)
                pointer += r;
            return r;
        }
    }

    private FreeList freeList;

    private Map<Integer, CachedNormalFile> cachedFiles;
    private Map<Integer, String> removingFiles;

    private static final int rootBlock = 1, freeListBlock = 2, filesysHead = 2;

    /**
     * initialize the file system
     * 
     * @param format
     *          whether to format the file system
     */
    public void init (boolean format) {
        cachedFiles = new HashMap<Integer, CachedNormalFile>();
        removingFiles = new HashMap<Integer, String>();

        if (format) {
            createFilesys();
            importStub();
        } else {
            SuperBlock sb = SuperBlock.load();
            if (sb == null) {
                Lib.debug(dbgFilesys, "Broken filesys detected; formatting");
                createFilesys();
            } else {
                freeList = freeList.load(sb.freeList);
            }
        }
    }

    public void createFilesys() {
        SuperBlock.create(rootBlock, freeListBlock);

        Directory.create(rootBlock, -1);
        freeList = FreeList.create(freeListBlock, filesysHead);
    }

    private Entry findSingleEntry(Directory parent, String fileName) {
        Lib.assertTrue(parent != null);

        Entry entry = parent.find(fileName);
        if (entry != null)
            return entry;
        else if (parent.hasNext())
            return findSingleEntry(parent.loadNext(), fileName);
        else
            return null;
    }

    private Entry findEntryRaw(String absoluteFileName, boolean parent, int depth) {
        if (depth < 0)
            return null;

        if (!absoluteFileName.startsWith("/"))
            return null;
        if (absoluteFileName.endsWith("/"))
            return null;

        int token = absoluteFileName.indexOf("/"), last = absoluteFileName.lastIndexOf("/");
        Directory current = Directory.load(rootBlock);
        while (token != last) {
            int nextToken = absoluteFileName.indexOf("/", token + 1);

            String e = absoluteFileName.substring(token + 1, nextToken);
            Entry entry = findSingleEntry(current, e);

            if (entry == null)
                return null;
            else {
                if (entry.type == Entry.DIRECTORY)
                    current = (Directory) entry.load();
                else if (entry.type == Entry.SYMBOLIC_LINK) {
                    SymbolicLink sl = (SymbolicLink) entry.load();
                    return findEntryRaw(sl.getTarget() + absoluteFileName.substring(nextToken + 1), parent, depth - 1);
                } else
                    return null;
            }

            token = nextToken;
        }

        String e = absoluteFileName.substring(last + 1);
        Entry entry = findSingleEntry(current, e);
        if (entry == null) {
            if (parent)
                return new Entry(Entry.DIRECTORY, current.getBlock());
            else
                return null;
        } else if (entry.type == Entry.NORMAL_FILE)
            return entry;
        else if (entry.type == Entry.SYMBOLIC_LINK) {
            SymbolicLink sl = (SymbolicLink) entry.load();
            return findEntryRaw(sl.getTarget(), parent, depth - 1);
        } else
            return null;
    }

    private Entry findEntry(String absoluteFileName, boolean parent) {
        return findEntryRaw(absoluteFileName, parent, 128);
    }

    public void save() {
        for (CachedNormalFile cnf: cachedFiles.values())
            cnf.save();
        for (String file: removingFiles.values())
            Lib.debug(dbgFilesys, file + " is still being used; not removing it until it's closed");
        freeList.save();
    }

    /** import from stub filesystem */
    private void importStub() {
        FileSystem stubFS = Machine.stubFileSystem();
        FileSystem realFS = this;
        String[] file_list = Machine.stubFileList();
        for (int i = 0; i < file_list.length; ++i) {
            if (!file_list[i].endsWith(".coff"))
                continue;
            OpenFile src = stubFS.open(file_list[i], false);
            if (src == null) {
                continue;
            }
            OpenFile dst = realFS.open("/" + file_list[i], true);
            int size = src.length();
            byte[] buffer = new byte[size];
            src.read(0, buffer, 0, size);
            dst.write(0, buffer, 0, size);
            src.close();
            dst.close();
        }
    }

    private CachedNormalFile getFile(int block) {
        if (!cachedFiles.containsKey(new Integer(block))) {
            NormalFile nf = NormalFile.load(block);
            Lib.assertTrue(nf != null);

            CachedNormalFile cnf = new CachedNormalFile(nf);
            cachedFiles.put(new Integer(block), cnf);
        }
        
        return cachedFiles.get(new Integer(block));
    }

    private void closeFile(int block) {
        CachedNormalFile cnf = cachedFiles.get(new Integer(block));
        Lib.assertTrue(cnf != null);
        cnf.decreaseUseCount();
    }

    private NormalFile createFile(Directory folder, String fileName) {
        Lib.debug(dbgFilesys, "Creating file " + fileName);

        int pos = freeList.occupy();
        if (pos == -1)
            return null;

        Directory current = folder;
        while (current.addEntry(fileName, Entry.NORMAL_FILE, pos) == false) {
            if (current.hasNext())
                current = current.loadNext();
            else {
                int dirPos = freeList.occupy();
                if (dirPos == -1)
                    return null;
                Directory next = Directory.create(dirPos, current.getParent());
                current.setNext(dirPos);
                current = next;
            }
        }

        NormalFile nf = NormalFile.create(pos, true, 1);

        return nf;
    }

    private void ls(Directory folder) {
        while (true) {
            for (int i = 0; i < folder.getValidCount(); ++i) {
                Entry e = folder.getEntry(i);
                Lib.debug(dbgFilesys, e.name);
            }

            if (folder.hasNext())
                folder = folder.loadNext();
            else
                break;
        }
        Lib.debug(dbgFilesys, "");
    }

    public OpenFile open(String name, boolean create) {
        Lib.debug(dbgFilesys, "Opening " + name);

        Entry e = findEntry(name, create);
        if (e == null) {
            Lib.debug(dbgFilesys, name + " not found");
            return null;
        } else if (e.type == Entry.NORMAL_FILE) {
            if (removingFiles.containsKey(new Integer(e.block)))
                return null;

            CachedNormalFile cnf = getFile(e.block);
            cnf.increaseUseCount();
            return new File(cnf, name);
        } else {
            Lib.assertTrue(e.type == Entry.DIRECTORY);

            String fileName = name.substring(name.lastIndexOf("/") + 1);

            Directory dir = Directory.load(e.block);
            NormalFile nf = createFile(dir, fileName);
            if (nf == null)
                return null;
            else {
                CachedNormalFile cnf = getFile(nf.block);
                cnf.increaseUseCount();
                return new File(cnf, name);
            }
        }
    }

    public boolean remove(String name) {
        Entry entry = findEntry(name, false);
        if (entry == null || entry.type != Entry.NORMAL_FILE)
            return false;

        CachedNormalFile cnf = getFile(entry.block);
        if (cnf.isUsing())
            removingFiles.put(new Integer(entry.block), name);
        else
            doRemoveFile(name);

        return true;
    }

    private boolean removeEntry(Directory folder, String name) {
        Directory current = folder, last = null, target = null;
        Entry supplant = null;
        int pos = -1;
        while (true) {
            if (pos == -1) {
                pos = current.findPos(name);
                if (pos != -1)
                    target = current;
            }

            if (current.hasNext()) {
                last = current;
                current = current.loadNext();
                Lib.assertTrue(current != null);
            } else {
                if (current.getValidCount() == 0) {
                    Lib.assertTrue(target == null);
                    return false;
                } else {
                    supplant = current.getEntry(current.getValidCount() -1);

                    if (target != null) {
                        Lib.assertTrue(pos != -1);

                        target.replaceEntry(pos, supplant.name, supplant.type, supplant.block);
                        Lib.assertTrue(current.removeLastEntry());

                        if (current.getValidCount() == 0 && last != null) {
                            last.setNext(-1);
                            freeList.free(current.getBlock());
                        }
                    } else
                        return false;
                }

                break;
            }
        }
        return true;
    }

    private void freeFile(NormalFile nf) {
        cachedFiles.remove(new Integer(nf.getBlock()));

        NormalFile current = nf;
        while (current != null) {
            for (int i = 0; i < current.getValidCount(); ++i)
                freeList.free(current.getLink(i));

            int t = current.getBlock();
            if (current.hasNext())
                current = current.loadNext();
            else
                current = null;
            freeList.free(t);
        }
    }

    private void doRemoveFile(String fileName) {
        Lib.debug(dbgFilesys, "Removing " + fileName);
        Lib.assertTrue(fileName.startsWith("/"));
        Lib.assertTrue(!fileName.endsWith("/"));

        String path = fileName.substring(0, fileName.lastIndexOf("/") + 1),
               file = fileName.substring(fileName.lastIndexOf("/") + 1);

        Entry eFolder = findEntry(path, false);
        Lib.assertTrue(eFolder != null && eFolder.type == Entry.DIRECTORY);
        Directory folder = Directory.load(eFolder.block);
        Lib.assertTrue(folder != null);
        
        Entry entry = findSingleEntry(folder, file);
        Lib.assertTrue(entry != null && entry.type == Entry.NORMAL_FILE);
        CachedNormalFile cnf = getFile(entry.block);
        Lib.assertTrue(!cnf.isUsing());
        
        Lib.assertTrue(removeEntry(folder, file));
        cnf.normalFile().decreaseLinkCount();

        if (cnf.normalFile().getLinkCount() == 0)
            freeFile(cnf.normalFile());

        if (removingFiles.containsKey(new Integer(entry.block)))
            removingFiles.remove(new Integer(entry.block));
    }

    public boolean createFolder(String name) {
        //TODO implement this
        return false;
    }

    public boolean removeFolder(String name) {
        //TODO implement this
        return false;
    }

    public boolean changeCurFolder(String name) {
        //TODO implement this
        return false;
    }

    public String[] readDir(String name) {
        //TODO implement this
        return null;
    }

    public FileStat getStat(String name) {
        //TODO implement this
        return null;
    }

    public boolean createLink(String src, String dst) {
        //TODO implement this
        return false;
    }

    public boolean createSymlink(String src, String dst) {
        //TODO implement this
        return false;
    }

    private static final char dbgFilesys = 'f';
}
