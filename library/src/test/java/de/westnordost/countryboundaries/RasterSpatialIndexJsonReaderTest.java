package de.westnordost.countryboundaries;

import org.junit.Test;

import static org.junit.Assert.*;

public class RasterSpatialIndexJsonReaderTest
{
	@Test public void parsingWorks()
	{
		RasterSpatialIndex parsed = new RasterSpatialIndexJsonReader().read(
				"{\"raster\":[0,1,0,2],\"rasterWidth\":2," +
				"\"indices\":[[[\"A\"],[]], [[\"B\"],[\"C\"]], [[],[\"D\"]]]}");

		RasterSpatialIndex expected = new RasterSpatialIndex(new short[]{0,1,0,2}, 2,
				new QueryResult[]
				{
					new QueryResult(new String[]{"A"}, null),
					new QueryResult(new String[]{"B"}, new String[]{"C"}),
					new QueryResult(null, new String[]{"D"})
				});

		assertEquals(expected, parsed);
	}
}