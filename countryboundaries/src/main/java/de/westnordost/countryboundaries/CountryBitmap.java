package de.westnordost.countryboundaries;

import java.util.HashSet;
import java.util.Set;

// TODO test
public class CountryBitmap implements CountryBoundariesIndex
{
	private final int[] bitmap;
	private final int bitmapWidth;
	private final CountryQueryResult[] indices;

	public CountryBitmap(int[] bitmap, int bitmapWidth, CountryQueryResult[] indices)
	{
		this.bitmap = bitmap;
		this.indices = indices;
		this.bitmapWidth = bitmapWidth;
	}

	private CountryQueryResult getPixel(int x, int y)
	{
		int index = bitmap[y * bitmapWidth + x];
		return indices[index];
	}

	@Override
	public CountryQueryResult query(double longitude, double latitude)
	{
		return getPixel(longitudeToPixelX(longitude), latitudeToPixelY(latitude));
	}

	@Override
	public CountryQueryResult query(double minLong, double minLat, double maxLong, double maxLat)
	{
		int minX = longitudeToPixelX(minLong);
		int minY = latitudeToPixelY(minLat);
		int maxX = longitudeToPixelX(maxLong);
		int maxY = latitudeToPixelY(maxLat);

		if (minX > maxX) throw new IllegalArgumentException("maxLong is smaller than minLong");
		if (minY > maxY) throw new IllegalArgumentException("maxLat is smaller than minLat");

		Set<String> containingCountryCodes = new HashSet<>();
		Set<String> possiblyContainingCountryCodes = new HashSet<>();
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				CountryQueryResult r = getPixel(x, y);
				containingCountryCodes.addAll(r.getContainingCountryCodes());
				possiblyContainingCountryCodes.addAll(r.getIntersectingCountryCodes());
			}
		}
		containingCountryCodes.removeAll(possiblyContainingCountryCodes);

		return new CountryQueryResult(containingCountryCodes, possiblyContainingCountryCodes);
	}

	private int longitudeToPixelX(double longitude)
	{
		return (int) Math.floor((180 + normalize(longitude, -180, 360)) / 360.0 * bitmapWidth);
	}

	private int latitudeToPixelY(double latitude)
	{
		int bitmapHeight = bitmap.length / bitmapWidth;
		return (int) Math.floor((90 - normalize(latitude, -90, 180)) / 180.0 * bitmapHeight);
	}

	private static double normalize(double value, double startAt, double base)
	{
		while (value < startAt) value += base;
		while (value > startAt + base) value -= base;
		return value;
	}
}
