package de.westnordost.countryboundaries.geojson;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeoJsonReaderTest
{
	@Test public void readPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Point\",\n" +
				"  \"coordinates\": [1,2]\n" +
				"}");
		assertTrue(g instanceof Point);
		Point p = (Point) g;
		assertEquals(1.0,p.getX(),1e-6);
		assertEquals(2.0,p.getY(),1e-6);
	}

	@Test public void read3DPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Point\",\n" +
				"  \"coordinates\": [1,2,3]\n" +
				"}");
		assertTrue(g instanceof Point);
		Point p = (Point) g;
		assertEquals(1.0,p.getX(),1e-6);
		assertEquals(2.0,p.getY(),1e-6);
		assertEquals(3.0,p.getCoordinate().z,1e-6);
	}

	@Test public void readLineString()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"LineString\",\n" +
				"  \"coordinates\": [[1,2],[2,4]]\n" +
				"}");
		assertTrue(g instanceof LineString);
		LineString l = (LineString) g;
		assertEquals(2,l.getNumPoints());
		assertEquals(1.0,l.getCoordinateN(0).x,1e-6);
		assertEquals(2.0,l.getCoordinateN(0).y,1e-6);
		assertEquals(2.0,l.getCoordinateN(1).x,1e-6);
		assertEquals(4.0,l.getCoordinateN(1).y,1e-6);
	}

	@Test public void readMultiPoint()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPoint\",\n" +
				"  \"coordinates\": [[1,2],[2,4]]\n" +
				"}");
		assertTrue(g instanceof MultiPoint);
		MultiPoint m = (MultiPoint) g;
		assertEquals(2,m.getNumGeometries());
		Point p0 = (Point) m.getGeometryN(0);
		Point p1 = (Point) m.getGeometryN(1);
		assertEquals(1.0,p0.getX(),1e-6);
		assertEquals(2.0,p0.getY(),1e-6);
		assertEquals(2.0,p1.getX(),1e-6);
		assertEquals(4.0,p1.getY(),1e-6);
	}

	@Test public void validateLineString()
	{
		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"LineString\",\n" +
					"  \"coordinates\": [[1,2]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	@Test public void readMultiLineString()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiLineString\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4]],[[1,1],[1,2],[2,1]]]\n" +
				"}");

		assertTrue(g instanceof MultiLineString);
		MultiLineString ml = (MultiLineString) g;
		assertEquals(2,ml.getNumGeometries());
		assertEquals(6,ml.getNumPoints());
	}

	@Test public void readPolygon()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Polygon\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4],[0,0]],[[1,1],[1,2],[2,1],[1,1]]]\n" +
				"}");

		assertTrue(g instanceof Polygon);
		Polygon p = (Polygon) g;
		assertEquals(8,p.getNumPoints());
		assertEquals(1,p.getNumInteriorRing());
	}

	@Test public void readPolygonWithMergableInnerHoles()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Polygon\",\n" +
				"  \"coordinates\": [[[0,0],[4,0],[0,4],[4,4],[0,0]],[[1,1],[1,3],[3,3],[1,1]],[[1,1],[3,1],[3,3],[1,1]]]\n" +
				"}");

		assertTrue(g instanceof Polygon);
		Polygon p = (Polygon) g;
		assertEquals(10,p.getNumPoints());
		assertEquals(1,p.getNumInteriorRing());
	}

	@Test public void validatePolygon()
	{
		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"Polygon\",\n" +
					"  \"coordinates\": [[[0,0],[4,0],[0,0]]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}

		try
		{
			Geometry g = read("{\n" +
					"  \"type\": \"Polygon\",\n" +
					"  \"coordinates\": [[[0,0],[4,0],[0,4],[2,3]]]\n" +
					"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	@Test public void readMultiPolygon()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPolygon\",\n" +
				"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]],[[[5,5],[3,2],[2,3],[5,5]]]]\n" +
				"}");

		assertTrue(g instanceof MultiPolygon);
		MultiPolygon mp = (MultiPolygon) g;
		assertEquals(2,mp.getNumGeometries());
		assertEquals(8,mp.getNumPoints());
	}

	@Test public void multiPolygonMergable()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"MultiPolygon\",\n" +
				"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]], [[[4,0],[4,4],[0,4],[4,0]]], [[[4,0],[4,4],[8,4],[4,0]]]]\n" +
				"}");

		assertTrue(g instanceof MultiPolygon);
		MultiPolygon mp = (MultiPolygon) g;
		assertEquals(1,mp.getNumGeometries());
		assertEquals(6,mp.getNumPoints());
	}

	@Test public void readEmptyGeometryCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"GeometryCollection\",\n" +
				"  \"geometries\": []\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertTrue(g.isEmpty());
	}

	@Test public void readGeometryCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"GeometryCollection\",\n" +
				"  \"geometries\":\n" +
				"  [\n" +
				"    {\n" +
				"      \"type\": \"Point\",\n" +
				"      \"coordinates\": [5,10]\n" +
				"    },\n" +
				"    {\n" +
				"      \"type\": \"LineString\",\n" +
				"      \"coordinates\": [[5,10],[10,5]]\n" +
				"    }\n" +
				"  ]\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertEquals(2,g.getNumGeometries());
		assertTrue(g.getGeometryN(0) instanceof Point);
		assertTrue(g.getGeometryN(1) instanceof LineString);
		assertEquals(3,g.getNumPoints());
	}

	@Test public void readFeatureWithEmptyProperties()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Feature\",\n" +
				"  \"properties\": {},\n" +
				"  \"geometry\": {\n" +
				"    \"type\": \"Point\",\n" +
				"    \"coordinates\": [5,5]\n" +
				"  }\n" +
				"}");
		assertTrue(g instanceof Point);
		assertNull(g.getUserData());
	}

	@Test public void readFeatureWithProperties()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Feature\",\n" +
				"  \"properties\": {\"a\": 3, \"b\": \"blub\", \"c\": null, \"d\": [1,2], \"e\": {\"hi\":\"ho\"}},\n" +
				"  \"geometry\": {\n" +
				"    \"type\": \"Point\",\n" +
				"    \"coordinates\": [5,5]\n" +
				"  }\n" +
				"}");
		assertTrue(g instanceof Point);
		assertTrue(g.getUserData() instanceof Map);
		Map props = (Map) g.getUserData();
		assertEquals(3,props.get("a"));
		assertEquals("blub",props.get("b"));
		assertEquals(null,props.get("c"));
		assertEquals(Arrays.asList(1,2), props.get("d"));
		assertTrue(props.get("e") instanceof Map);
		assertEquals("ho", ((Map) props.get("e")).get("hi"));
	}

	@Test public void readFeatureWithArrayProperties()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"Feature\",\n" +
				"  \"properties\": [1,\"bla\", null, [2], {\"hi\":\"ho\"}],\n" +
				"  \"geometry\": {\n" +
				"    \"type\": \"Point\",\n" +
				"    \"coordinates\": [5,5]\n" +
				"  }\n" +
				"}");
		assertTrue(g instanceof Point);
		assertTrue(g.getUserData() instanceof List);
		List list = (List) g.getUserData();
		assertEquals(5, list.size());
		assertEquals(1,list.get(0));
		assertEquals("bla",list.get(1));
		assertEquals(null,list.get(2));
		assertEquals(Arrays.asList(2),list.get(3));
		assertTrue(list.get(4) instanceof Map);
		assertEquals("ho",((Map)list.get(4)).get("hi"));
	}

	@Test public void readEmptyFeatureCollection()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"FeatureCollection\",\n" +
				"  \"features\": []\n" +
				"}");
		assertTrue(g instanceof GeometryCollection);
		assertTrue(g.isEmpty());
	}

	@Test public void readFeatures()
	{
		Geometry g = read("{\n" +
				"  \"type\": \"FeatureCollection\",\n" +
				"  \"features\": [\n" +
				"      {\n" +
				"      \"type\": \"Feature\",\n" +
				"      \"properties\": {\"a\":\"b\"},\n" +
				"      \"geometry\": {\n" +
				"        \"type\": \"Point\",\n" +
				"        \"coordinates\": [10,20]\n" +
				"      }\n" +
				"    },\n" +
				"    {\n" +
				"      \"type\": \"Feature\",\n" +
				"      \"properties\": {\"c\":\"d\"},\n" +
				"      \"geometry\": {\n" +
				"        \"type\": \"LineString\",\n" +
				"        \"coordinates\": [[20,10],[30,30]]\n" +
				"      }\n" +
				"    }\n" +
				"  ]\n" +
				"}");

		assertTrue(g instanceof GeometryCollection);
		assertEquals(2,g.getNumGeometries());
		assertTrue(g.getGeometryN(0) instanceof Point);
		assertTrue(g.getGeometryN(1) instanceof LineString);
		assertEquals(3,g.getNumPoints());
		assertEquals("b",((Map)g.getGeometryN(0).getUserData()).get("a"));
		assertEquals("d",((Map)g.getGeometryN(1).getUserData()).get("c"));
	}

	private static Geometry read(String s)
	{
		return new GeoJsonReader().read(s);
	}
}
