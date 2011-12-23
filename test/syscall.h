/**
 * The Nachos system call interface. These are Nachos kernel operations that
 * can be invoked from user programs using the syscall instruction.
 * 
 * This interface is derived from the UNIX syscalls. This information is
 * largely copied from the UNIX man pages.
 */

#ifndef SYSCALL_H
#define SYSCALL_H

/**
 * System call codes, passed in $r0 to tell the kernel which system call to do.
 */
#define	syscallHalt		0
#define	syscallExit		1
#define	syscallExec		2
#define	syscallJoin		3
#define	syscallCreate		4
#define	syscallOpen		5
#define	syscallRead		6
#define	syscallWrite		7
#define	syscallClose		8
#define	syscallUnlink		9
#define syscallMmap		10
#define syscallConnect		11
#define syscallAccept		12
#define syscallGetpid		13
#define syscallMkdir		14
#define syscallRmdir		15
#define syscallChdir		16
#define syscallGetcwd		17
#define syscallReaddir		18
#define syscallStat	      	19
#define syscallLink               20
#define syscallSymlink		21

/* Don't want the assembler to see C code, but start.s includes syscall.h. */
#ifndef START_S

/* When a process is created, two streams are already open. File descriptor 0
 * refers to keyboard input (UNIX stdin), and file descriptor 1 refers to
 * display output (UNIX stdout). File descriptor 0 can be read, and file
 * descriptor 1 can be written, without previous calls to open().
 */
#define fdStandardInput		0
#define fdStandardOutput	1

#define FileNameMaxLen 256

#define NormalFileType 0
#define DirFileType 1
#define LinkFileType 2

typedef struct FileStatType {
    char name[FileNameMaxLen];
    int size;
    int sectors;
    int type;
    int inode;
    int links;
} FileStat;

/* The system call interface. These are the operations the Nachos kernel needs
 * to support, to be able to run user programs.
 *
 * Each of these is invoked by a user program by simply calling the procedure;
 * an assembly language stub stores the syscall code (see above) into $r0 and
 * executes a syscall instruction. The kernel exception handler is then
 * invoked.
 */

/* Halt the Nachos machine by calling Machine.halt(). Only the root process
 * (the first process, executed by UserKernel.run()) should be allowed to
 * execute this syscall. Any other process should ignore the syscall and return
 * immediately.
 */
void halt();

/* PROCESS MANAGEMENT SYSCALLS: exit(), exec(), join() */

/**
 * Terminate the current process immediately. Any open file descriptors
 * belonging to the process are closed. Any children of the process no longer
 * have a parent process.
 *
 * status is returned to the parent process as this process's exit status and
 * can be collected using the join syscall. A process exiting normally should
 * (but is not required to) set status to 0.
 *
 * exit() never returns.
 */
void exit(int status);

/**
 * Execute the program stored in the specified file, with the specified
 * arguments, in a new child process. The child process has a new unique
 * process ID, and starts with stdin opened as file descriptor 0, and stdout
 * opened as file descriptor 1.
 *
 * file is a null-terminated string that specifies the name of the file
 * containing the executable. Note that this string must include the ".coff"
 * extension.
 *
 * argc specifies the number of arguments to pass to the child process. This
 * number must be non-negative.
 *
 * argv is an array of pointers to null-terminated strings that represent the
 * arguments to pass to the child process. argv[0] points to the first
 * argument, and argv[argc-1] points to the last argument.
 *
 * exec() returns the child process's process ID, which can be passed to
 * join(). On error, returns -1.
 */
int exec(char *file, int argc, char *argv[]);

/**
 * Suspend execution of the current process until the child process specified
 * by the processID argument has exited. If the child has already exited by the
 * time of the call, returns immediately. When the current process resumes, it
 * disowns the child process, so that join() cannot be used on that process
 * again.
 *
 * processID is the process ID of the child process, returned by exec().
 *
 * status points to an integer where the exit status of the child process will
 * be stored. This is the value the child passed to exit(). If the child exited
 * because of an unhandled exception, the value stored is not defined.
 *
 * If the child exited normally, returns 1. If the child exited as a result of
 * an unhandled exception, returns 0. If processID does not refer to a child
 * process of the current process, returns -1.
 */
