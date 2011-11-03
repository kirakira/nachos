package nachos.filesys;

public class FileStat
{
  public static final int FILE_NAME_MAX_LEN = 256;
  public static final int NORMAL_FILE_TYPE = 0;
  public static final int DIR_FILE_TYPE = 1;
  public static final int LinkFileType = 2;
  
  public String name;
  public int size;
  public int sectors;
  public int type;
  public int inode;
  public int links;
}
