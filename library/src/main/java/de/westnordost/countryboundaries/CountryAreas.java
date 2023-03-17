package de.westnordost.countryboundaries;

import java.util.Arrays;

/** Represents the areas that one country with the given id covers. */
class CountryAreas
{
	final String id;
	final Point[][] outer;
	final Point[][] inner;

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
		int wn = 0;
		for (int j = 0, i = v.length-1; j < v.length; i = j++) {
			if (v[i].y <= p.y) {
				if(v[j].y > p.y) {
					if (isLeft(v[i],v[j],p) > 0)
						++wn;
				}
			} else {
				if(v[j].y <= p.y) {
					if (isLeft(v[i],v[j],p) < 0)
						--wn;
				}
			}
		}
		return wn != 0;
	}

	private static long isLeft(Point p0, Point p1, Point p)
	{
		return ((long) p1.x - p0.x) * ((long) p.y - p0.y) - ((long) p.x - p0.x) * ((long) p1.y - p0.y);
	}
}