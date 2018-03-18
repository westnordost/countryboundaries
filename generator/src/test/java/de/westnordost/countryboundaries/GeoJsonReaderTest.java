package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class GeoJsonReaderTest
{
	private static final int WGS84 = 4326;
	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	@Test public void readPoint()
	{
		assertEquals(
			factory.createPoint(p(1,2)),
			read("{" +
			"  \"type\": \"Point\"," +
			"  \"coordinates\": [1,2]" +
			"}")
		);
	}

	@Test public void read3DPoint()
	{
		assertEquals(
			factory.createPoint(new Coordinate(1,2,3)),
			read("{" +
			"  \"type\": \"Point\"," +
			"  \"coordinates\": [1,2,3]" +
			"}")
		);
	}

	@Test public void readLineString()
	{
		assertEquals(
			factory.createLineString(new Coordinate[]{p(1,2), p(2,4)}),
			read("{" +
			"  \"type\": \"LineString\"," +
			"  \"coordinates\": [[1,2],[2,4]]" +
			"}")
		);
	}

	@Test public void readMultiPoint()
	{
		assertEquals(
			factory.createMultiPoint(new Coordinate[]{p(1,2), p(2,4)}),
			read("{" +
			"  \"type\": \"MultiPoint\"," +
			"  \"coordinates\": [[1,2],[2,4]]" +
			"}")
		);
	}

	@Test public void validateLineString()
	{
		try
		{
			read("{" +
			"  \"type\": \"LineString\"," +
			"  \"coordinates\": [[1,2]]" +
			"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	@Test public void readMultiLineString()
	{
		assertEquals(
			factory.createMultiLineString(new LineString[]{
				factory.createLineString(new Coordinate[]{p(0,0), p(4,0), p(0,4)}),
				factory.createLineString(new Coordinate[]{p(1,1), p(1,2), p(2,1)})}
			),
			read("{" +
			"  \"type\": \"MultiLineString\"," +
			"  \"coordinates\": [[[0,0],[4,0],[0,4]],[[1,1],[1,2],[2,1]]]" +
			"}")
		);
	}

	@Test public void readPolygon()
	{
		assertEquals(
			factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(0,0), p(0,4), p(4,0), p(0,0)}),
				new LinearRing[]{factory.createLinearRing(new Coordinate[]{p(1,1), p(2,1), p(1,2),p(1,1)})
			}),
			read("{" +
			"  \"type\": \"Polygon\"," +
			"  \"coordinates\": [[[0,0],[4,0],[0,4],[0,0]],[[1,1],[1,2],[2,1],[1,1]]]" +
			"}")
		);
	}

	@Test public void validatePolygon()
	{
		try
		{
			read("{" +
			"  \"type\": \"Polygon\"," +
			"  \"coordinates\": [[[0,0],[4,0],[0,0]]]" +
			"}");
			fail();
		}
		catch (GeoJsonException e) {}

		try
		{
			read("{" +
			"  \"type\": \"Polygon\"," +
			"  \"coordinates\": [[[0,0],[4,0],[0,4],[2,3]]]" +
			"}");
			fail();
		}
		catch (GeoJsonException e) {}
	}

	@Test public void readMultiPolygon()
	{
		Geometry expected = factory.createMultiPolygon(new Polygon[]{
				factory.createPolygon(new Coordinate[]{p(0,0),p(0,4),p(4,0),p(0,0)}),
				factory.createPolygon(new Coordinate[]{p(5,5),p(2,3),p(3,2),p(5,5)})
		});
		expected.normalize();
		assertEquals(expected,
			read("{" +
			"  \"type\": \"MultiPolygon\"," +
			"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]],[[[5,5],[3,2],[2,3],[5,5]]]]" +
			"}")
		);
	}

	@Test public void readEmptyGeometryCollection()
	{
		assertEquals(
			factory.createGeometryCollection(new Geometry[]{}),
			read("{" +
			"  \"type\": \"GeometryCollection\"," +
			"  \"geometries\": []" +
			"}")
		);
	}

	@Test public void readGeometryCollection()
	{
		assertEquals(
			factory.createGeometryCollection(new Geometry[]{
				factory.createPoint(p(5,10)),
				factory.createLineString(new Coordinate[]{p(5,10),p(10,5)})
			}),
			read("{" +
			"  \"type\": \"GeometryCollection\"," +
			"  \"geometries\":" +
			"  [" +
			"    {" +
			"      \"type\": \"Point\"," +
			"      \"coordinates\": [5,10]" +
			"    }," +
			"    {" +
			"      \"type\": \"LineString\"," +
			"      \"coordinates\": [[5,10],[10,5]]" +
			"    }" +
			"  ]" +
			"}")
		);
	}

	@Test public void readFeatureWithEmptyProperties()
	{
		assertNull(
			read("{" +
			"  \"type\": \"Feature\"," +
			"  \"properties\": {}," +
			"  \"geometry\": {" +
			"    \"type\": \"Point\"," +
			"    \"coordinates\": [5,5]" +
			"  }" +
			"}").getUserData());
	}

	@Test public void readFeatureWithProperties()
	{
		Geometry g = read("{" +
				"  \"type\": \"Feature\"," +
				"  \"properties\": {\"a\": 3, \"b\": \"blub\", \"c\": null, \"d\": [1,2], \"e\": {\"hi\":\"ho\"}}," +
				"  \"geometry\": {" +
				"    \"type\": \"Point\"," +
				"    \"coordinates\": [5,5]" +
				"  }" +
				"}");
		assertTrue(g.getUserData() instanceof Map);
		Map props = (Map) g.getUserData();
		assertEquals(3,props.get("a"));
		assertEquals("blub",props.get("b"));
		assertEquals(null,props.get("c"));
		assertEquals(Arrays.asList(1,2), props.get("d"));
		assertTrue(props.get("e") instanceof Map);
		assertEquals("ho", ((Map) props.get("e")).get("hi"));
	}

	@Test public void readFeatureWithInvalidProperties()
	{
		try
		{
			read("{" +
			"  \"type\": \"Feature\"," +
			"  \"properties\": [1,2,3]," +
			"  \"geometry\": {" +
			"    \"type\": \"Point\"," +
			"    \"coordinates\": [5,5]" +
			"  }" +
			"}");

			fail();
		} catch (GeoJsonException e) {}
	}

	@Test public void readEmptyFeatureCollection()
	{
		assertEquals(
			factory.createGeometryCollection(new Geometry[]{}),
			read("{" +
			"  \"type\": \"FeatureCollection\"," +
			"  \"features\": []" +
			"}")
		);
	}

	@Test public void readFeatures()
	{
		Point point = factory.createPoint(p(10,20));
		point.setUserData(Collections.singletonMap("a","b"));

		LineString str = factory.createLineString(new Coordinate[]{p(20,10),p(30,30)});
		str.setUserData(Collections.singletonMap("c","d"));

		assertEquals(
			factory.createGeometryCollection(new Geometry[]{ point, str }),
			read("{" +
			"  \"type\": \"FeatureCollection\"," +
			"  \"features\": [" +
			"      {" +
			"      \"type\": \"Feature\"," +
			"      \"properties\": {\"a\":\"b\"}," +
			"      \"geometry\": {" +
			"        \"type\": \"Point\"," +
			"        \"coordinates\": [10,20]" +
			"      }" +
			"    }," +
			"    {" +
			"      \"type\": \"Feature\"," +
			"      \"properties\": {\"c\":\"d\"}," +
			"      \"geometry\": {" +
			"        \"type\": \"LineString\"," +
			"        \"coordinates\": [[20,10],[30,30]]" +
			"      }" +
			"    }" +
			"  ]" +
			"}")
		);
	}

	@Test public void readPolygonWithMergableInnerHoles()
	{
		assertEquals(
			factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(0,0),p(0,4),p(4,4),p(4,0),p(0,0)}),
				new LinearRing[]{
						factory.createLinearRing(new Coordinate[]{p(1,1),p(3,1),p(3,3),p(1,3),p(1,1)})
				}
			),
			new GeoJsonReader(true).read("{" +
			"  \"type\": \"Polygon\"," +
			"  \"coordinates\": [[[0,0],[4,0],[4,4],[0,4],[0,0]],[[1,1],[1,3],[3,3],[1,1]],[[1,1],[3,1],[3,3],[1,1]]]" +
			"}")
		);
	}

	@Test public void multiPolygonMergable()
	{
		assertEquals(
			factory.createPolygon(new Coordinate[]{p(0,0),p(0,4),p(4,4),p(8,4),p(4,0),p(0,0)}),
			new GeoJsonReader(true).read("{" +
			"  \"type\": \"MultiPolygon\"," +
			"  \"coordinates\": [[[[0,0],[4,0],[0,4],[0,0]]], [[[4,0],[4,4],[0,4],[4,0]]], [[[4,0],[4,4],[8,4],[4,0]]]]" +
			"}")
		);
	}

	private static Geometry read(String s)
	{
		return new GeoJsonReader().read(s);
	}

	private static Coordinate p(double x, double y) { return new Coordinate(x,y); }
}
