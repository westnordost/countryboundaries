package de.westnordost.countryboundaries;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountryAreasTest
{

	private static final Point[] BIG_SQUARE = new Point[]{p(0, 0), p(0, 10), p(10, 10), p(10, 0)};

	private static final Point[] HOLE = new Point[]{p(2, 2), p(2, 8), p(8, 8), p(8, 2)};

	private static final Point[] SMALL_SQUARE = new Point[]{p(4, 4), p(4, 6), p(6, 6), p(6, 4)};


	@Test public void coversSimplePolygon()
	{
		assertTrue(new CountryAreas("A",
				new Point[][]{BIG_SQUARE},
				new Point[][]{}).covers(p(5,5))
		);
	}

	@Test public void doesNotCoverHole()
	{
		assertFalse(new CountryAreas("A",
				new Point[][]{BIG_SQUARE},
				new Point[][]{HOLE}).covers(p(5,5))
		);
	}

	@Test public void doesCoverPolygonInHole()
	{
		assertTrue(new CountryAreas("A",
				new Point[][]{BIG_SQUARE , SMALL_SQUARE},
				new Point[][]{HOLE}).covers(p(5,5))
		);
	}

	private static Point p(double x, double y)
	{
		return new Point(Fixed1E7.doubleToFixed(x),Fixed1E7.doubleToFixed(y));
	}
}
