package nachos.filesys;

public abstract class INode {
    protected int block, type;

    public INode(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
