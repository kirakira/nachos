package nachos.filesys;

import java.util.Hashtable;

/**
 * Folder is a special type of file used to implement hierarchical filesystem.
 * It maintains a map from filename to the address of the file.
 * There's a special folder called root folder with pre-defined address.
 * It's the origin from where you traverse the entire filesystem.
 * 
 * @author starforever
 */
public class Folder extends File
{
  /** the static address for root folder */
  public static int STATIC_ADDR = 1;
  
  private int size;
  
  /** mapping from filename to folder entry */
  private Hashtable<String, FolderEntry> entry;
  
  public Folder (INode inode)
  {
    super(inode);
    size = 4;
    entry = new Hashtable<String, FolderEntry>();
  }
  
  /** open a file in the folder and return its address */
  public int open (String filename)
  {
    //TODO implement this
    return 0;
  }
  
  /** create a new file in the folder and return its address */
  public int create (String filename)
  {
    //TODO implement this
    return 0;
  }
  
  /** add an entry with specific filename and address to the folder */
  public void addEntry (String filename, int addr)
  {
    //TODO implement this
  }
  
  /** remove an entry from the folder */
  public void removeEntry (String filename)
  {
  }
  
  /** save the content of the folder to the disk */
  public void save ()
  {
    //TODO implement this
  }
  
  /** load the content of the folder from the disk */
  public void load ()
  {
    //TODO implement this
  }
}
