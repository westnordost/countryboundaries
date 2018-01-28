package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountrySTRtree implements CountryBoundariesIndex
{
	private static String ISO3166_1_ALPHA2 = "ISO3166-1:alpha2";
	private static String ISO3166_2 = "ISO3166-2";

	private final STRtree index;

	public CountrySTRtree(GeometryCollection countriesBoundaries)
	{
		index = new STRtree();

		for (int i = 0; i < countriesBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countriesBoundaries.getGeometryN(i);

			Map<String, String> props = (Map<String, String>) countryBoundary.getUserData();
			if (props == null) continue;
			if (props.containsKey(ISO3166_2) || props.containsKey(ISO3166_1_ALPHA2))
			{
				insertIntoIndex(countryBoundary);
			}
		}
	}

	private void insertIntoIndex(Geometry countryBoundary)
	{
		// split multipolygons into its elements and copy the properties to them to make better use
		// of the index data structure. I.e. the United Kingdom would be on the top level of the
		// index since because with all those oversees territories, it spans almost the whole
		// world.
		if (countryBoundary instanceof GeometryCollection)
		{
			GeometryCollection countryBoundaries = (GeometryCollection) countryBoundary;
			for (int j = 0; j < countryBoundaries.getNumGeometries(); j++)
			{
				Geometry countryBoundariesSegment = countryBoundaries.getGeometryN(j);
				index.insert(countryBoundariesSegment.getEnvelopeInternal(), countryBoundary);
			}
		} else
		{
			index.insert(countryBoundary.getEnvelopeInternal(), countryBoundary);
		}
	}

	@Override
	public CountryQueryResult query(double longitude, double latitude)
	{
		return query(new Envelope(new Coordinate(longitude, latitude)));
	}

	@Override
	public CountryQueryResult query(double minLong, double minLat, double maxLong, double maxLat)
	{
		return query(new Envelope(minLong, maxLong, minLat, maxLat));
	}

	private CountryQueryResult query(Envelope e)
	{
		List<Geometry> geometries = index.query(e);
		Set<String> possibleMatches = new HashSet<>();
		for (Geometry geometry : geometries)
		{
			possibleMatches.add(getIsoCode(geometry));
		}

		return new CountryQueryResult(null, possibleMatches);
	}

	private static String getIsoCode(Geometry geometry)
	{
		Map<String, String> props = (Map<String, String>) geometry.getUserData();
		if (props.containsKey(ISO3166_1_ALPHA2))
			return props.get(ISO3166_1_ALPHA2);
		else if (props.containsKey(ISO3166_2))
			return props.get(ISO3166_2);
		return null;
	}
}
