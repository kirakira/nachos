package nachos.filesys;

import nachos.machine.OpenFile;

/**
 * File provide some basic IO operations.
 * Each File is associated with an INode which stores the basic information for the file.
 * 
 * @author starforever
 */
public class File extends OpenFile
{
  INode inode;
  
  private int pos;
  
  public File (INode inode)
  {
    this.inode = inode;
    pos = 0;
  }
  
  public int length ()
  {
    return inode.file_size;
  }
  
  public void close ()
  {
    //TODO implement this
  }
  
  public void seek (int pos)
  {
    this.pos = pos;
  }
  
  public int tell ()
  {
    return pos;
  }
  
  public int read (byte[] buffer, int start, int limit)
  {
    int ret = read(pos, buffer, start, limit);
    pos += ret;
    return ret;
  }
  
  public int write (byte[] buffer, int start, int limit)
  {
    int ret = write(pos, buffer, start, limit);
    pos += ret;
    return ret;
  }
  
  public int read (int pos, byte[] buffer, int start, int limit)
  {
    //TODO implement this
    return 0;
  }
  
  public int write (int pos, byte[] buffer, int start, int limit)
  {
    //TODO implement this
    return 0;
  }
}
