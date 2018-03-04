package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.util.HashMap;
import java.util.Map;
public class GeometriesReader
{
	private static final int WGS84 = 4326;
	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	public Map<String, Geometry> read(String input)
	{
		Map<String, Geometry> result = new HashMap<>();

		String[] lines = input.split("\\r?\\n");

		for (String line : lines)
		{
			String[] lineArray = line.split(":");
			if(lineArray.length != 2) throw new RuntimeException();
			String id = lineArray[0];

			String[] polygonString = lineArray[1].split(";");
			if(polygonString.length == 0) throw new RuntimeException();

			Polygon[] polygons = new Polygon[polygonString.length];
			for(int p = 0; p < polygonString.length; ++p)
			{
				String[] ringsString = polygonString[p].split(",");
				if(ringsString.length == 0) throw new RuntimeException();

				LinearRing shell = null;
				LinearRing[] holes = new LinearRing[ringsString.length-1];
				for(int r = 0; r < ringsString.length; ++r)
				{
					String[] doubles = ringsString[r].split(" ");
					if(doubles.length % 2 != 0) throw new RuntimeException();

					Coordinate[] coords = new Coordinate[doubles.length/2];
					for (int d = 0; d < doubles.length; d+=2)
					{
						coords[d/2] = new Coordinate(
								Double.parseDouble(doubles[d]), Double.parseDouble(doubles[d+1]));
					}
					LinearRing ring = factory.createLinearRing(coords);
					if(r == 0) shell = ring;
					else       holes[r-1] = ring;
				}

				polygons[p] = factory.createPolygon(shell, holes);
			}

			if(polygons.length == 1)
			{
				result.put(id, polygons[0]);
			}
			else
			{
				result.put(id, factory.createMultiPolygon(polygons));
			}
		}
		return result;
	}
}
