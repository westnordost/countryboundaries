package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// TODO test
public class JosmCountryBoundariesParser
{
	// https://josm.openstreetmap.de/export/HEAD/josm/trunk/data/boundaries.osm

	private static final String ISO3166_1_ALPHA2 = "ISO3166-1:alpha2";
	private static final String ISO3166_2 = "ISO3166-2";

	private static final int WGS84 = 4326;
	private final GeometryFactory factory =
			new ConvertToOgcSfsCompliantGeometryFactory(new PrecisionModel(), WGS84);

	public GeometryCollection read(Reader reader) throws OsmXmlException
	{
		try
		{
			OsmMapData osm = readXml(reader);
			List<Poly> polys = convertToPolys(osm);
			return createGeometry(polys);
		}
		catch (XmlPullParserException | IOException e)
		{
			throw new OsmXmlException(e);
		}
	}

	private OsmMapData readXml(Reader reader) throws XmlPullParserException, IOException{

		OsmMapData result = new OsmMapData();

		XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
		xpp.setInput( reader );
		int eventType = xpp.getEventType();

		Way way = null;
		Relation relation = null;
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if(eventType == XmlPullParser.START_TAG)
			{
				String name = xpp.getName();
				if ("node".equals(name))
				{
					Coordinate coordinate = new Coordinate(
							Double.valueOf(xpp.getAttributeValue(null, "lon")),
							Double.valueOf(xpp.getAttributeValue(null, "lat")));
					result.nodes.put(Long.valueOf(xpp.getAttributeValue(null, "id")), coordinate);
				}
				else if("way".equals(name))
				{
					way = new Way();
					result.ways.put(Long.valueOf(xpp.getAttributeValue(null, "id")), way);
				}
				else if("nd".equals(name) && way != null)
				{
					way.nodes.add(Long.valueOf(xpp.getAttributeValue(null, "ref")));
				}
				else if("relation".equals(name))
				{
					relation = new Relation();
					result.relations.put(Long.valueOf(xpp.getAttributeValue(null, "id")), relation);
				}
				else if("member".equals(name) && relation != null)
				{
					if("relation".equals(xpp.getAttributeValue(null, "type")))
					{
						throw new UnsupportedOperationException("Parsing relations as relation members is not supported!");
					}
					else if("way".equals("type"))
					{
						long ref = Long.valueOf(xpp.getAttributeValue(null, "ref"));
						String role = xpp.getAttributeValue(null, "role");
						if ("outer".equals(role))      relation.outer.add(ref);
						else if ("inner".equals(role)) relation.inner.add(ref);
					}
				}
				else if("tag".equals(name) && (way != null || relation != null))
				{
					Named ele = way != null ? way : relation;
					String key = xpp.getAttributeValue(null, "k");
					if(ISO3166_1_ALPHA2.equals(key) || ele.name == null && ISO3166_2.equals(key))
					{
						ele.name = xpp.getAttributeValue(null, "v");
					}
				}
			}
			else if(eventType == XmlPullParser.END_TAG)
			{
				String name = xpp.getName();
				if("way".equals(name))
				{
					way = null;
				}
				else if("relation".equals(name))
				{
					relation = null;
				}
			}
			eventType = xpp.next();
		}
		return result;
	}

	private List<Poly> convertToPolys(OsmMapData osm)
	{
		List<Poly> result = new ArrayList<>();

		for (Map.Entry<Long, Way> entry : osm.ways.entrySet())
		{
			Way way = entry.getValue();
			if(way.name == null) continue;

			Poly polygon = new Poly();
			polygon.name = way.name;
			polygon.outer.add(factory.createLinearRing(osm.getCoordinates(entry.getKey())));
			result.add(polygon);
		}
		for (Relation relation : osm.relations.values())
		{
			if(relation.name == null) continue;

			Poly polygon = new Poly();
			polygon.name = relation.name;
			for (Long wayId : relation.outer)
			{
				polygon.outer.add(factory.createLinearRing(osm.getCoordinates(wayId)));
			}
			for (Long wayId : relation.inner)
			{
				polygon.inner.add(factory.createLinearRing(osm.getCoordinates(wayId)));
			}
		}
		return result;
	}

	private GeometryCollection createGeometry(List<Poly> polys)
	{
		List<Geometry> geometries = new ArrayList<>();
		for (Poly poly : polys)
		{
			Geometry g;
			// polygons
			if(poly.outer.size() == 1)
			{
				LinearRing shell = poly.outer.get(0);
				LinearRing[] holes = poly.inner.toArray(new LinearRing[]{});
				g = factory.createPolygon(shell, holes);
			}
			// multipolygons
			else
			{
				// in the OSM XML, it is not sorted which inner linear ring belongs to which outer
				Polygon[] polygons = new Polygon[poly.outer.size()];
				for (int i=0; i<polygons.length; ++i)
				{
					LinearRing shell = poly.outer.get(i);
					Polygon tempOuter = factory.createPolygon(shell);
					ArrayList<LinearRing> holes = new ArrayList<>();

					Iterator<LinearRing> holesIt = poly.inner.iterator();
					while(holesIt.hasNext())
					{
						LinearRing hole = holesIt.next();
						Point p = factory.createPoint(hole.getCoordinateN(0));
						if(tempOuter.contains(p))
						{
							holes.add(hole);
							holesIt.remove();
						}
					}

					polygons[i] = factory.createPolygon(shell, holes.toArray(new LinearRing[]{}));
				}

				g = factory.createMultiPolygon(polygons);
			}
			g.normalize();
			g.setUserData(poly.name);
			geometries.add(g);
		}

		return factory.createGeometryCollection(geometries.toArray(new Geometry[]{}));
	}

	private static class OsmMapData
	{
		Map<Long, Coordinate> nodes = new HashMap<>();
		Map<Long, Way> ways = new HashMap<>();
		Map<Long, Relation> relations = new HashMap<>();

		Coordinate[] getCoordinates(long wayId)
		{
			Way way = ways.get(wayId);
			Coordinate[] coords = new Coordinate[way.nodes.size()];
			for (int i = 0; i < way.nodes.size(); i++)
			{
				Long node = way.nodes.get(i);
				coords[i] = nodes.get(node);
			}
			return coords;
		}
	}

	private static abstract class Named	{ String name; }

	private static class Way extends Named
	{
		List<Long> nodes = new ArrayList<>();
	}

	private static class Relation extends Named
	{
		List<Long> outer = new ArrayList<>();
		List<Long> inner = new ArrayList<>();
	}

	private static class Poly extends Named
	{
		List<LinearRing> outer = new ArrayList<>();
		List<LinearRing> inner = new ArrayList<>();
	}
}
