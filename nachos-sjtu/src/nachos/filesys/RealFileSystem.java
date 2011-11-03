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
public class RealFileSystem implements FileSystem
{
  /** the free list */
  private FreeList free_list;
  
  /** the root folder */
  private Folder root_folder;
  
  /** the current folder */
  private Folder cur_folder;
  
  /** the string representation of the current folder */
  private LinkedList<String> cur_path = new LinkedList<String>();
  
  /**
   * initialize the file system
   * 
   * @param format
   *          whether to format the file system
   */
  public void init (boolean format)
  {
    if (format)
    {
      //TODO implement this
    }
    else
    {
      INode inode_free_list = new INode(FreeList.STATIC_ADDR);
      inode_free_list.load();
      free_list = new FreeList(inode_free_list);
      free_list.load();
      
      INode inode_root_folder = new INode(Folder.STATIC_ADDR);
      inode_root_folder.load();
      root_folder = new Folder(inode_root_folder);
      root_folder.load();
    }
  }
  
  public void finish ()
  {
    root_folder.save();
    free_list.save();
  }
  
  /** import from stub filesystem */
  private void importStub ()
  {
    FileSystem stubFS = Machine.stubFileSystem();
    FileSystem realFS = FilesysKernel.realFileSystem;
    String[] file_list = Machine.stubFileList();
    for (int i = 0; i < file_list.length; ++i)
    {
      if (!file_list[i].endsWith(".coff"))
        continue;
      OpenFile src = stubFS.open(file_list[i], false);
      if (src == null)
      {
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
  
  /** get the only free list of the file system */
  public FreeList getFreeList ()
  {
    return free_list;
  }
  
  /** get the only root folder of the file system */
  public Folder getRootFolder ()
  {
    return root_folder;
  }
  
  public OpenFile open (String name, boolean create)
  {
    //TODO implement this
    return null;
  }
  
  public boolean remove (String name)
  {
    //TODO implement this
    return false;
  }
  
  public boolean createFolde (String name)
  {
    //TODO implement this
    return false;
  }
  
  public boolean removeFolder (String name)
  {
    //TODO implement this
    return false;
  }
  
  public boolean changeCurFolder (String name)
  {
    //TODO implement this
    return false;
  }
  
  public String[] readDir (String name)
  {
    //TODO implement this
    return null;
  }
  
  public FileStat getStat (String name)
  {
    //TODO implement this
    return null;
  }
  
  public boolean createLink (String src, String dst)
  {
    //TODO implement this
    return false;
  }
  
  public boolean createSymlink (String src, String dst)
  {
    //TODO implement this
    return false;
  }
}
