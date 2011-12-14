package nachos.vm;

public class IntPair {
    public int int1, int2;

    public IntPair(int int1, int int2) {
        this.int1 = int1;
        this.int2 = int2;
    }

    public boolean equals(Object x) {
        if (x == null || !(x instanceof IntPair))
            return false;
        return int1 == ((IntPair) x).int1 && int2 == ((IntPair) x).int2;
    }

    public int hashCode() {
        return new Integer(int1).hashCode() + new Integer(int2).hashCode();
    }
}

