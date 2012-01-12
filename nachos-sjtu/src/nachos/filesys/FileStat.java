package nachos.filesys;

public class FileStat {
    public String name;
    public int size;
    public int type;
    public int sectors;
    public int inode;
    public int links;

    public FileStat(String name, int size, int type, int sector, int inode, int links) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.sectors = sector;
        this.inode = inode;
        this.links = links;
    }
}
