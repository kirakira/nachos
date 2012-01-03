package nachos.filesys;

import java.util.LinkedList;
import nachos.machine.FileSystem;
import nachos.machine.Machine;
import nachos.machine.OpenFile;

/**
 * RealFileSystem provide necessary methods for filesystem syscall.
 * The FileSystem interface already define two basic methods, you should implement your own to adapt to your task.
 * 
 * @author starforever
 */
public class RealFileSystem implements FileSystem {
    private FreeList freeList;
    private Directory root;

    /**
     * initialize the file system
     * 
     * @param format
     *          whether to format the file system
     */
    public void init (boolean format) {
        root = Directory.create(0);
        freeList = FreeList.create(0, 1);

        if (format)
            format();
    }

    /** import from stub filesystem */
    private void importStub () {
        FileSystem stubFS = Machine.stubFileSystem();
        FileSystem realFS = FilesysKernel.realFileSystem;
        String[] file_list = Machine.stubFileList();
        for (int i = 0; i < file_list.length; ++i) {
            if (!file_list[i].endsWith(".coff"))
                continue;
            OpenFile src = stubFS.open(file_list[i], false);
            if (src == null) {
                continue;
            }
            OpenFile dst = realFS.open(file_list[i], true);
            int size = src.length();
            byte[] buffer = new byte[size];
            src.read(0, buffer, 0, size);
            dst.write(0, buffer, 0, size);
            src.close();
            dst.close();
        }
    }

    public OpenFile open (String name, boolean create) {
        //TODO implement this
        return null;
    }

    public boolean remove (String name) {
        //TODO implement this
        return false;
    }

    public boolean createFolde (String name) {
        //TODO implement this
        return false;
    }

    public boolean removeFolder (String name) {
        //TODO implement this
        return false;
    }

    public boolean changeCurFolder (String name) {
        //TODO implement this
        return false;
    }

    public String[] readDir (String name) {
        //TODO implement this
        return null;
    }

    public FileStat getStat (String name) {
        //TODO implement this
        return null;
    }

    public boolean createLink (String src, String dst) {
        //TODO implement this
        return false;
    }

    public boolean createSymlink (String src, String dst) {
        //TODO implement this
        return false;
    }
}
