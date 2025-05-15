package de.westnordost.countryboundaries

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.*

internal class CountryBoundariesIntegrationTest {
    
    @Test fun return_correct_results_at_cell_edges() {
        val boundaries = load()

        // in clockwise order...
        assertEquals(listOf("HR"), boundaries.getIds(16.0, 45.5))
        assertEquals(listOf("HR"), boundaries.getIds(16.0, 46.0))
        assertEquals(listOf("HR"), boundaries.getIds(16.5, 46.0))
        assertEquals(listOf("HR"), boundaries.getIds(17.0, 46.0))
        assertEquals(listOf("HR"), boundaries.getIds(17.0, 45.5))

        assertEquals(listOf("BA-SRP", "BA"), boundaries.getIds(17.0, 45.0))
        assertEquals(listOf("BA-SRP", "BA"), boundaries.getIds(17.5, 45.0))
        assertEquals(listOf("BA-SRP", "BA"), boundaries.getIds(18.0, 45.0))
    }
    
    @Test fun containing_ids_at_180th_meridian() {
        assertEquals(
            setOf("RU"),
            load().getContainingIds(178.0, 66.0, -178.0, 68.0)
        )
    }

    @Test fun intersecting_ids_at_180th_meridian() {
        assertEquals(
            setOf("RU", "US-AK", "US"),
            load().getIntersectingIds(163.0, 50.0, -150.0, 67.0)
        )
    }

    @Test fun isInAny() {
        // The given position is in Myanmar and not in any of these countries
        assertFalse(load().isInAny(96.0, 21.0, listOf("BD", "DJ", "IR", "PS")))
    }

    @Test fun buesingen() {
        assertTrue(load().isIn(8.6910, 47.6973, "DE"))
    }

    @Test fun ids_sorts_by_size_ascending() {
        assertEquals(
            listOf("US-TX", "US"),
            load().getIds(-97.0, 33.0)
        )
    }
}

private fun load() =
    SystemFileSystem
        .source(Path("src/commonTest/resources","boundaries180x90.ser"))
        .buffered()
        .use { CountryBoundaries.deserializeFrom(it) }