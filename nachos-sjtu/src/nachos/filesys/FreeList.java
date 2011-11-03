package nachos.filesys;

import java.util.LinkedList;
import nachos.machine.Disk;
import nachos.machine.Lib;

/**
 * FreeList is a single special file used to manage free space of the filesystem.
 * It maintains a list of sector numbers to indicate those that are available to use.
 * When there's a need to allocate a new sector in the filesystem, call allocate().
 * And you should call deallocate() to free space at a appropriate time (eg. when a file is deleted) for reuse in the future.
 * 
 * @author starforever
 */
public class FreeList extends File
{
  /** the static address */
  public static int STATIC_ADDR = 0;
  
  /** size occupied in the disk (bitmap) */
  static int size = Lib.divRoundUp(Disk.NumSectors, 8);
  
  /** maintain address of all the free sectors */
  private LinkedList<Integer> free_list;
  
  public FreeList (INode inode)
  {
    super(inode);
    free_list = new LinkedList<Integer>();
  }
  
  public void init ()
  {
    for (int i = 2; i < Disk.NumSectors; ++i)
      free_list.add(i);
  }
  
  /** allocate a new sector in the disk */
  public int allocate ()
  {
    //TODO implement this
    return 0;
  }
  
  /** deallocate a sector to be reused */
  public void deallocate (int sec)
  {
    //TODO implement this
  }
  
  /** save the content of freelist to the disk */
  public void save ()
  {
    //TODO implement this
  }
  
  /** load the content of freelist from the disk */
  public void load ()
  {
    //TODO implement this
  }
}
