package de.westnordost.countryboundaries


internal fun cell(
    containing: List<String> = emptyList(),
    intersecting: List<CountryAreas> = emptyList()
) =
    CountryBoundariesCell(containing, intersecting)

internal fun p(x: Int, y: Int) =
    Point(x.toUShort(), y.toUShort())

