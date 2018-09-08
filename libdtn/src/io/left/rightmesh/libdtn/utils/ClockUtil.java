package io.left.rightmesh.libdtn.utils;

/**
 * Utility class to manipulate time.
 *
 * @author Lucien Loiseau on 05/09/18.
 */
public class ClockUtil {

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Check whether a given timestamp and lifetime is expired according to localtime.
     * If timestamp is 0, it checks according to local time and so simply check that lifetime is
     * positive and superior to 0.
     *
     * @param timestamp start time
     * @param lifetime  limit time
     * @return true if timestamp + lifetime is still in the future
     */
    public static boolean isExpired(long timestamp, long lifetime) {
        if (timestamp == 0) {
            return lifetime > 0;
        }
        return getCurrentTime() < (timestamp + lifetime);
    }
}
