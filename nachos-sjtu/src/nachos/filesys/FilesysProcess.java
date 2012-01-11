package nachos.filesys;

import nachos.machine.Machine;
import nachos.machine.FileSystem;
import nachos.machine.Lib;
import nachos.threads.ThreadedKernel;
import nachos.machine.Processor;
import nachos.vm.VMProcess;

/**
 * FilesysProcess is used to handle syscall and exception through some callback methods.
 * 
 * @author starforever
 */
public class FilesysProcess extends VMProcess {
    protected static final int SYSCALL_MKDIR = 14;
    protected static final int SYSCALL_RMDIR = 15;
    protected static final int SYSCALL_CHDIR = 16;
    protected static final int SYSCALL_GETCWD = 17;
    protected static final int SYSCALL_READDIR = 18;
    protected static final int SYSCALL_STAT = 19;
    protected static final int SYSCALL_LINK = 20;
    protected static final int SYSCALL_SYMLINK = 21;

    protected String currentDir = "/";

    private static final char dbgFilesys = 'f';

    private RealFileSystem getFs() {
        FileSystem fs = ThreadedKernel.fileSystem;
        if (fs instanceof RealFileSystem)
            return (RealFileSystem) fs;
        else
            return null;
    }

    private int handleMkdir(int a0) {
        String dir = readVirtualMemoryString(a0, maxArgLen);
        if (dir == null)
            return -1;
        if (getFs().createFolder(absoluteFileName(dir)))
            return 0;
        else
            return -1;
    }

    private int handleRmdir(int a0) {
        String dir = readVirtualMemoryString(a0, maxArgLen);
        if (dir == null)
            return -1;
        if (getFs().removeFolder(absoluteFileName(dir)))
            return 0;
        else
            return -1;
    }

    private int handleChdir(int a0) {
        String dir = readVirtualMemoryString(a0, maxArgLen);
        if (dir == null)
            return -1;
        String t = getFs().getCanonicalPathName(absoluteFileName(dir));
        if (t == null)
            return -1;
        else {
            currentDir = t;
            Lib.debug(dbgFilesys, "Current dir now changed to " + currentDir);
            return 0;
        }
    }

    private int handleGetcwd(int a0, int a1) {
        String s;
        if (currentDir.endsWith("/") && currentDir.length() > 1)
            s = currentDir.substring(0, currentDir.length() - 1);
        else
            s = currentDir;
        byte[] data = s.getBytes();
        byte[] buffer = new byte[data.length + 1];
        System.arraycopy(data, 0, buffer, 0, data.length);
        buffer[data.length] = 0;
        if (buffer.length < a1) {
            return writeVirtualMemory(a0, data);
        } else
            return -1;
    }

    private int handleReaddir(int a0, int a1, int a2, int a3) {
        return -1;
    }

    private int handleStat(int a0, int a1) {
        return -1;
    }

    private int handleLink(int a0, int a1) {
        String src = readVirtualMemoryString(a0, maxArgLen);
        if (src == null)
            return -1;
        String dst = readVirtualMemoryString(a1, maxArgLen);
        if (dst == null)
            return -1;

        if (getFs().createLink(absoluteFileName(src), absoluteFileName(dst)))
            return 0;
        else
            return -1;
    }

    private int handleSymlink(int a0, int a1) {
        String src = readVirtualMemoryString(a0, maxArgLen);
        if (src == null)
            return -1;
        String dst = readVirtualMemoryString(a1, maxArgLen);
        if (dst == null)
            return -1;

        if (getFs().createSymlink(absoluteFileName(src), absoluteFileName(dst)))
            return 0;
        else
            return -1;
    }

    public int handleSyscall (int syscall, int a0, int a1, int a2, int a3) {
        switch (syscall) {
            case SYSCALL_MKDIR:
                return handleMkdir(a0);

            case SYSCALL_RMDIR:
                return handleRmdir(a0);

            case SYSCALL_CHDIR:
                return handleChdir(a0);

            case SYSCALL_GETCWD:
                return handleGetcwd(a0, a1);

            case SYSCALL_READDIR:
                return handleReaddir(a0, a1, a2, a3);

            case SYSCALL_STAT:
                return handleStat(a0, a1);

            case SYSCALL_LINK:
                return handleLink(a0, a1);

            case SYSCALL_SYMLINK:
                return handleSymlink(a0, a1);

            default:
                return super.handleSyscall(syscall, a0, a1, a2, a3);
        }
    }

    public String absoluteFileName(String s) {
        if (s.startsWith("/"))
            return s;
        else
            return currentDir + s;
    }

    public String getSwapFileName() {
        return "/SWAP";
    }
}
