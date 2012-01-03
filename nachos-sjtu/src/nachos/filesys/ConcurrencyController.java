package nachos.filesys;

import nachos.thread.Lock;
import nachos.thread.Condition;

public class ConcurrencyController {
    private Lock lock;
    private Condition cond;
    private readCount, writeCount;

    public ConcurrencyController() {
        lock = new Lock();
        cond = new Condition(lock);
        readCount = 0;
        writeCount = 0;
    }

    public void beginRead() {
        lock.acquire();
        while (writeCount > 0)
            cond.sleep();
        ++readCount;
        lock.release();
    }

    public void endRead() {
        lock.acquire();
        --readCount;
        if (readCount == 0)
            cond.wakeAll();
        lock.release();
    }

    public void beginWrite() {
        lock.acquire();
        while (writeCount > 0 || readCount > 0)
            cond.sleep();
        ++writeCount;
        lock.release();
    }

    public void endWrite() {
        lock.acquire();
        --writeCount;
        cond.wakeAll();
        lock.release();
    }
}
