package de.westnordost.countryboundaries

import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.readDouble
import kotlinx.io.readString
import kotlinx.io.readUByte
import kotlinx.io.readUShort

internal fun Source.readRegionIndex() : CountryBoundaries {
    val version = readUShort().toInt()
    if (version != 2) {
        throw IOException(
            "Wrong version number '$version' of the file serialization format " +
            "(expected: '2'). You may need to get the current version of the data."
        )
    }

    val geometrySizesCount = readInt()
    if (geometrySizesCount < 0) throw IOException("Invalid data")

    val geometrySizes = HashMap<String, Double>(geometrySizesCount)
    repeat(geometrySizesCount) {
        val id = readUTF().interned()
        val size = readDouble()
        geometrySizes.put(id, size)
    }

    val rasterWidth = readInt()
    if (rasterWidth < 0) throw IOException("Invalid data")

    val rasterSize = readInt()
    if (rasterSize < 0) throw IOException("Invalid data")

    val raster = List(rasterSize) { readCell() }

    return CountryBoundaries(raster, rasterWidth, geometrySizes)
}

private fun Source.readCell(): CountryBoundariesCell {
    val containingIdsSize = readUByte().toInt()

    val containingIds = List(containingIdsSize) { readUTF().interned() }

    val intersectingAreasSize = readUByte().toInt()
    val intersectingAreas = List(intersectingAreasSize) { readAreas() }

    return CountryBoundariesCell(containingIds, intersectingAreas)
}

private fun Source.readAreas(): CountryAreas {
    val id = readUTF().interned()
    val outer = readPolygons()
    val inner = readPolygons()

    return CountryAreas(id, outer, inner)
}

private fun Source.readPolygons(): List<List<Point>> {
    val polygonsSize = readUByte().toInt()
    return List(polygonsSize) { readRing() }
}

private fun Source.readRing(): List<Point> {
    val ringSize = readInt()
    if (ringSize < 0) {
        throw IOException("Invalid data")
    }
    return List(ringSize) { readPoint() }
}

private fun Source.readPoint(): Point {
    val x = readUShort()
    val y = readUShort()
    return Point(x, y)
}

// mimics https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html#readUTF--
private fun Source.readUTF(): String {
    val length = readUShort()
    return readString(length.toLong())
}

private inline fun <reified T> List(capacity: Int, noinline init: (Int) -> T): List<T> {
    // memory improvement: Don't even create new list if it is empty
    if (capacity == 0) return emptyList()
    return Array(capacity, init).asList()
}

internal expect fun String.interned(): String