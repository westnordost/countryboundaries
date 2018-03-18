package de.westnordost.countryboundaries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/** Represents the areas that one country with the given id covers. */
class CountryAreas
{
	final String id;
	private final Point[][] outer;
	private final Point[][] inner;

	CountryAreas(String id, Point[][] outer, Point[][] inner)
	{
		this.id = id;
		this.outer = outer;
		this.inner = inner;
	}

	boolean covers(Point point)
	{
		int insides = 0;
		for (Point[] area : outer)
		{
			if(isPointInPolygon(point, area)) insides++;
		}
		for (Point[] area : inner)
		{
			if(isPointInPolygon(point, area)) insides--;
		}
		return insides > 0;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CountryAreas that = (CountryAreas) o;

		return id.equals(that.id) && Arrays.deepEquals(inner, that.inner) && Arrays.deepEquals(outer, that.outer);
	}

	@Override public int hashCode()
	{
		return 31 *(31 * id.hashCode() + Arrays.deepHashCode(outer)) + Arrays.deepHashCode(inner);
	}

	@Override public String toString()
	{
		return "" + id + ":" + Arrays.deepToString(outer) + " - " + Arrays.deepToString(inner);
	}

	void write(ObjectOutputStream out) throws IOException
	{
		out.writeUTF(id);
		out.writeInt(outer.length);
		for (Point[] ring : outer)
		{
			writeRing(ring, out);
		}
		out.writeInt(inner.length);
		for (Point[] ring : inner)
		{
			writeRing(ring, out);
		}
	}

	private void writeRing(Point[] points, ObjectOutputStream out) throws IOException
	{
		out.writeInt(points.length);
		for (Point point : points)
		{
			point.write(out);
		}
	}

	static CountryAreas read(ObjectInputStream in) throws IOException
	{
		String id = in.readUTF().intern();
		Point[][] outer = new Point[in.readInt()][];
		for (int i = 0; i < outer.length; i++)
		{
			outer[i] = readRing(in);
		}
		Point[][] inner = new Point[in.readInt()][];
		for (int i = 0; i < inner.length; i++)
		{
			inner[i] = readRing(in);
		}
		return new CountryAreas(id, outer, inner);
	}

	private static Point[] readRing(ObjectInputStream in) throws IOException
	{
		Point[] ring = new Point[in.readInt()];
		for (int j = 0; j < ring.length; j++)
		{
			ring[j] = Point.read(in);
		}
		return ring;
	}

	// modified from:

	// Copyright 2000 softSurfer, 2012 Dan Sunday
	// This code may be freely used and modified for any purpose
	// providing that this copyright notice is included with it.
	// SoftSurfer makes no warranty for this code, and cannot be held
	// liable for any real or imagined damage resulting from its use.
	// Users of this code must verify correctness for their application.
	// http://geomalgorithms.com/a03-_inclusion.html

	private static boolean isPointInPolygon(Point p, Point[] v )
	{
		boolean c = false;
		for (int i = 0, j = v.length-1; i < v.length; j = i++) {
			if ( (v[i].y > p.y) != (v[j].y > p.y) ) {
				double vt = (double)(p.y  - v[i].y) / (v[j].y - v[i].y);
				if (p.x < v[i].x + vt * (v[j].x - v[i].x))
					c = !c;
			}
		}
		return c;
	}
}
