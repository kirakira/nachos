package nachos.threads;

import java.util.*;

import nachos.machine.Lib;
import nachos.machine.Machine;

/**
 * A scheduler that chooses threads using a lottery.
 * 
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 * 
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 * 
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking the
 * maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
	}

	/**
	 * Allocate a new lottery thread queue.
	 * 
	 * @param transferPriority
	 *            <tt>true</tt> if this queue should transfer tickets from
	 *            waiting threads to the owning thread.
	 * @return a new lottery thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new LotteryQueue(transferPriority);
	}

    public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= 1);

        getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
        if (priority == Integer.MAX_VALUE) {
            Machine.interrupt().restore(intStatus);
            return false;
        }

		setPriority(thread, priority + 1);

		Machine.interrupt().restore(intStatus);
		return true;
    }

    public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == 1) {
            Machine.interrupt().restore(intStatus);
			return false;
        }

		setPriority(thread, priority - 1);
        
        Machine.interrupt().restore(intStatus);
        return true;
    }

	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

    protected class LotteryQueue extends PriorityQueue {
        public LotteryQueue(boolean transferPriority) {
            super(transferPriority);
        }

        protected KThread pickNextThread() {
            long sum = 0;
            ArrayList<Long> tickets = new ArrayList<Long>();
            ArrayList<KThread> threads = new ArrayList<KThread>();
            for (KThread t: waitQueue) {
                long pri = LotteryScheduler.this.getThreadState(t).getEffectivePriorityLong();
                threads.add(t);
                tickets.add(new Long(pri));
                sum += pri;
            }
            if (sum == 0)
                return null;

            for (int i = 0; i < tickets.size() - 1; ++i) {
                double p = ((double) tickets.get(i)) / ((double) sum);
                if (Lib.random() <= p)
                    return threads.get(i);
                sum -= tickets.get(i);
            }

            return threads.get(threads.size() - 1);
        }
    }

    protected class ThreadState extends PriorityScheduler.ThreadState {
        public ThreadState(KThread thread) {
            super(thread);
        }

        public int getEffectivePriorityRaw(Set<KThread> visited) {
            return (int) getEffectivePriorityLongRaw(visited);
        }

        public long getEffectivePriorityLong() {
            return getEffectivePriorityLongRaw(new HashSet<KThread>());
        }

        public long getEffectivePriorityLongRaw(Set<KThread> visited) {
            if (visited.contains(thread)) {
                Lib.debug('d', "Dead lock detected");
                return priority;
            }

            visited.add(thread);
            long ret = priority;
            for (PriorityQueue q: wantingList) {
                if (q.transferPriority == false)
                    continue;
                for (KThread kt: q)
                    ret += LotteryScheduler.this.getThreadState(kt).getEffectivePriorityLongRaw(visited);
            }
            return ret;
        }
    }
}
