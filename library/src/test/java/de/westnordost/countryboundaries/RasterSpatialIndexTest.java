package de.westnordost.countryboundaries;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RasterSpatialIndexTest
{
	@Test public void latLonIsCorrectlyMappedToPixelPos()
	{
		QueryResult q0 = r(new String[]{"A"}, null);
		QueryResult q1 = r(new String[]{"B"}, null);
		QueryResult q2 = r(new String[]{"C"}, null);
		QueryResult q3 = r(new String[]{"D"}, null);
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0,1,2,3}, 2, new QueryResult[]{q0,q1,q2,q3});

		assertEquals(q0, b.query(-100, 50));
		assertEquals(q1, b.query(+100, 50));
		assertEquals(q2, b.query(-100, -50));
		assertEquals(q3, b.query(+100, -50));
	}

	@Test public void canHandleWrappedLongitude()
	{
		QueryResult q0 = r(new String[]{"A"}, null);
		QueryResult q1 = r(new String[]{"B"}, null);
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0,1}, 2, new QueryResult[]{q0,q1});
		assertEquals(q0, b.query(181,0));
		assertEquals(q1, b.query(361,0));

		assertEquals(q1, b.query(-181,0));
		assertEquals(q0, b.query(-361,0));

		assertEquals(q0, b.query(181+720,0));
		assertEquals(q1, b.query(361+720,0));

		assertEquals(q1, b.query(-181-720,0));
		assertEquals(q0, b.query(-361-720,0));
	}

	@Test public void rejectInvalidLatitudeForLatLonQuery()
	{
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0}, 1, new QueryResult[]{null});
		try
		{
			b.query(100, -91);
			fail();
		} catch (IllegalArgumentException e){}
		try
		{
			b.query(100, +91);
			fail();
		} catch (IllegalArgumentException e){}
	}

	@Test public void rejectInvalidLatitudeForBoundsQuery()
	{
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0}, 1, new QueryResult[]{r(null,null)});
		try {
			b.query(100, -91, 101, 90);
			fail();
		} catch (IllegalArgumentException e) {}
		try {
			b.query(100, -90, 101, 91);
			fail();
		} catch (IllegalArgumentException e) {}
	}

	@Test public void rejectInvalidBoundsQuery()
	{
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0}, 1, new QueryResult[]{r(null,null)});
		try {
			b.query(100, 0, 101, -1);
			fail();
		} catch (IllegalArgumentException e) {}
	}

	@Test public void queryWrapsAround180Longitude()
	{
		QueryResult q0 = r(new String[]{"A"}, null);
		QueryResult q1 = r(new String[]{"B"}, null);
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0,1}, 2, new QueryResult[]{q0,q1});

		assertEquals(r(null, new String[]{"A","B"}), b.query(170,0, -170, 10));
	}

	@Test public void mergeQueryResults()
	{
		String[] A = {"A"};
		String[] B = {"B"};
		String[] AB = {"A","B"};

		assertEquals(r(null, null), merge(r(null,null), r(null,null)));

		assertEquals(r(null, A),    merge(r(A, null), r(null, null)));
		assertEquals(r(null, A),    merge(r(null, null), r(A, null)));

		assertEquals(r(A, null),    merge(r(A, null), r(A, null)));

		assertEquals(r(A, B),       merge(r(A, null), r(AB, null)));
		assertEquals(r(A, B),       merge(r(AB, null), r(A, null)));

		assertEquals(r(AB, null),   merge(r(AB, null), r(AB, null)));

		assertEquals(r(null, A),    merge(r(A, null), r(null, A)));
		assertEquals(r(null, A),    merge(r(null, A), r(A, null)));

		assertEquals(r(null, AB),   merge(r(null, A), r(null, AB)));
	}

	@Test public void equals()
	{
		String[] A = {"A"};
		String[] B = {"B"};
		String[] C = {"C"};
		String[] D = {"D"};

		RasterSpatialIndex index1 = new RasterSpatialIndex(
				new short[]{0,1}, 1, new QueryResult[]{r(A,B), r(C,D)});
		RasterSpatialIndex index2 = new RasterSpatialIndex(
				new short[]{0,1}, 1, new QueryResult[]{r(A,B), r(C,D)});
		assertEquals(index1, index2);
		assertEquals(index1.hashCode(), index2.hashCode());
	}

	@Test public void gettersReturnsCorrectStuff()
	{
		String[] A = {"A"};
		String[] B = {"B"};
		String[] C = {"C"};
		String[] D = {"D"};

		RasterSpatialIndex index = new RasterSpatialIndex(
				new short[]{0,1}, 1, new QueryResult[]{r(A,B), r(C,D)});

		assertEquals(1,index.getRasterWidth());
		assertArrayEquals(new short[]{0,1}, index.getRaster());
		assertArrayEquals(new QueryResult[]{r(A,B), r(C,D)}, index.getIndices());
	}

	private static QueryResult merge(QueryResult q0, QueryResult q1)
	{
		RasterSpatialIndex b = new RasterSpatialIndex(new short[]{0,1}, 2, new QueryResult[]{q0,q1});
		return b.query(-10,0, 10, 10);
	}

	private static QueryResult r(String[] containing, String[] intersecting)
	{
		return new QueryResult(containing, intersecting);
	}
}