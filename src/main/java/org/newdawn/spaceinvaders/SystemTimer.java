package org.newdawn.spaceinvaders;

/**
 * A wrapper class that provides timing methods. This class
 * provides us with a central location where we can add
 * our current timing implementation.
 *
 * @author Kevin Glass
 */
public class SystemTimer {

	/**
	 * Get the high resolution time in milliseconds
	 *
	 * @return The high resolution time in milliseconds
	 */
	public static long getTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Sleep for a fixed number of milliseconds.
	 *
	 * @param duration The amount of time in milliseconds to sleep for
	 */
	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// we don't care about the interruption
		}
	}
}