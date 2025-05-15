package de.westnordost.countryboundaries

import kotlin.test.*

internal class AreasTest {
    @Test fun covers_simple_polygon() {
        assertTrue(CountryAreas("A", listOf(BIG_SQUARE), listOf()).covers(p(5, 5)))
    }

    @Test fun does_not_cover_hole() {
        assertFalse(CountryAreas("A", listOf(BIG_SQUARE), listOf(HOLE)).covers(p(5, 5)))
    }

    @Test fun does_cover_polygon_in_hole() {
        assertTrue(CountryAreas("A", listOf(BIG_SQUARE, SMALL_SQUARE), listOf(HOLE)).covers(p(5, 5)))
    }

    @Test fun only_upper_left_edge_counts_as_inside() {
        val areas = CountryAreas("A", listOf(BIG_SQUARE), listOf())

        assertTrue(areas.covers(p(0, 0)))
        assertTrue(areas.covers(p(5, 0)))
        assertTrue(areas.covers(p(0, 5)))
        assertFalse(areas.covers(p(0, 10)))
        assertFalse(areas.covers(p(10, 0)))
        assertFalse(areas.covers(p(5, 10)))
        assertFalse(areas.covers(p(10, 5)))
        assertFalse(areas.covers(p(10, 10)))
    }
}

private val BIG_SQUARE = listOf(p(0, 0), p(0, 10), p(10, 10), p(10, 0))

private val HOLE = listOf(p(2, 2), p(2, 8), p(8, 8), p(8, 2))

private val SMALL_SQUARE = listOf(p(4, 4), p(4, 6), p(6, 6), p(6, 4))