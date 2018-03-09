package de.westnordost.countryboundaries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RasterSpatialIndex implements SpatialIndex
{
	private final short[] raster;
	private final int bitmapWidth;
	private final QueryResult[] indices;

	public RasterSpatialIndex(short[] raster, int rasterWidth, QueryResult[] indices)
	{
		if(indices.length > Short.MAX_VALUE) {
			throw new IllegalArgumentException("RasterSpatialIndex only supports up to 32768 different indices");
		}
		this.raster = raster;
		this.indices = indices;
		this.bitmapWidth = rasterWidth;
	}

	private QueryResult getPixel(int x, int y)
	{
		int index = raster[y * bitmapWidth + x];
		return indices[index];
	}

	@Override
	public QueryResult query(double longitude, double latitude)
	{
		if (latitude < -90 || latitude > 90) throw new IllegalArgumentException("latitude is out of bounds");
		return getPixel(longitudeToPixelX(longitude), latitudeToPixelY(latitude));
	}

	@Override
	public QueryResult query(double minLong, double minLat, double maxLong, double maxLat)
	{
		if (minLat < -90 || minLat > 90) throw new IllegalArgumentException("minLat is out of bounds");
		if (maxLat < -90 || maxLat > 90) throw new IllegalArgumentException("maxLat is out of bounds");
		if (minLat > maxLat) throw new IllegalArgumentException("maxLat is smaller than minLat");

		int minX = longitudeToPixelX(minLong);
		int minY = latitudeToPixelY(minLat);
		int maxX = longitudeToPixelX(maxLong);
		int maxY = latitudeToPixelY(maxLat);

		// might wrap around
		int stepsX = minX > maxX ? bitmapWidth - minX + maxX : maxX - minX;

		Set<String> containingIds = new HashSet<>();
		Set<String> possiblyContainingIds = new HashSet<>();
		boolean first = true;
		for (int xStep = 0; xStep <= stepsX; ++xStep)
		{
			int x = (minX + xStep) % bitmapWidth;

			for (int y = minY; y <= maxY; y++)
			{
				QueryResult r = getPixel(x, y);
				if(first)
				{
					containingIds.addAll(r.getContainingIds());
					first = false;
				}
				else
				{
					Iterator<String> it = containingIds.iterator();
					while(it.hasNext())
					{
						String containingCountryCode = it.next();
						if(!r.getContainingIds().contains(containingCountryCode))
						{
							possiblyContainingIds.add(containingCountryCode);
							it.remove();
						}
					}
					for (String containingCountryCode : r.getContainingIds())
					{
						if(!containingIds.contains(containingCountryCode))
						{
							possiblyContainingIds.add(containingCountryCode);
						}
					}
				}
				possiblyContainingIds.addAll(r.getIntersectingIds());
			}
		}
		containingIds.removeAll(possiblyContainingIds);

		return new QueryResult(containingIds, possiblyContainingIds);
	}

	private int longitudeToPixelX(double longitude)
	{
		return (int) Math.floor((180 + normalize(longitude, -180, 360)) / 360.0 * bitmapWidth);
	}

	private int latitudeToPixelY(double latitude)
	{
		int bitmapHeight = raster.length / bitmapWidth;
		return (int) Math.floor((90 - latitude) / 180.0 * bitmapHeight);
	}

	private static double normalize(double value, double startAt, double base)
	{
		while (value < startAt) value += base;
		while (value > startAt + base) value -= base;
		return value;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RasterSpatialIndex that = (RasterSpatialIndex) o;

		return
				bitmapWidth == that.bitmapWidth
				&& Arrays.equals(raster, that.raster)
				&& Arrays.equals(indices, that.indices);
	}

	@Override public int hashCode()
	{
		return bitmapWidth + 31 * (Arrays.hashCode(raster) + 31 * Arrays.hashCode(indices));
	}
}
