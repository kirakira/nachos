package nachos.ag;

import nachos.threads.KThread;
import nachos.threads.Lock;
import nachos.threads.RoundRobinScheduler;
import nachos.threads.ThreadedKernel;

/**
 * LockGrader11 tests basic functions of locks.
 * @author TLP
 *
 */
public class LockGrader11 extends BasicTestGrader {
	static int resource = 0;
	static Lock resourceLock = null;

	void run() {
		resourceLock = new Lock();
		assertTrue(ThreadedKernel.scheduler instanceof RoundRobinScheduler,
				"this test requires roundrobin scheduler");

		/* Test ThreadGrader3.a: Tries a join on thread x before x actually runs */
		ThreadHandler t1 = forkNewThread(new PingTest(1));
		ThreadHandler t2 = forkNewThread(new PingTest(0));
		t2.thread.join();
		t1.thread.join();
		done();
	}

	private class PingTest implements Runnable {
		PingTest(int which) {
			System.out.printf("PingTest(%s)\n", which);
			this.which = which;
		}

		public void run() {
			for (int i = 0; i < 1000; i++) {
				resourceLock.acquire();
				resource = which;
				KThread.yield();
				assertTrue(resource == which, "Lock not working properly");
				resourceLock.release();
				KThread.yield();
			}
		}

		private int which;
	}
}
