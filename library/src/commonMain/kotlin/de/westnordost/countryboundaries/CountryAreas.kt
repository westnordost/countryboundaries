package de.westnordost.countryboundaries

/** Represents the areas that one country with the given id covers.  */
internal data class CountryAreas(
    internal val id: String,
    internal val outer: List<List<Point>>,
    internal val inner: List<List<Point>>
) {
    /** Returns whether it contains the given point  */
    fun covers(point: Point): Boolean =
        outer.count { point.isInPolygon(it) } - inner.count { point.isInPolygon(it) } > 0

    // modified from:
    // Copyright 2000 softSurfer, 2012 Dan Sunday
    // This code may be freely used and modified for any purpose
    // providing that this copyright notice is included with it.
    // SoftSurfer makes no warranty for this code, and cannot be held
    // liable for any real or imagined damage resulting from its use.
    // Users of this code must verify correctness for their application.
    // http://geomalgorithms.com/a03-_inclusion.html

    /** Return whether this is in [polygon] */
    private fun Point.isInPolygon(polygon: List<Point>): Boolean {
        var wn = 0
        if (polygon.isEmpty()) return false
        var a = polygon[polygon.size - 1]
        for (b in polygon) {
            if (a.y <= y) {
                if (b.y > y && isLeftOf(a, b) > 0) {
                    ++wn
                }
            } else if (b.y <= y && isLeftOf(a, b) < 0) {
                --wn
            }
            a = b
        }
        return wn != 0
    }

    /** Return whether this is left of line spanned by [a] and [b] */
    private fun Point.isLeftOf(a: Point, b: Point): Long =
        // need to convert to long to avoid integer overflow
        (b.x.toLong() - a.x.toInt()) * (y.toLong() - a.y.toInt()) -
        (x.toLong() - a.x.toInt()) * (b.y.toLong() - a.y.toInt())

}
