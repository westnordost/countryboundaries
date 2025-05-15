package de.westnordost.countryboundaries

import kotlin.test.*

internal class CountryBoundariesCellTest {
    @Test fun definite_ids() {
		assertEquals(
			listOf("A", "C"),
			cell(containing = listOf("A", "C")).getIds(p(0, 0))
		)
	}

    @Test fun in_geometry_ids() {
		assertEquals(
			listOf("B"),
			cell(intersecting = listOf(B)).getIds(p(1, 1))
		)
	}

    @Test fun dont_get_out_of_geometry_ids() {
        assertEquals(
            emptyList(),
            cell(intersecting = listOf(B)).getIds(p(4, 4))
        )
    }

    @Test fun definite_and_in_geometry_ids() {
		assertEquals(
			listOf("A", "B"),
			cell(containing = listOf("A"), intersecting = listOf(B)).getIds(p(1, 1))
		)
	}

    @Test fun all_ids() {
		assertEquals(
			listOf("A", "B"),
			cell(containing = listOf("A"), intersecting = listOf(B)).getAllIds()
		)
	}

    @Test fun is_in_any_definitely() {
		assertTrue(
			cell(containing = listOf("A")).isInAny(p(0, 0), listOf("B", "A"))
		)
	}

    @Test fun is_in_any_definitely_not() {
		assertFalse(
			cell(containing = listOf("A")).isInAny(p(0, 0), listOf("B"))
		)
	}

    @Test fun is_in_any_in_geometry() {
		assertTrue(
			cell(intersecting = listOf(B)).isInAny(p(1, 1), listOf("B"))
		)
	}

    @Test fun is_in_any_out_of_geometry() {
		assertFalse(
			cell(intersecting = listOf(B)).isInAny(p(4, 4), listOf("B"))
		)
	}
}

private val B = CountryAreas(
	id = "B",
	outer = listOf(listOf(p(0, 0), p(0, 2), p(2, 2), p(2, 0))),
	inner = listOf()
)

private fun p(x: Int, y: Int) = Point(x.toUShort(), y.toUShort())

private fun cell(containing: List<String> = emptyList(), intersecting: List<CountryAreas> = emptyList()) =
    CountryBoundariesCell(containing, intersecting)
