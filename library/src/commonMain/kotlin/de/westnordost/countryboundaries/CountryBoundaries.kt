package de.westnordost.countryboundaries

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.math.ceil
import kotlin.math.floor

/**
 * A spatial index to look up in which country a given geo position is located fast.
 *
 * Construct with [CountryBoundaries.Companion.deserializeFrom].
 * */
public class CountryBoundaries internal constructor(
    internal val raster: List<CountryBoundariesCell>,
    internal val rasterWidth: Int,
    internal val geometrySizes: Map<String, Double>
) {
    private val rasterHeight = raster.size / rasterWidth

    /**
     * @param longitude longitude of geo position (-180...180)
     * @param latitude latitude of geo position (-90...90)
     * @param ids ids of the countries to look for. Note that if you have many ids, you should use
     *            a Set to increase performance
     *
     * @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite) or
     *                                  latitude is not between -90.0 and +90.0
     *
     * @return whether the given position is in any of the countries with the given ids
     */
    public fun isInAny(longitude: Double, latitude: Double, ids: Collection<String>): Boolean {
        var longitude = longitude
        validatePosition(longitude, latitude)
        longitude = normalizeLongitude(longitude)
        val cellX = longitudeToCellX(longitude)
        val cellY = latitudeToCellY(latitude)
        val localX = longitudeToLocalX(cellX, longitude)
        val localY = latitudeToLocalY(cellY, latitude)

        return getCell(cellX, cellY).isInAny(Point(localX, localY), ids)
    }

    /**
     * @param longitude longitude of geo position (-180...180)
     * @param latitude latitude of geo position (-90...90)
     * @param id id of the country to look for
     *
     * @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite)
     *         or latitude is not between -90.0 and +90.0
     *
     * @return whether the given position is in the country with the given id
     */
    public fun isIn(longitude: Double, latitude: Double, id: String): Boolean {
        return isInAny(longitude, latitude, listOf(id))
    }

    /**
     * @param longitude longitude of geo position (-180...180)
     * @param latitude latitude of geo position (-90...90)
     * @return the ids of the countries the given position is contained in, ordered by size
     *         ascending
     */
    public fun getIds(longitude: Double, latitude: Double): List<String> {
        var longitude = longitude
        validatePosition(longitude, latitude)
        longitude = normalizeLongitude(longitude)
        val cellX = longitudeToCellX(longitude)
        val cellY = latitudeToCellY(latitude)
        val localX = longitudeToLocalX(cellX, longitude)
        val localY = latitudeToLocalY(cellY, latitude)

        return getCell(cellX, cellY)
            .getIds(Point(localX, localY))
            .sortedBy { geometrySizes[it] ?: 0.0 }
    }

    /**
     * Identify which countries are guaranteed to contain the given bounding box fully.
     * The given bounding box may wrap around the 180th longitude, e.g. minLongitude = 170 and
     * maxLongitude = -170.
     *
     * @param minLongitude minimum longitude of geo position (-180...180)
     * @param minLatitude minimum latitude of geo position (-90...90)
     * @param maxLongitude maximum longitude of geo position (-180...180)
     * @param maxLatitude maximum latitude of geo position (-90...90)
     *
     * @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite),
     *                                  minLatitude is greater than maxLatitude or any latitude is
     *                                  not between -90.0 and +90.0
     *
     * @return the ids of the countries the given bounding box is guaranteed to be contained in,
     *         not in any particular order
     */
    public fun getContainingIds(
        minLongitude: Double, minLatitude: Double, maxLongitude: Double, maxLatitude: Double
    ): Set<String> {
        val ids = HashSet<String>()
        var firstCell = true
        for (cell in cellsIn(minLongitude, minLatitude, maxLongitude, maxLatitude)) {
            if (firstCell) {
                ids.addAll(cell.containingIds)
                firstCell = false
            } else {
                ids.retainAll(cell.containingIds)
                if (ids.isEmpty()) break
            }
        }
        return ids
    }

    /**
     * Identify which countries may intersect with the given bounding box. In other words, any
     * point within the given bounding box can only be in any of the returned countries (or none).
     *
     * So, from the technical point of view, it just returns which countries are in the same
     * cell(s) of the raster as the bounding box.
     *
     * The given bounding box may
     * wrap around the 180th longitude, i.e minLongitude = 170 and maxLongitude = -170.
     *
     * @param minLongitude minimum longitude of geo position (-180...180)
     * @param minLatitude minimum latitude of geo position (-90...90)
     * @param maxLongitude maximum longitude of geo position (-180...180)
     * @param maxLatitude maximum latitude of geo position (-90...90)
     *
     * @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite),
     * minLatitude is greater than maxLatitude or any latitude is not between
     * -90.0 and +90.0
     *
     * @return the ids of the countries the given bounding box may intersects with, not in any
     * particular order
     */
    public fun getIntersectingIds(
        minLongitude: Double, minLatitude: Double, maxLongitude: Double, maxLatitude: Double
    ): Set<String> {
        val ids = HashSet<String>()
        for (cell in cellsIn(minLongitude, minLatitude, maxLongitude, maxLatitude)) {
            ids.addAll(cell.getAllIds())
        }
        return ids
    }

    private fun cellsIn(
        minLongitude: Double, minLatitude: Double, maxLongitude: Double, maxLatitude: Double,
    ): Sequence<CountryBoundariesCell> = sequence {
        var minLongitude = minLongitude
        var maxLongitude = maxLongitude
        validateBounds(minLongitude, minLatitude, maxLongitude, maxLatitude)
        minLongitude = normalizeLongitude(minLongitude)
        maxLongitude = normalizeLongitude(maxLongitude)
        val minX = longitudeToCellX(minLongitude)
        val maxY = latitudeToCellY(minLatitude)
        val maxX = longitudeToCellX(maxLongitude)
        val minY = latitudeToCellY(maxLatitude)

        // might wrap around
        val stepsX = if (minX > maxX) rasterWidth - minX + maxX else maxX - minX

        for (xStep in 0..stepsX) {
            val x = (minX + xStep) % rasterWidth
            for (y in minY..maxY) {
                yield(getCell(x, y))
            }
        }
    }

    private fun getCell(x: Int, y: Int): CountryBoundariesCell =
        raster[y * rasterWidth + x]

    private fun longitudeToCellX(longitude: Double): Int =
        floor(rasterWidth * (180 + longitude) / 360.0).toInt().coerceAtMost(rasterWidth - 1)

    private fun latitudeToCellY(latitude: Double): Int =
        (ceil(rasterHeight * (90 - latitude) / 180.0) - 1).toInt().coerceAtLeast(0)

    private fun longitudeToLocalX(cellX: Int, longitude: Double): UShort {
        val cellLongitude = -180.0 + 360.0 * cellX / rasterWidth
        return ((longitude - cellLongitude) * rasterWidth * 0xffff / 360.0).toInt().toUShort()
    }

    private fun latitudeToLocalY(cellY: Int, latitude: Double): UShort {
        val cellLatitude = +90 - 180.0 * (cellY + 1) / rasterHeight
        return ((latitude - cellLatitude) * rasterHeight * 0xffff / 180.0).toInt().toUShort()
    }

    private fun validateBounds(
        minLongitude: Double, minLatitude: Double, maxLongitude: Double, maxLatitude: Double
    ) {
        require(minLongitude.isFinite()) { "minLongitude must be finite" }
        require(minLatitude.isFinite()) { "minLatitude must be finite" }
        require(maxLongitude.isFinite()) { "maxLongitude must be finite" }
        require(maxLatitude.isFinite()) { "maxLatitude must be finite" }
        require(minLatitude >= -90 && minLatitude <= 90) { "minLatitude is out of bounds" }
        require(maxLatitude >= -90 && maxLatitude <= 90) { "maxLatitude is out of bounds" }
        require(minLatitude <= maxLatitude) { "maxLatitude is smaller than minLatitude" }
    }

    private fun validatePosition(longitude: Double, latitude: Double) {
        require(longitude.isFinite()) { "longitude must be finite" }
        require(latitude.isFinite()) { "latitude must be finite" }
        require(latitude >= -90 && latitude <= 90) { "latitude is out of bounds" }
    }

    private fun normalizeLongitude(longitude: Double): Double =
        normalize(longitude, -180.0, 360.0)

    private fun normalize(value: Double, startAt: Double, base: Double): Double {
        var value = value
        value = value % base
        if (value < startAt) value += base
        else if (value >= (startAt + base)) value -= base
        return value
    }

    /** Serialize this RegionIndex to the given sink */
    internal fun serializeTo(sink: Sink) {
        sink.writeRegionIndex(this)
    }

    public companion object {
        /** Create a new RegionIndex by deserializing from the given source */
        public fun deserializeFrom(source: Source): CountryBoundaries =
            source.readRegionIndex()
    }
}