package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.GeometryCollection;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountryBoundariesFactory
{


	public CountryBoundaries create(InputStream boundariesJsonStream, InputStream indicesJsonStream) throws IOException
	{
		String boundariesJson = readToString(boundariesJsonStream);
		String indicesJson = readToString(indicesJsonStream);

		GeometryCollection boundaries = (GeometryCollection) new GeoJsonReader().read(boundariesJson);
		... remove map-getUserData...!

		RasterSpatialIndex index = new RasterSpatialIndexJsonReader().read(indicesJson);
		return new CountryBoundaries(boundaries, index);
	}

	private static String readToString(InputStream is) throws IOException
	{
		try
		{
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1)
			{
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		}
		finally
		{
			if(is != null) is.close();
		}
	}
}
