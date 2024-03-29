package de.westnordost.countryboundaries;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CountryBoundariesTest
{
	@Test public void delegatesToCorrectCellAtEdges()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A"), null),
				cell(arrayOf("B"), null),
				cell(arrayOf("C"), null),
				cell(arrayOf("D"), null)
		), 2, Collections.emptyMap());

		assertEquals(listOf("C"), boundaries.getIds(-180,-90));
		assertEquals(listOf("C"), boundaries.getIds(-90,-90));
		assertEquals(listOf("C"), boundaries.getIds(-180,-45));
		// wrap around
		assertEquals(listOf("C"), boundaries.getIds(180,-45));
		assertEquals(listOf("C"), boundaries.getIds(180,-90));

		assertEquals(listOf("A"), boundaries.getIds(-180,0));
		assertEquals(listOf("A"), boundaries.getIds(-180,45));
		assertEquals(listOf("A"), boundaries.getIds(-90,0));
		// wrap around
		assertEquals(listOf("A"), boundaries.getIds(180,0));
		assertEquals(listOf("A"), boundaries.getIds(180,45));

		assertEquals(listOf("B"), boundaries.getIds(0,0));
		assertEquals(listOf("B"), boundaries.getIds(0,45));
		assertEquals(listOf("B"), boundaries.getIds(90,0));

		assertEquals(listOf("D"), boundaries.getIds(0,-45));
		assertEquals(listOf("D"), boundaries.getIds(0,-90));
		assertEquals(listOf("D"), boundaries.getIds(90,-90));
	}

	@Test public void noArrayIndexOutOfBoundsAtWorldEdges()
	{
		CountryBoundaries boundaries = new CountryBoundaries(
				cells(cell(arrayOf("A"), null)),
				1, Collections.emptyMap());
		boundaries.getIds(-180,-90);
		boundaries.getIds(+180,+90);
		boundaries.getIds(-180,+90);
		boundaries.getIds(+180,-90);
	}

	@Test public void getContainingIdsSortedBySizeAscending()
	{
		Map<String, Double> sizes = new HashMap<>();
		sizes.put("A", 10.0);
		sizes.put("B", 15.0);
		sizes.put("C", 100.0);
		sizes.put("D", 800.0);

		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("D","B","C","A"), null)
		), 1, sizes);

		assertEquals(listOf("A","B","C","D"),boundaries.getIds(1,1));
	}

	@Test public void getIntersectingIdsInBBoxIsMergedCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A"), null),
				cell(arrayOf("B"), null),
				cell(arrayOf("C"), null),
				cell(arrayOf("D", "E"), null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getIntersectingIds(-10,-10,10,10).containsAll(
				listOf("A","B","C","D","E")
		));
	}

	@Test public void getIntersectingIdsInBBoxWrapsLongitudeCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A"), null),
				cell(arrayOf("B"), null),
				cell(arrayOf("C"), null)
		), 3, Collections.emptyMap());

		assertTrue(boundaries.getIntersectingIds(170,0,-170,1).containsAll(
				listOf("A","C")
		));
	}

	@Test public void getContainingIdsInBBoxWrapsLongitudeCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A","B","C"), null),
				cell(arrayOf("X"), null),
				cell(arrayOf("A","B"), null)
		), 3, Collections.emptyMap());

		assertTrue(boundaries.getContainingIds(170,0,-170,1).containsAll(
				listOf("A","B")
		));
	}

	@Test public void getContainingIdsInBBoxReturnsCorrectResultWhenOneCellIsEmpty()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(null, null),
				cell(arrayOf("A"), null),
				cell(arrayOf("A"), null),
				cell(arrayOf("A"), null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getContainingIds(-10,-10,10,10).isEmpty());
	}

	@Test public void getContainingIdsInBBoxIsMergedCorrectly()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A","B"), null),
				cell(arrayOf("B","A"), null),
				cell(arrayOf("C","B","A"), null),
				cell(arrayOf("D","A"), null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getContainingIds(-10,-10,10,10).containsAll(
				listOf("A")
		));
	}

	@Test public void getContainingIdsInBBoxIsMergedCorrectlyAnNothingIsLeft()
	{
		CountryBoundaries boundaries = new CountryBoundaries(cells(
				cell(arrayOf("A"), null),
				cell(arrayOf("B"), null),
				cell(arrayOf("C"), null),
				cell(arrayOf("D"), null)
		), 2, Collections.emptyMap());

		assertTrue(boundaries.getContainingIds(-10,-10,10,10).isEmpty());
	}

	@Test
	public void latitudeOutOfBoundsThrows() {
		CountryBoundaries b = emptyBoundaries();

		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, -90.0001, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, +90.0001, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, 0, -90.0001));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, 0, +90.0001));

		assertThrows(IllegalArgumentException.class, () -> b.getIds(0, -90.0001));
		assertThrows(IllegalArgumentException.class, () -> b.getIds(0, +90.0001));
	}

	@Test
	public void maxLatitudeIsSmallerThanMinLatitudeThrows() {
		CountryBoundaries b = emptyBoundaries();

		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 1.1, 0, 1.0));
	}

	@Test
	public void nonFiniteNumbersThrows() {
		CountryBoundaries b = emptyBoundaries();

		assertThrows(IllegalArgumentException.class, () -> b.getIds(Double.NaN, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getIds(0, Double.NaN));

		assertThrows(IllegalArgumentException.class, () -> b.getIds(Double.POSITIVE_INFINITY, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getIds(0, Double.POSITIVE_INFINITY));

		assertThrows(IllegalArgumentException.class, () -> b.getIds(0, Double.NEGATIVE_INFINITY));
		assertThrows(IllegalArgumentException.class, () -> b.getIds(Double.NEGATIVE_INFINITY, 0));

		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(Double.NaN, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, Double.NaN, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, Double.NaN, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, 0, Double.NaN));

		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(Double.POSITIVE_INFINITY, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, Double.POSITIVE_INFINITY, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, Double.POSITIVE_INFINITY, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, 0, Double.POSITIVE_INFINITY));

		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(Double.NEGATIVE_INFINITY, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, Double.NEGATIVE_INFINITY, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, Double.NEGATIVE_INFINITY, 0));
		assertThrows(IllegalArgumentException.class, () -> b.getContainingIds(0, 0, 0, Double.NEGATIVE_INFINITY));
	}

	/* Helpers */

	private static void assertThrows(Class<?> clazz, Runnable block) {
		try {
			block.run();
		} catch (Throwable e) {
			if (e.getClass() != clazz) {
				fail("Expected exception of type " + clazz.getName() + " but got a " + e.getClass().getName());
			}
			return;
		}
		fail("Expected exception");
	}

	private static CountryBoundaries emptyBoundaries() {
		return new CountryBoundaries(cells(cell(null, null)), 1, Collections.emptyMap());
	}

	private static CountryBoundariesCell cell(String[] containingIds, CountryAreas[] intersecting)
	{
		return new CountryBoundariesCell(
				containingIds == null ? listOf() : listOf(containingIds),
				intersecting == null ? listOf() : listOf(intersecting)
		);
	}


	private static <T> List<T> listOf(T ...elements) { return Arrays.asList(elements); }
	private static String[] arrayOf(String ...elements) { return elements; }

	private static CountryBoundariesCell[] cells(CountryBoundariesCell ...cells) { return cells; }

}
