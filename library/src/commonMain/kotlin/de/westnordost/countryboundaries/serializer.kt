package de.westnordost.countryboundaries

import kotlinx.io.Sink
import kotlinx.io.writeDouble
import kotlinx.io.writeUByte
import kotlinx.io.writeUShort
import kotlin.collections.iterator

internal fun Sink.writeCountryBoundaries(boundaries: CountryBoundaries) {
    // version
    writeUShort(2u)

    writeInt(boundaries.geometrySizes.size)
    for ((id, size) in boundaries.geometrySizes) {
        writeUTF(id)
        writeDouble(size)
    }
    writeInt(boundaries.rasterWidth)
    writeInt(boundaries.raster.size)
    for (cell in boundaries.raster) {
        writeCell(cell)
    }
}

private fun Sink.writeCell(cell: CountryBoundariesCell) {
    val containingIdsSize = cell.containingIds.size
    require(containingIdsSize <= UByte.MAX_VALUE.toInt()) {
        "At most 255 different areas per cell are supported (try a bigger raster)"
    }
    writeUByte(containingIdsSize.toUByte())
    for (id in cell.containingIds) {
        writeUTF(id)
    }

    val intersectingAreasSize = cell.intersectingAreas.size
    require(intersectingAreasSize <= UByte.MAX_VALUE.toInt()) {
        "At most 255 different areas per cell are supported (try a bigger raster)"
    }
    writeUByte(intersectingAreasSize.toUByte())
    for (areas in cell.intersectingAreas) {
        writeAreas(areas)
    }
}

private fun Sink.writeAreas(areas: CountryAreas) {
    writeUTF(areas.id)
    writePolygons(areas.outer)
    writePolygons(areas.inner)
}

private fun Sink.writePolygons(polygons: List<List<Point>>) {
    val polygonsCount = polygons.size
    require(polygonsCount <= UByte.MAX_VALUE.toInt()) {
        "At most 255 different polygons are supported per area (try a bigger raster)"
    }
    writeUByte(polygons.size.toUByte())
    for (ring in polygons) {
        writeRing(ring)
    }
}

private fun Sink.writeRing(points: List<Point>) {
    writeInt(points.size)
    for (point in points) {
        writePoint(point)
    }
}

private fun Sink.writePoint(point: Point) {
    writeUShort(point.x)
    writeUShort(point.y)
}


private fun Sink.writeUTF(string: String) {
    val bytes = string.encodeToByteArray()
    require(bytes.size <= UShort.MAX_VALUE.toInt()) { "ID too long" }
    writeUShort(bytes.size.toUShort())
    write(bytes)
}
