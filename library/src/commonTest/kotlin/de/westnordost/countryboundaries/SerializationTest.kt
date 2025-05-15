package de.westnordost.countryboundaries

import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals


internal class SerializationTest {

    @Test fun serialization_works() {
        val sizes = mapOf("A" to 123.0, "B" to 64.4)
        val A = listOf(p(0, 0), p(0, 1), p(1, 0))
        val B = listOf(p(0, 0), p(0, 3), p(3, 3), p(3, 0))
        val Bh = listOf(p(1, 1), p(2, 1), p(2, 2), p(1, 2))

        val index = CountryBoundaries(
            raster = listOf(
                cell(),
                cell(containing = listOf("A", "B")),
                cell(
                    containing = listOf("B"),
                    intersecting = listOf(CountryAreas("A", listOf(A), listOf()))
                ),
                cell(
                    containing = listOf(),
                    intersecting = listOf(
                        CountryAreas("B", listOf(B), listOf(Bh)),
                        CountryAreas("C", listOf(B, A), listOf(Bh))
                    )
                ),
            ),
            rasterWidth = 2,
            geometrySizes = sizes
        )

        val buffer = Buffer()

        index.serializeTo(buffer)
        val index2 = CountryBoundaries.deserializeFrom(buffer)

        assertEquals(index.rasterWidth, index2.rasterWidth)
        assertEquals(index.geometrySizes, index2.geometrySizes)
        assertEquals(index.raster, index2.raster)
    }
}

private fun cell(containing: List<String> = emptyList(), intersecting: List<CountryAreas> = emptyList()) =
    CountryBoundariesCell(containing, intersecting)

private fun p(x: Int, y: Int) = Point(x.toUShort(), y.toUShort())
