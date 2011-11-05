package nachos.threads;

import java.util.*;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
        waitingList = new PriorityQueue<WaitingThread>(
                    11, new Comparator<WaitingThread>() {
                        public int compare(WaitingThread t1, WaitingThread t2) {
                            if (t1.due < t2.due)
                                return -1;
                            else if (t1.due == t2.due)
                                return 0;
                            else
                                return 1;
                        }
                    });

		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
        while (!waitingList.isEmpty()
                && waitingList.peek().due >= Machine.timer().getTime()) {

            boolean intStatus = Machine.interrupt().disable();
            waitingList.poll().thread.ready();
            Machine.interrupt().restore(intStatus);
        }

		KThread.yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x
	 *            the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		long wakeTime = Machine.timer().getTime() + x;
        waitingList.add(new WaitingThread(KThread.currentThread(), wakeTime));

        boolean intStatus = Machine.interrupt().disable();
        KThread.sleep();
        Machine.interrupt().restore(intStatus);
	}

    private static class WaitingThread {
        KThread thread;
        long due;

        WaitingThread(KThread thread, long due) {
            this.thread = thread;
            this.due = due;
        }
    }

    private PriorityQueue<WaitingThread> waitingList;
}
