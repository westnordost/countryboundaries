package de.westnordost.countryboundaries;

/**
 * Fixed point number math with 7 decimal places.
 */
class Fixed1E7
{
	private static final int DECIMAL_PLACES = 7;
	private static final int FIXED = (int) Math.pow(10, DECIMAL_PLACES);

	public static int doubleToFixed(double value) { return (int) Math.round(FIXED * value); }

	public static double toDouble(int value) { return (double) value / FIXED; }
}