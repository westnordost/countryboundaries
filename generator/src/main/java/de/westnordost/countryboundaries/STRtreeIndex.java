package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class STRtreeIndex implements SpatialIndex
{
	private final STRtree index;

	public STRtreeIndex(GeometryCollection countriesBoundaries)
	{
		index = new STRtree();

		for (int i = 0; i < countriesBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countriesBoundaries.getGeometryN(i);

			Object userData = countryBoundary.getUserData();
			if(userData != null && userData instanceof String)
			{
				index.insert(countryBoundary.getEnvelopeInternal(), countryBoundary);
			}
		}
	}

	@Override
	public QueryResult query(double longitude, double latitude)
	{
		return query(new Envelope(new Coordinate(longitude, latitude)));
	}

	@Override
	public QueryResult query(double minLong, double minLat, double maxLong, double maxLat)
	{
		return query(new Envelope(minLong, maxLong, minLat, maxLat));
	}

	private QueryResult query(Envelope e)
	{
		List<Geometry> geometries = index.query(e);
		Set<String> possibleMatches = new HashSet<>();
		for (Geometry geometry : geometries)
		{
			possibleMatches.add((String) geometry.getUserData());
		}

		return new QueryResult(null, possibleMatches);
	}
}
