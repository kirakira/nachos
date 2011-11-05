package nachos.threads;

import java.util.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
        listener = 0;
        id_acc = 0;
        speakers = new HashMap<Integer, Integer>();

        lock = new Lock();
        cvListener = new Condition(lock);
        cvSpeaker = new Condition(lock);
	}

    private Integer getId() {
        int ret = id_acc;
        ++id_acc;
        return new Integer(ret);
    }

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word
	 *            the integer to transfer.
	 */
	public void speak(int word) {
        lock.acquire();

        Integer id = getId();

        speakers.put(id, new Integer(word));

        if (listener > 0)
        {
            --listener;
            cvListener.wake();
        }

        do {
            cvSpeaker.sleep();
        } while (speakers.containsKey(id));

        lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
        lock.acquire();

        while (speakers.size() == 0) {
            ++listener;
            cvListener.sleep();
        }

        int word = 0;
        Integer id = null;
        for (Map.Entry<Integer, Integer> e: speakers.entrySet()) {
            id = e.getKey();
            word = e.getValue().intValue();
            break;
        }
        speakers.remove(id);

        cvSpeaker.wakeAll();

        lock.release();

        return word;
	}

    private int listener;
    private int id_acc;
    private Map<Integer, Integer> speakers;

    private Lock lock;
    private Condition cvListener, cvSpeaker;
}
