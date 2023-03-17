package de.westnordost.countryboundaries;

class Point
{
	final int x, y;

	Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point point = (Point) o;
		return x == point.x && y == point.y;
	}

	@Override public int hashCode()
	{
		return 31 * x + y;
	}

	@Override public String toString()
	{
		return "[" + Fixed1E7.toDouble(x) + "," + Fixed1E7.toDouble(y) + "]";
	}
}
