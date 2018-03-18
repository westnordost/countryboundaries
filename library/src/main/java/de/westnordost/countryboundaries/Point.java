package de.westnordost.countryboundaries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

	void write(ObjectOutputStream out) throws IOException
	{
		out.writeInt(x);
		out.writeInt(y);
	}

	static Point read(ObjectInputStream in) throws IOException
	{
		return new Point(in.readInt(), in.readInt());
	}
}
