package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryBoundaries
{
	private static String ISO3166_1_ALPHA2 = "ISO3166-1:alpha2";
	private static String ISO3166_2 = "ISO3166-2";
	private static final int WGS84 = 4326;

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);
	private final Map<String, Geometry> geometriesByIsoCodes;
	private final CountryBoundariesIndex index;
	private final Map<String, Double> geometrySizeCache;

	public CountryBoundaries(GeometryCollection countriesBoundaries, CountryBoundariesIndex index)
	{
		this.index = index;
		geometrySizeCache = new HashMap<>(400);
		geometriesByIsoCodes = new HashMap<>(400);

		for (int i = 0; i < countriesBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countriesBoundaries.getGeometryN(i);

			Map<String, String> props = (Map<String, String>) countryBoundary.getUserData();
			if (props == null) continue;
			if (props.containsKey(ISO3166_1_ALPHA2) || props.containsKey(ISO3166_2))
			{
				insertIntoIsoCodes(countryBoundary);
			}
		}
	}

	private void insertIntoIsoCodes(Geometry countryBoundary)
	{
		Map<String, String> props = (Map<String, String>) countryBoundary.getUserData();
		String iso3166_2 = props.get(ISO3166_2);
		String iso3166_1_alpha2 = props.get(ISO3166_1_ALPHA2);

		if (iso3166_1_alpha2 != null)
		{
			geometriesByIsoCodes.put(iso3166_1_alpha2, countryBoundary);
		}
		if (iso3166_2 != null)
		{
			geometriesByIsoCodes.put(iso3166_2, countryBoundary);
		}
	}

	public List<String> getIsoCodes(double longitude, double latitude)
	{
		CountryQueryResult queryResult = index.query(longitude, latitude);

		List<String> result = new ArrayList<>();
		result.addAll(queryResult.getContainingCountryCodes());

		Collection<String> possibleMatches = queryResult.getIntersectingCountryCodes();
		if (!possibleMatches.isEmpty())
		{
			Coordinate coord = new Coordinate(longitude, latitude, 0);
			Point point = factory.createPoint(coord);

			for (String countryCode : possibleMatches)
			{
				Geometry country = geometriesByIsoCodes.get(countryCode);
				if (country != null && country.covers(point))
				{
					result.add(countryCode);
				}
			}
		}
		Collections.sort(result, this::compareSize);
		return result;
	}

	public CountryQueryResult getIsoCodes(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude)
	{
		CountryQueryResult queryResult = index.query(minLongitude, minLatitude, maxLongitude, maxLatitude);

		List<String> containingCountryCodes = new ArrayList<>();
		List<String> intersectingCountryCodes = new ArrayList<>();
		containingCountryCodes.addAll(queryResult.getContainingCountryCodes());

		Collection<String> possibleMatches = queryResult.getIntersectingCountryCodes();
		if (!possibleMatches.isEmpty())
		{
			Polygon box = createBounds(minLongitude, minLatitude, maxLongitude, maxLatitude);
			for (String countryCode : possibleMatches)
			{
				Geometry country = geometriesByIsoCodes.get(countryCode);
				if (country != null)
				{
					IntersectionMatrix im = country.relate(box);
					if (im.isCovers())
					{
						containingCountryCodes.add(countryCode);
					} else if (!im.isDisjoint())
					{
						intersectingCountryCodes.add(countryCode);
					}
				}
			}
		}
		Collections.sort(containingCountryCodes, this::compareSize);
		Collections.sort(intersectingCountryCodes, this::compareSize);

		return new CountryQueryResult(containingCountryCodes, intersectingCountryCodes);
	}

	private double getSize(String isoCode)
	{
		if (!geometrySizeCache.containsKey(isoCode))
		{
			Geometry country = geometriesByIsoCodes.get(isoCode);
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
