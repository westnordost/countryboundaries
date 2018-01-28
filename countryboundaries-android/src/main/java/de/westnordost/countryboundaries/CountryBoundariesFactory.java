package de.westnordost.countryboundaries;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vividsolutions.jts.geom.GeometryCollection;

import java.io.IOException;
import java.io.InputStream;

import de.westnordost.countryboundaries.geojson.GeoJsonReader;

public class CountryBoundariesFactory
{
	public static CountryBoundaries createCountryBoundaries(
			InputStream boundaries, InputStream bitmap, InputStream indices) throws IOException
	{
		String boundariesString = IOStreamUtils.readToString(boundaries);

		return new CountryBoundaries(
				(GeometryCollection) new GeoJsonReader(true).read(boundariesString),
				createCountryBitmap(bitmap, indices));
	}

	static CountryBitmap createCountryBitmap(InputStream bitmapStream, InputStream indicesStream)
			throws IOException
	{
		Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		String indicesString = IOStreamUtils.readToString(indicesStream);
		CountryQueryResult[] indices = CountryBoundariesIndicesUtil.createFromJson(indicesString);

		return new CountryBitmap(pixels, width, indices);
	}
}
