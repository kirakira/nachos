package nachos.filesys;

import java.util.LinkedList;

/**
 * INode contains detail information about a file.
 * Most important among these is the list of sector numbers the file occupied, 
 * it's necessary to find all the pieces of the file in the filesystem.
 * 
 * @author starforever
 */
public class INode
{
  /** represent a system file (free list) */
  public static int TYPE_SYSTEM = 0;
  
  /** represent a folder */
  public static int TYPE_FOLDER = 1;
  
  /** represent a normal file */
  public static int TYPE_FILE = 2;
  
  /** represent a normal file that is marked as delete */
  public static int TYPE_FILE_DEL = 3;
  
  /** represent a symbolic link file */
  public static int TYPE_SYMLINK = 4;
  
  /** represent a folder that are not valid */
  public static int TYPE_FOLDER_DEL = 5;
  
  /** the reserve size (in byte) in the first sector */
  private static final int FIRST_SEC_RESERVE = 16;
  
  /** size of the file in bytes */
  int file_size;
  
  /** the type of the file */
  int file_type;
  
  /** the number of programs that have access on the file */
  int use_count;
  
  /** the number of links on the file */
  int link_count;
  
  /** maintain all the sector numbers this file used in order */
  private LinkedList<Integer> sec_addr;
  
  /** the first address */
  private int addr;
  
  /** the extended address */
  private LinkedList<Integer> addr_ext;
  
  public INode (int addr)
  {
    file_size = 0;
    file_type = TYPE_FILE;
    use_count = 0;
    link_count = 0;
    sec_addr = new LinkedList<Integer>();
    this.addr = addr;
    addr_ext = new LinkedList<Integer>();
  }
  
  /** get the sector number of a position in the file  */
  public int getSector (int pos)
  {
    //TODO implement this
    return 0;
  }
  
  /** change the file size and adjust the content in the inode accordingly */
  public void setFileSize (int size)
  {
    //TODO implement this
  }
  
  /** free the disk space occupied by the file (including inode) */
  public void free ()
  {
    //TODO implement this
  }
  
  /** load inode content from the disk */
  public void load ()
  {
    //TODO implement this
  }
  
  /** save inode content to the disk */
  public void save ()
  {
    //TODO implement this
  }
}
