package de.westnordost.countryboundaries;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountryBoundaries
{
	final CountryBoundariesCell[] raster;
	final int rasterWidth;
	final Map<String, Double> geometrySizes;

	CountryBoundaries(
			CountryBoundariesCell[] raster, int rasterWidth, Map<String, Double> geometrySizes)
	{
		this.raster = raster;
		this.rasterWidth = rasterWidth;
		this.geometrySizes = geometrySizes;
	}

	public static CountryBoundaries load(InputStream is) throws IOException
	{
		return new CountryBoundariesDeserializer().read(new DataInputStream(is));
	}

	/** @param longitude longitude of geo position (-180...180)
	 *  @param latitude latitude of geo position (-90...90)
	 *  @param ids ids of the countries to look for. Note that if you have many ids, you should use
	 *             a Set to increase performance
	 *
	 *  @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite)
	 *          or latitude is not between -90.0 and +90.0
	 *
	 *  @return whether the given position is in any of the countries with the given ids */
	public boolean isInAny(double longitude, double latitude, Collection<String> ids)
	{
		validatePosition(longitude, latitude);
		longitude = normalize(longitude, -180.0, 360.0);
		int cellX = longitudeToCellX(longitude);
		int cellY = latitudeToCellY(latitude);
		int localX = longitudeToLocalX(cellX, longitude);
		int localY = latitudeToLocalY(cellY, latitude);
		return getCell(cellX, cellY).isInAny(localX, localY, ids);
	}

	/** @param longitude longitude of geo position (-180...180)
	 *  @param latitude latitude of geo position (-90...90)
	 *  @param id id of the country to look for
	 *
	 *  @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite)
	 *          or latitude is not between -90.0 and +90.0
	 *
	 *  @return whether the given position is in the country with the given id */
	public boolean isIn(double longitude, double latitude, String id)
	{
		return isInAny(longitude, latitude, Collections.singleton(id));
	}

	/** @param longitude longitude of geo position (-180...180)
	 *  @param latitude latitude of geo position (-90...90)
	 *  @return the ids of the countries the given position is contained in, ordered by size ascending */
	public List<String> getIds(double longitude, double latitude)
	{
		validatePosition(longitude, latitude);
		longitude = normalize(longitude, -180.0, 360.0);
		int cellX = longitudeToCellX(longitude);
		int cellY = latitudeToCellY(latitude);
		int localX = longitudeToLocalX(cellX, longitude);
		int localY = latitudeToLocalY(cellY, latitude);

		List<String> result = getCell(cellX, cellY).getIds(localX, localY);
		Collections.sort(result, this::compareSize);
		return result;
	}

	/** Identify which countries are guaranteed to contain the given bounding box fully.
	 *  The given bounding box may wrap around the 180th longitude, i.e minLongitude = 170 and
	 *  maxLongitude = -170.
	 *
	 *  @param minLongitude minimum longitude of geo position (-180...180)
	 *  @param minLatitude minimum latitude of geo position (-90...90)
	 *  @param maxLongitude maximum longitude of geo position (-180...180)
	 *  @param maxLatitude maximum latitude of geo position (-90...90)
	 *
	 *  @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite),
	 *          minLatitude is greater than maxLatitude or any latitude is not between
	 *          -90.0 and +90.0
	 *
	 *  @return the ids of the countries the given bounding box is guaranteed to be contained in,
	 *          not in any particular order */
	public Set<String> getContainingIds(
			double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		Set<String> ids = new HashSet<>();
		final boolean[] firstCell = {true};
		forCellsIn(minLongitude, minLatitude, maxLongitude, maxLatitude, cell ->
		{
			if(firstCell[0])
			{
				ids.addAll(cell.containingIds);
				firstCell[0] = false;
			}
			else
			{
				ids.retainAll(cell.containingIds);
				if (ids.isEmpty()) return false;
			}
			return true;
		});
		return ids;
	}

	/** Identify which countries intersect with the given bounding box. The given bounding box may
	 *  wrap around the 180th longitude, i.e minLongitude = 170 and maxLongitude = -170.
	 *
	 *  @param minLongitude minimum longitude of geo position (-180...180)
	 *  @param minLatitude minimum latitude of geo position (-90...90)
	 *  @param maxLongitude maximum longitude of geo position (-180...180)
	 *  @param maxLatitude maximum latitude of geo position (-90...90)
	 *
	 *  @throws IllegalArgumentException if any parameter is not finite (NaN or Infinite),
	 *          minLatitude is greater than maxLatitude or any latitude is not between
	 *          -90.0 and +90.0
	 *
	 *  @return the ids of the countries the given bounding box intersects with, not in any
	 *          particular order */
	public Set<String> getIntersectingIds(
			double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		Set<String> ids = new HashSet<>();
		forCellsIn(minLongitude, minLatitude, maxLongitude, maxLatitude, cell ->
		{
			ids.addAll(cell.getAllIds());
			return true;
		});
		return ids;
	}

	private void forCellsIn(
			double minLongitude, double minLatitude, double maxLongitude, double maxLatitude,
			CellRunnable runnable)
	{
		if (!Double.isFinite(minLongitude))
			throw new IllegalArgumentException("minLongitude must be finite");
		if (!Double.isFinite(minLatitude))
			throw new IllegalArgumentException("minLatitude must be finite");
		if (!Double.isFinite(maxLongitude))
			throw new IllegalArgumentException("maxLongitude must be finite");
		if (!Double.isFinite(maxLatitude))
			throw new IllegalArgumentException("maxLatitude must be finite");
		if (minLatitude < -90 || minLatitude > 90)
			throw new IllegalArgumentException("minLatitude is out of bounds");
		if (maxLatitude < -90 || maxLatitude > 90)
			throw new IllegalArgumentException("maxLatitude is out of bounds");
		if (minLatitude > maxLatitude)
			throw new IllegalArgumentException("maxLatitude is smaller than minLatitude");

		minLongitude = normalize(minLongitude, -180, 360);
		maxLongitude = normalize(maxLongitude, -180, 360);
		int minX = longitudeToCellX(minLongitude);
		int maxY = latitudeToCellY(minLatitude);
		int maxX = longitudeToCellX(maxLongitude);
		int minY = latitudeToCellY(maxLatitude);

		// might wrap around
		int stepsX = minX > maxX ? rasterWidth - minX + maxX : maxX - minX;

		for (int xStep = 0; xStep <= stepsX; ++xStep)
		{
			int x = (minX + xStep) % rasterWidth;
			for (int y = minY; y <= maxY; y++)
			{
				if (!runnable.run(getCell(x, y))) return;
			}
		}
	}

	private interface CellRunnable
	{
		boolean run(CountryBoundariesCell cell);
	}

	private CountryBoundariesCell getCell(int x, int y) {
		return raster[y * rasterWidth + x];
	}

	private void validatePosition(double longitude, double latitude) {
		if (!Double.isFinite(longitude))
			throw new IllegalArgumentException("longitude must be finite");
		if (!Double.isFinite(latitude))
			throw new IllegalArgumentException("latitude must be finite");
		if (latitude < -90 || latitude > 90)
			throw new IllegalArgumentException("latitude is out of bounds");
	}

	private int longitudeToCellX(double longitude) {
		return (int) Math.min(
				rasterWidth - 1,
				Math.floor(rasterWidth * (180 + longitude) / 360.0)
		);
	}

	private int latitudeToCellY(double latitude) {
		int rasterHeight = raster.length / rasterWidth;
		return (int) Math.max(
				0,
				Math.ceil(rasterHeight * (90 - latitude) / 180.0) - 1
		);
	}

	private int longitudeToLocalX(int cellX, double longitude) {
		double cellLongitude = -180.0 + 360.0 * cellX / rasterWidth;
		return (int) ((longitude - cellLongitude) * 360.0 * 0xffff / rasterWidth);
	}

	private int latitudeToLocalY(int cellY, double latitude) {
		int rasterHeight = raster.length / rasterWidth;
		double cellLatitude = +90 - 180.0 * (cellY + 1) / rasterHeight;
		return (int) ((latitude - cellLatitude) * 180.0 * 0xffff / rasterHeight);
	}

	private static double normalize(double value, double startAt, double base)
	{
		value = value % base;
		if (value < startAt) value += base;
		else if (value >= (startAt + base)) value -= base;
		return value;
	}

	private double getSize(String id)
	{
		Double size = geometrySizes.get(id);
		return size != null ? size : 0;
	}

	private int compareSize(String isoCode1, String isoCode2)
	{
		return (int) (getSize(isoCode1) - getSize(isoCode2));
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CountryBoundaries that = (CountryBoundaries) o;

		return
				rasterWidth == that.rasterWidth
						&& Arrays.equals(raster, that.raster)
						&& geometrySizes.equals(that.geometrySizes);
	}

	@Override public int hashCode()
	{
		return rasterWidth + 31 * (Arrays.hashCode(raster) + 31 * geometrySizes.hashCode());
	}
}
