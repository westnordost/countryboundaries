package de.westnordost.countryboundaries

import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.NaN
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.test.*

internal class CountryBoundariesTest {
    @Test fun delegates_to_correct_cell_at_edges() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A")),
                cell(listOf("B")),
                cell(listOf("C")),
                cell(listOf("D"))
            ), 2, emptyMap()
        )

        assertEquals(listOf("C"), boundaries.getIds(-180.0, -90.0))
        assertEquals(listOf("C"), boundaries.getIds(-90.0, -90.0))
        assertEquals(listOf("C"), boundaries.getIds(-180.0, -45.0))
        // wrap around
        assertEquals(listOf("C"), boundaries.getIds(180.0, -45.0))
        assertEquals(listOf("C"), boundaries.getIds(180.0, -90.0))

        assertEquals(listOf("A"), boundaries.getIds(-180.0, 0.0))
        assertEquals(listOf("A"), boundaries.getIds(-180.0, 45.0))
        assertEquals(listOf("A"), boundaries.getIds(-90.0, 0.0))
        // wrap around
        assertEquals(listOf("A"), boundaries.getIds(180.0, 0.0))
        assertEquals(listOf("A"), boundaries.getIds(180.0, 45.0))

        assertEquals(listOf("B"), boundaries.getIds(0.0, 0.0))
        assertEquals(listOf("B"), boundaries.getIds(0.0, 45.0))
        assertEquals(listOf("B"), boundaries.getIds(90.0, 0.0))

        assertEquals(listOf("D"), boundaries.getIds(0.0, -45.0))
        assertEquals(listOf("D"), boundaries.getIds(0.0, -90.0))
        assertEquals(listOf("D"), boundaries.getIds(90.0, -90.0))
    }

    @Test fun no_array_index_out_of_bounds_at_world_edges() {
        val boundaries = CountryBoundaries(listOf(cell(listOf("A"))), 1, emptyMap())
        boundaries.getIds(-180.0, -90.0)
        boundaries.getIds(+180.0, +90.0)
        boundaries.getIds(-180.0, +90.0)
        boundaries.getIds(+180.0, -90.0)
    }

    @Test fun containing_ids_sorted_by_size_ascending() {
        val boundaries = CountryBoundaries(
            raster = listOf(cell(listOf("D", "B", "C", "A"))),
            rasterWidth = 1,
            geometrySizes = mapOf("A" to 10.0, "B" to 15.0, "C" to 100.0, "D" to 800.0))

        assertEquals(
            listOf("A", "B", "C", "D"), boundaries.getIds(1.0, 1.0)
        )
    }

    @Test fun intersecting_ids_in_bbox_is_merged_correctly() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A")),
                cell(listOf("B")),
                cell(listOf("C")),
                cell(listOf("D", "E"))
            ), 2, emptyMap()
        )

        assertTrue(
            boundaries.getIntersectingIds(-10.0, -10.0, 10.0, 10.0).containsAll(
                listOf("A", "B", "C", "D", "E")
            )
        )
    }

    @Test fun intersecting_ids_in_bbox_wraps_longitude_correctly() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A")),
                cell(listOf("B")),
                cell(listOf("C"))
            ), 3, emptyMap()
        )

        assertTrue(
            boundaries.getIntersectingIds(170.0, 0.0, -170.0, 1.0).containsAll(listOf("A", "C"))
        )
    }

    @Test fun containing_ids_in_bbox_wraps_longitude_correctly() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A", "B", "C")),
                cell(listOf("X")),
                cell(listOf("A", "B"))
            ), 3, emptyMap()
        )

        assertTrue(
            boundaries.getContainingIds(170.0, 0.0, -170.0, 1.0).containsAll(listOf("A", "B"))
        )
    }

    @Test fun containing_ids_in_bbox_returns_correct_result_when_one_cell_is_empty() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(),
                cell(listOf("A")),
                cell(listOf("A")),
                cell(listOf("A"))
            ), 2, emptyMap()
        )

        assertTrue(boundaries.getContainingIds(-10.0, -10.0, 10.0, 10.0).isEmpty())
    }

    @Test fun containing_ids_in_bbox_is_merged_correctly() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A", "B")),
                cell(listOf("B", "A")),
                cell(listOf("C", "B", "A")),
                cell(listOf("D", "A"))
            ), 2, emptyMap()
        )

        assertTrue(
            boundaries.getContainingIds(-10.0, -10.0, 10.0, 10.0).containsAll(listOf("A"))
        )
    }

    @Test fun containing_ids_in_bbox_is_merged_correctly_and_nothing_is_left() {
        val boundaries = CountryBoundaries(
            listOf(
                cell(listOf("A")),
                cell(listOf("B")),
                cell(listOf("C")),
                cell(listOf("D"))
            ), 2, emptyMap()
        )

        assertTrue(boundaries.getContainingIds(-10.0, -10.0, 10.0, 10.0).isEmpty())
    }

    @Test fun latitude_out_of_bounds_throws() {
        val b = emptyBoundaries()
        val e = IllegalArgumentException::class

        assertFailsWith(e) { b.getContainingIds(0.0, -90.0001, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, +90.0001, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, 0.0, -90.0001) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, 0.0, +90.0001) }

        assertFailsWith(e) { b.getIds(0.0, -90.0001) }
        assertFailsWith(e) { b.getIds(0.0, +90.0001) }
    }

    @Test fun max_latitude_is_smaller_than_min_latitude_throws() {
        val b = emptyBoundaries()
        val e = IllegalArgumentException::class

        assertFailsWith(e) { b.getContainingIds(0.0, 1.1, 0.0, 1.0) }
    }

    @Test fun non_finite_numbers_throws() {
        val b = emptyBoundaries()
        val e = IllegalArgumentException::class

        assertFailsWith(e) { b.getIds(NaN, 0.0) }
        assertFailsWith(e) { b.getIds(0.0, NaN) }

        assertFailsWith(e) { b.getIds(POSITIVE_INFINITY, 0.0) }
        assertFailsWith(e) { b.getIds(0.0, POSITIVE_INFINITY) }

        assertFailsWith(e) { b.getIds(0.0, NEGATIVE_INFINITY) }
        assertFailsWith(e) { b.getIds(NEGATIVE_INFINITY, 0.0) }

        assertFailsWith(e) { b.getContainingIds(NaN, 0.0, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, NaN, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, NaN, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, 0.0, NaN) }

        assertFailsWith(e) { b.getContainingIds(POSITIVE_INFINITY, 0.0, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, POSITIVE_INFINITY, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, POSITIVE_INFINITY, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, 0.0, POSITIVE_INFINITY) }

        assertFailsWith(e) { b.getContainingIds(NEGATIVE_INFINITY, 0.0, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, NEGATIVE_INFINITY, 0.0, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, NEGATIVE_INFINITY, 0.0) }
        assertFailsWith(e) { b.getContainingIds(0.0, 0.0, 0.0, NEGATIVE_INFINITY) }
    }
}

private fun emptyBoundaries(): CountryBoundaries {
    return CountryBoundaries(listOf(cell()), 1, emptyMap())
}

private fun cell(containing: List<String> = emptyList(), intersecting: List<CountryAreas> = emptyList()) =
    CountryBoundariesCell(containing, intersecting)
