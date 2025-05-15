package de.westnordost.countryboundaries

internal data class Point(val x: UShort, val y: UShort) {
    // for java because access to UShort is super awkward
    internal constructor(x: Int, y: Int) : this(x.toUShort(), y.toUShort())
}
