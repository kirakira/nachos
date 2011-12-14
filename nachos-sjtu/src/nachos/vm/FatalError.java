package nachos.vm;

public class FatalError extends Error {
    public FatalError() {
        super();
    }

    public FatalError(String msg) {
        super(msg);
    }
}
