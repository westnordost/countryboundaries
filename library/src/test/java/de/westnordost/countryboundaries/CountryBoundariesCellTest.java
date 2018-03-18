package de.westnordost.countryboundaries;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountryBoundariesCellTest
{

	private static CountryAreas[] B = new CountryAreas[]{ new CountryAreas("B",
			new Point[][]{new Point[]{p(0, 0), p(0, 2), p(2, 2), p(2, 0)}},
			new Point[][]{})
	};

	@Test public void getDefiniteIds()
	{
		assertEquals(
			Arrays.asList("A","C"),
			cell(new String[]{"A","C"}, null).getIds(0,0)
		);
	}

	@Test public void getInGeometryIds()
	{
		assertEquals(
			Arrays.asList("B"),
			cell(null, B).getIds(1,1)
		);
	}

	@Test public void dontGetOutOfGeometryIds()
	{
		assertEquals(
			Collections.emptyList(),
			cell(null, B).getIds(4,4)
		);
	}

	@Test public void getDefiniteAndInGeometryIds()
	{
		assertEquals(
			Arrays.asList("A","B"),
			cell(new String[]{"A"},B).getIds(1,1)
		);
	}

	@Test public void getAllIds()
	{
		assertEquals(
			Arrays.asList("A","B"),
			cell(new String[]{"A"},B).getAllIds()
		);
	}

	@Test public void isInAnyDefinitely()
	{
		assertTrue(cell(new String[]{"A"},null).isInAny(0,0,Arrays.asList("B","A")));
	}

	@Test public void isInAnyDefinitelyNot()
	{
		assertFalse(cell(new String[]{"A"},null).isInAny(0,0,Arrays.asList("B")));
	}

	@Test public void isInAnyInGeometry()
	{
		assertTrue(cell(null, B).isInAny(1,1,Arrays.asList("B")));
	}

	@Test public void isInAnyOutOfGeometry()
	{
		assertFalse(cell(null, B).isInAny(4,4,Arrays.asList("B")));
	}

	/* Helpers */

	private static CountryBoundariesCell cell(String[] containingIds, CountryAreas[] intersecting)
	{
		return new CountryBoundariesCell(
				containingIds == null ? Collections.emptyList() : Arrays.asList(containingIds),
				intersecting == null ? Collections.emptyList() : Arrays.asList(intersecting)
		);
	}

	private static Point p(double x, double y)
	{
		return new Point(Fixed1E7.doubleToFixed(x),Fixed1E7.doubleToFixed(y));
	}
}
