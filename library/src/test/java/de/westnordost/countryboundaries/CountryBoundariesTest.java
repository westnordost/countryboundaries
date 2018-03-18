package de.westnordost.countryboundaries;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CountryBoundariesTest
{
	@Test public void serializationWorks() throws IOException, ClassNotFoundException
	{
		Map<String,Double> sizes = new HashMap<>();
		sizes.put("A",123.0);
		sizes.put("B",64.4);

		Point[] A = polygon(p(0,0),p(0,1),p(1,0));
		Point[] B = polygon(p(0,0),p(0,3),p(3,3),p(3,0));
		Point[] Bh = polygon(p(1,1),p(2,1),p(2,2),p(1,2));

		CountryBoundaries boundaries = new CountryBoundaries(
			cells(
				cell(null, null),
				cell(new String[]{"A","B"}, null),
				cell(new String[]{"B"}, countryAreas(new CountryAreas("A",polygons(A),polygons()))),
				cell(null, countryAreas(
						new CountryAreas("B",polygons(B), polygons(Bh)),
						new CountryAreas("C",polygons(B,A), polygons(Bh))
				))
			), 2, sizes
		);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bos);
		boundaries.write(os);
		os.close();
		bos.close();

		byte[] bytes = bos.toByteArray();

		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream is = new ObjectInputStream(bis);
		CountryBoundaries boundaries2 = CountryBoundaries.read(is);

		assertEquals(boundaries, boundaries2);
	}

	@Test public void getContainingIdsSortedBySizeAscending()
	{
		Map<String, Double> sizes = new HashMap<>();
		sizes.put("A", 10.0);
		sizes.put("B", 15.0);
		sizes.put("C", 100.0);
		sizes.put("D", 800.0);

		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(new String[] {"D","B","C","A"}, null)
		), 1, sizes);

		assertEquals(Arrays.asList("A","B","C","D"),boundaries.getIds(1,1));
	}

	@Test public void getIdsInBBoxIsMergedCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(new String[] {"A"}, null),
				cell(new String[] {"B"}, null),
				cell(new String[] {"C"}, null),
				cell(new String[] {"D"}, null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getIds(-10,-10,10,10).containsAll(
				Arrays.asList("A","B","C","D")
		));
	}

	@Test public void getIdsInBBoxWrapsLongitudeCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(new String[] {"A"}, null),
				cell(new String[] {"B"}, null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getIds(170,0,-170,1).containsAll(
				Arrays.asList("A","B")
		));
	}

	/* Helpers */

	private static Point p(double x, double y)
	{
		return new Point(Fixed1E7.doubleToFixed(x),Fixed1E7.doubleToFixed(y));
	}

	private static CountryBoundariesCell cell(String[] containingIds, CountryAreas[] intersecting)
	{
		return new CountryBoundariesCell(
				containingIds == null ? Collections.emptyList() : Arrays.asList(containingIds),
				intersecting == null ? Collections.emptyList() : Arrays.asList(intersecting)
		);
	}

	private static CountryBoundariesCell[] cells(CountryBoundariesCell ...cells) { return cells; }

	private static Point[] polygon(Point ...points) { return points; }
	private static Point[][] polygons(Point[] ...polygons) { return polygons; }

	private static CountryAreas[] countryAreas(CountryAreas ...areas) { return areas; }
}
