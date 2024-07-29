package utility;

/**
 * The WaitConstant class provides a set of constants representing various wait times
 * in seconds. These constants can be used throughout the application to standardize
 * wait durations for actions such as waiting for elements to load or for certain conditions
 * to be met.
 */
public class WaitConstant extends Constant {

    /**
     * Default constructor for the WaitConstant class.
     * Initializes an instance of WaitConstant, setting up default wait time constants.
     */
    public WaitConstant() {
        super();
    }

    /**
     * The shortest wait time, typically used for quick checks or small delays (1 second).
     */
    public static final int SHORTEST_WAIT_TIME = 1;

    /**
     * A very short wait time, slightly longer than the shortest wait time (3 seconds).
     */
    public static final int SHORTER_WAIT_TIME = 3;

    /**
     * A short wait time for brief delays (5 seconds).
     */
    public static final int VERY_SHORT_WAIT_TIME = 5;

    /**
     * A standard short wait time for moderate delays (10 seconds).
     */
    public static final int SHORT_WAIT_TIME = 10;

    /**
     * A medium wait time, used for slightly longer delays (15 seconds).
     */
    public static final int MEDIUM_WAIT_TIME = 15;

    /**
     * A standard wait time, often used as a default for typical operations (20 seconds).
     */
    public static final int STANDARD_WAIT_TIME = 20;

    /**
     * A balanced wait time, used when a longer delay is acceptable (30 seconds).
     */
    public static final int BALANCED_WAIT_TIME = 30;

    /**
     * An extended wait time for operations that require more time (45 seconds).
     */
    public static final int EXTENDED_WAIT_TIME = 45;

    /**
     * A long wait time, suitable for operations that may take significant time (60 seconds).
     */
    public static final int LONG_WAIT_TIME = 60;

    /**
     * A very long wait time for operations that need more time to complete (90 seconds).
     */
    public static final int VERY_LONG_WAIT_TIME = 90;

    /**
     * An extremely long wait time for operations that require extensive time (120 seconds).
     */
    public static final int EXTREMELY_LONG_WAIT_TIME = 120;

    /**
     * A longer wait time for operations that may take an extended period (180 seconds).
     */
    public static final int LONGER_WAIT_TIME = 180;

    /**
     * The longest wait time for operations that may take a very long time (240 seconds).
     */
    public static final int LONGEST_WAIT_TIME = 240;

    /**
     * The very longest wait time for operations that may take the maximum amount of time (300 seconds).
     */
    public static final int VERY_LONGEST_WAIT_TIME = 300;
}
