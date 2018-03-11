package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryBoundaries
{
	private static final int WGS84 = 4326;

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);
	private final Map<String, Geometry> geometriesByIds;
	private final SpatialIndex spatialIndex;
	private final Map<String, Double> geometrySizeCache;

	CountryBoundaries(GeometryCollection boundaries, SpatialIndex index)
	{
		this.spatialIndex = index;
		geometrySizeCache = new HashMap<>(400);
		geometriesByIds = new HashMap<>(400);

		for (int i = 0; i < boundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = boundaries.getGeometryN(i);

			Object userData = countryBoundary.getUserData();
			if(userData != null && userData instanceof String)
			{
				geometriesByIds.put((String) userData, countryBoundary);
			}
		}
	}

	public static CountryBoundaries load(InputStream boundariesJsonStream, InputStream indicesJsonStream) throws IOException
	{
		String boundariesJson = StreamUtils.readToString(boundariesJsonStream);
		String indicesJson = StreamUtils.readToString(indicesJsonStream);

		GeometryCollection boundaries = (GeometryCollection) new GeoJsonReader().read(boundariesJson);

		transformMapUserDataToStringUserData(boundaries);
		RasterSpatialIndex index = new RasterSpatialIndexJsonReader().read(indicesJson);
		return new CountryBoundaries(boundaries, index);
	}

	private static void transformMapUserDataToStringUserData(GeometryCollection geometries)
	{
		for (int i = 0; i < geometries.getNumGeometries(); i++)
		{
			Geometry g = geometries.getGeometryN(i);
			g.setUserData(((Map)g.getUserData()).get("id"));
		}
	}

	/** @return whether the given position is in any of the countries with the given ids */
	public boolean isInAny(double longitude, double latitude, Collection<String> ids)
	{
		QueryResult queryResult = spatialIndex.query(longitude, latitude);
		Collection<String> containingIds = queryResult.getContainingIds();
		for (String id : ids)
		{
			if(containingIds.contains(id)) return true;
		}
		Point point = factory.createPoint(new Coordinate(longitude, latitude, 0));
		Collection<String> intersectingIds = queryResult.getIntersectingIds();
		for (String id : ids)
		{
			if(intersectingIds.contains(id))
			{
				Geometry country = geometriesByIds.get(id);
				if(country != null && country.covers(point)) return true;
			}
		}
		return false;
	}

	/** @return whether the given position is in the country with the given id */
	public boolean isIn(double longitude, double latitude, String id)
	{
		return isInAny(longitude, latitude, Collections.singleton(id));
	}

	/** @return the ids of the countries the given position is contained in */
	public List<String> getIds(double longitude, double latitude)
	{
		QueryResult queryResult = spatialIndex.query(longitude, latitude);

		List<String> result = new ArrayList<>();
		result.addAll(queryResult.getContainingIds());

		Collection<String> possibleMatches = queryResult.getIntersectingIds();
		if (!possibleMatches.isEmpty())
		{
			Point point = factory.createPoint(new Coordinate(longitude, latitude, 0));

			for (String id : possibleMatches)
			{
				Geometry country = geometriesByIds.get(id);
				if (country != null && country.covers(point))
				{
					result.add(id);
				}
			}
		}
		Collections.sort(result, this::compareSize);
		return result;
	}

	/** @return the ids of the countries the given bounding box is contained in or intersect with */
	public QueryResult getIds(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		QueryResult queryResult = spatialIndex.query(minLongitude, minLatitude, maxLongitude, maxLatitude);

		List<String> containingIds = new ArrayList<>();
		List<String> intersectingIds = new ArrayList<>();
		containingIds.addAll(queryResult.getContainingIds());

		Collection<String> possibleMatches = queryResult.getIntersectingIds();
		if (!possibleMatches.isEmpty())
		{
			Polygon box = createBounds(minLongitude, minLatitude, maxLongitude, maxLatitude);
			for (String countryCode : possibleMatches)
			{
				Geometry country = geometriesByIds.get(countryCode);
				if (country != null)
				{
					IntersectionMatrix im = country.relate(box);
					if (im.isCovers())
					{
						containingIds.add(countryCode);
					} else if (!im.isDisjoint())
					{
						intersectingIds.add(countryCode);
					}
				}
			}
		}
		Collections.sort(containingIds, this::compareSize);
		Collections.sort(intersectingIds, this::compareSize);

		return new QueryResult(containingIds, intersectingIds);
	}

	private double getSize(String isoCode)
	{
		if (!geometrySizeCache.containsKey(isoCode))
		{
			Geometry country = geometriesByIds.get(isoCode);
			if (country == null) return 0;
			geometrySizeCache.put(isoCode, country.getArea());
		}
		return geometrySizeCache.get(isoCode);
	}

	private int compareSize(String isoCode1, String isoCode2)
	{
		return (int) (getSize(isoCode1) - getSize(isoCode2));
	}

	private Polygon createBounds(double minLong, double minLat, double matLong, double maxLat)
	{
		return factory.createPolygon(new Coordinate[]
				{
						new Coordinate(minLong, minLat),
						new Coordinate(matLong, minLat),
						new Coordinate(matLong, maxLat),
						new Coordinate(minLong, maxLat),
						new Coordinate(minLong, minLat)
				});
	}
}
