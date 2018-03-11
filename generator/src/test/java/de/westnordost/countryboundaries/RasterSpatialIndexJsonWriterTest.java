package de.westnordost.countryboundaries;

import org.junit.Test;

import static org.junit.Assert.*;

public class RasterSpatialIndexJsonWriterTest
{
	@Test public void writingWorks()
	{
		RasterSpatialIndex index = new RasterSpatialIndex(new short[]{0,1,0,2}, 2,
				new QueryResult[]
				{
					new QueryResult(new String[]{"A"}, null),
					new QueryResult(new String[]{"B"}, new String[]{"C"}),
					new QueryResult(null, new String[]{"D"})
				});
		assertEquals(
				"{\"raster\":[0,1,0,2],\"rasterWidth\":2," +
				"\"indices\":[[[\"A\"],[]],[[\"B\"],[\"C\"]],[[],[\"D\"]]]}",
				new RasterSpatialIndexJsonWriter().write(index));
	}
}