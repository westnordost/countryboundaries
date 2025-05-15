package de.westnordost.countryboundaries

/** One cell in the country boundaries grid  */
internal data class CountryBoundariesCell(
    val containingIds: Collection<String>,
    val intersectingAreas: Collection<CountryAreas>
) {
    /** Returns whether the given point is in any of the given ids  */
    fun isInAny(point: Point, ids: Collection<String>): Boolean =
        containingIds.any { it in ids } ||
        intersectingAreas.any { it.id in ids && it.covers(point) }

    /** Return all ids that cover the given point  */
    fun getIds(point: Point): List<String> =
        containingIds +
        intersectingAreas.filter { it.covers(point) }.map { it.id }

    /** Return all ids that completely cover or partly cover this cell  */
    fun getAllIds(): Collection<String> =
        containingIds +
        intersectingAreas.map { it.id }
}
