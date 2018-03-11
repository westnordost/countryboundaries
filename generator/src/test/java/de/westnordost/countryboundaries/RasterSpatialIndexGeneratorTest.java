package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.junit.Test;

import static org.junit.Assert.*;

public class RasterSpatialIndexGeneratorTest
{
	private static final int WGS84 = 4326;
	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	@Test public void generatedIndexGivesSameResultAsWithoutIndex()
	{
		TestCountryBoundaries boundaries = new TestCountryBoundaries();

		int width = 36;
		int height = 18;
		RasterSpatialIndex index = new RasterSpatialIndexGenerator().generate(36,18,boundaries);

		for(int y=0; y < height; ++y)
		{
			for(int x=0; x < width; ++x)
			{
				double lonMin = -180.0 + 360.0 * x/width;
				double latMax = +90.0 - 180.0 * y/height;
				double lonMax = -180.0 + 360.0 * (x+1-1e-7)/width;
				double latMin = +90.0 - 180.0 * (y+1-1e-7)/height;
				System.out.println();
				assertEquals(
						"For (" + lonMin + "," + latMin + "," +lonMax + "," + latMax +")",
						boundaries.getIds(lonMin, latMin, lonMax, latMax),
						index.query(lonMin, latMin, lonMax, latMax));
			}
		}
	}

	private class TestCountryBoundaries extends CountryBoundaries
	{
		TestCountryBoundaries()
		{
			super(factory.createGeometryCollection(new Geometry[]{}), null);
		}

		@Override
		public QueryResult getIds(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
		{
			return new QueryResult(new String[]{
					"" + minLongitude, "" + minLatitude, "" + maxLongitude, "" + maxLatitude},
					null);
		}
	}
}