int join(int processID, int *status);

/* FILE MANAGEMENT SYSCALLS: creat, open, read, write, close, unlink
 *
 * A file descriptor is a small, non-negative integer that refers to a file on
 * disk or to a stream (such as console input, console output, and network
 * connections). A file descriptor can be passed to read() and write() to
 * read/write the corresponding file/stream. A file descriptor can also be
 * passed to close() to release the file descriptor and any associated
 * resources.
 */

/**
 * Attempt to open the named disk file, creating it if it does not exist,
 * and return a file descriptor that can be used to access the file.
 *
 * Note that creat() can only be used to create files on disk; creat() will
 * never return a file descriptor referring to a stream.
 *
 * Returns the new file descriptor, or -1 if an error occurred.
 */
int creat(char *name);

/**
 * Attempt to open the named file and return a file descriptor.
 *
 * Note that open() can only be used to open files on disk; open() will never
 * return a file descriptor referring to a stream.
 *
 * Returns the new file descriptor, or -1 if an error occurred.
 */
int open(char *name);

/**
 * Attempt to read up to count bytes into buffer from the file or stream
 * referred to by fileDescriptor.
 *
 * On success, the number of bytes read is returned. If the file descriptor
 * refers to a file on disk, the file position is advanced by this number.
 *
 * It is not necessarily an error if this number is smaller than the number of
 * bytes requested. If the file descriptor refers to a file on disk, this
 * indicates that the end of the file has been reached. If the file descriptor
 * refers to a stream, this indicates that the fewer bytes are actually
 * available right now than were requested, but more bytes may become available
 * in the future. Note that read() never waits for a stream to have more data;
 * it always returns as much as possible immediately.
 *
 * On error, -1 is returned, and the new file position is undefined. This can
 * happen if fileDescriptor is invalid, if part of the buffer is read-only or
 * invalid, or if a network stream has been terminated by the remote host and
 * no more data is available.
 */
int read(int fileDescriptor, void *buffer, int count);

/**
 * Attempt to write up to count bytes from buffer to the file or stream
 * referred to by fileDescriptor. write() can return before the bytes are
 * actually flushed to the file or stream. A write to a stream can block,
 * however, if kernel queues are temporarily full.
 *
 * On success, the number of bytes written is returned (zero indicates nothing
 * was written), and the file position is advanced by this number. It IS an
 * error if this number is smaller than the number of bytes requested. For
 * disk files, this indicates that the disk is full. For streams, this
 * indicates the stream was terminated by the remote host before all the data
 * was transferred.
 *
 * On error, -1 is returned, and the new file position is undefined. This can
 * happen if fileDescriptor is invalid, if part of the buffer is invalid, or
 * if a network stream has already been terminated by the remote host.
 */
int write(int fileDescriptor, void *buffer, int count);

/**
 * Close a file descriptor, so that it no longer refers to any file or stream
 * and may be reused.
 *
 * If the file descriptor refers to a file, all data written to it by write()
 * will be flushed to disk before close() returns.
 * If the file descriptor refers to a stream, all data written to it by write()
 * will eventually be flushed (unless the stream is terminated remotely), but
 * not necessarily before close() returns.
 *
 * The resources associated with the file descriptor are released. If the
 * descriptor is the last reference to a disk file which has been removed using
 * unlink, the file is deleted (this detail is handled by the file system
 * implementation).
 *
 * Returns 0 on success, or -1 if an error occurred.
 */
int close(int fileDescriptor);

/**
 * Delete a file from the file system. If no processes have the file open, the
 * file is deleted immediately and the space it was using is made available for
 * reuse.
 *
 * If any processes still have the file open, the file will remain in existence
 * until the last file descriptor referring to it is closed. However, creat()
 * and open() will not be able to return new file descriptors for the file
 * until it is deleted.
 *
 * Returns 0 on success, or -1 if an error occurred.
 */
int unlink(char *name);

/**
 * Map the file referenced by fileDescriptor into memory at address. The file
 * may be as large as 0x7FFFFFFF bytes.
 * 
 * To maintain consistency, further calls to read() and write() on this file
 * descriptor will fail (returning -1) until the file descriptor is closed.
 *
 * When the file descriptor is closed, all remaining dirty pages of the map
 * will be flushed to disk and the map will be removed.
 *
 * Returns the length of the file on success, or -1 if an error occurred.
 */
