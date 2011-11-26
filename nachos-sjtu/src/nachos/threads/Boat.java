package nachos.threads;

import java.util.*;
import nachos.ag.BoatGrader;
import nachos.machine.Lib;

public class Boat {
    static BoatGrader bg;

    public static void selfTest() {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);

        // System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
        // begin(1, 2, b);

        // System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
        // begin(3, 3, b);
    }

    static private Lock lock;
    static private Condition cv;
    
    static int adultsOahu;
    static int childrenOahu;
    static boolean complete;
    static boolean boatAtOahu, boatAtMolokai;
    static boolean pilotOahu;

    public static void begin(int adults, int children, BoatGrader b) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here
        lock = new Lock();
        cv = new Condition(lock);
        adultsOahu = adults;
        childrenOahu = children;
        boatAtOahu = true;
        boatAtMolokai = false;
        pilotOahu = false;
        complete = false;

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.
        List<KThread> threads = new ArrayList<KThread>();

        for (int i = 0; i < adults; ++i) {
            KThread t = new KThread(
                    new Runnable() {
                        public void run() {
                            AdultItinerary();
                        }
                    });
            t.setName("Adult #" + i);
            threads.add(t);
            t.fork();
        }

        for (int i = 0; i < children; ++i) {
            KThread t = new KThread(
                    new Runnable() {
                        public void run() {
                            ChildItinerary();
                        }
                    });
            t.setName("Child #" + i);
            threads.add(t);
            t.fork();
        }

        for (KThread t: threads)
            t.join();
    }

    static void AdultItinerary() {
        /*
         * This is where you should put your solutions. Make calls to the
         * BoatGrader to show that it is synchronized. For example:
         * bg.AdultRowToMolokai(); indicates that an adult has rowed the boat
         * across to Molokai
         */
        lock.acquire();

        // Here, the adult is always at Oahu
        // Because once he got to Molokai
        // He will exit from this procedure
        // Therefore he can always access childrenOahu
        while (!(boatAtOahu && childrenOahu <= 1))
            cv.sleep();

        --adultsOahu;
        if (adultsOahu == 0 && childrenOahu == 0)
            complete = true;

        boatAtOahu = false;
        bg.AdultRowToMolokai();
        boatAtMolokai = true;
        cv.wakeAll();

        Lib.debug('b', "1 adult to Molokai");

        lock.release();
    }

    static void ChildItinerary() {
        boolean oahu = true;
        lock.acquire();

        while (!complete) {
            if (boatAtOahu && oahu) {
                if (childrenOahu >= 2) {
                    if (!pilotOahu) {
                        pilotOahu = true;

                        oahu = false;
                    } else {
                        pilotOahu = false;

                        childrenOahu -= 2;
                        if (adultsOahu == 0 && childrenOahu == 0)
                            complete = true;

                        boatAtOahu = false;
                        bg.ChildRowToMolokai();
                        bg.ChildRideToMolokai();
                        boatAtMolokai = true;
                        oahu = false;
                        cv.wakeAll();

                        Lib.debug('b', "2 children to Molokai");
                    }
                } else if (childrenOahu == 1 && adultsOahu == 0) {
                    childrenOahu -= 1;
                    if (adultsOahu == 0 && childrenOahu == 0)
                        complete = true;

                    boatAtOahu = false;
                    bg.ChildRowToMolokai();
                    boatAtMolokai = true;
                    oahu = false;
                    cv.wakeAll();

                    Lib.debug('b', "1 child to Molokai");
                } else
                    cv.sleep();
            } else if (boatAtMolokai && !oahu) {
                childrenOahu += 1;

                boatAtMolokai = false;
                bg.ChildRowToOahu();
                boatAtOahu = true;
                oahu = true;
                cv.wakeAll();

                Lib.debug('b', "1 child to Oahu");
            } else
                cv.sleep();
        }

        lock.release();
    }

    static void SampleItinerary() {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out
                .println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }
}