int mmap(int fileDescriptor, char *address);

/**
 * Attempt to initiate a new connection to the specified port on the specified
 * remote host, and return a new file descriptor referring to the connection.
 * connect() does not give up if the remote host does not respond immediately.
 *
 * Returns the new file descriptor, or -1 if an error occurred.
 */
int connect(int host, int port);

/**
 * Attempt to accept a single connection on the specified local port and return
 * a file descriptor referring to the connection.
 *
 * If any connection requests are pending on the port, one request is dequeued
 * and an acknowledgement is sent to the remote host (so that its connect()
 * call can return). Since the remote host will never cancel a connection
 * request, there is no need for accept() to wait for the remote host to
 * confirm the connection (i.e. a 2-way handshake is sufficient; TCP's 3-way
 * handshake is unnecessary).
 *
 * If no connection requests are pending, returns -1 immediately.
 *
 * In either case, accept() returns without waiting for a remote host.
 *
 * Returns a new file descriptor referring to the connection, or -1 if an error
 * occurred.
 */
int accept(int port);

/**
 * getpid() returns the process ID of the calling process.
 */
int getpid(void);

/* ADVANCED FILE MANAGEMENT SYSCALLS: mkdir, rmdir, chdir, getcwd */

/**
 * Create a directory named pathname.
 *
 * Return zero on success, or -1 if an error occurred.
 */
int mkdir(const char *pathname);

/**
 * Delete a directory, which must be empty.
 *
 * On success, zero is returned. On error, -1 is returned.
 */
int rmdir(const char *pathname);

/**
 * Change the working directory of the calling process to the directory
 * specified in pathname.
 * 
 * On success, zero is returned. On error, -1 is returned.
 */
int chdir(const char *path);

/**
 * Get current working directory.
 *
 * The getcwd() copies an absolute pathname of the current  working directory
 * to the array pointed by buf, which is of length size.
 *
 * Return -1 on failure (for example, if the current absolute path name would
 * require a buffer longer than size elements), and the number of characters
 * stored in buf on success.
 */
int getcwd(char *buf, int size);

/**
 * Get directory entries name in the directory.
 *
 * The readdir() copies all entries's name  in the directory named dirname to
 * the array pointed by buf, which is char[size][namesize]
 * 
 * Return -1 on failure (for example, if directory named dirname doesn't exist or
 * entry name longer than namesize elements or the number of entries bigger
 * than size), and the number of entries stored in buf on success.
 * 
 */
int readdir(char *dirname, char buf[][], int size, int namesize);

/**
 * Get file statistic
 *
 * The stat() copies all file statistic to the stat
 * 
 * Return -1 on failure, and the number of charactors stored in buf on success.
 */
int stat(char* filename, FileStat *stat);
/**
 *    The link() creates a new link (also known as a hard link) to an existing file.
 * 
 *    If newpath exists it will not be overwritten.
 *
 *    This new name may be used exactly as the old one for any operation; both names
 *    refer to the same file (and so have the same permissions and ownership) and it
 *    is impossible to tell which name was the "original".
 *
 *    On success, zero is returned.  On error, -1 is returned.
 **/
int link(char* oldname, char* newname);

/**
 *    The symlink() creates a new symbolic link to an existing file.
 * 
 *    If newpath exists it will not be overwritten.
 *    
 *     A  symbolic link is a special type of file whose contents are a string that is
 *     the pathname another file, the file to which the link refers.  In other words,
 *     a symbolic link is a pointer to another name, and not to an underlying object.
 *     For this reason, symbolic links may refer to directories and  may  cross  file
 *     system boundaries.
 *
 *    There  is  no  requirement  that  the  pathname referred to by a symbolic link
 *    should exist.  A symbolic link that refers to a pathname that does  not  exist
 *    is said to be a dangling link.
 *
 *    On success, zero is returned.  On error, -1 is returned.
 **/
int symlink(char* oldname, char* newname);

#endif /* START_S */

#endif /* SYSCALL_H */
