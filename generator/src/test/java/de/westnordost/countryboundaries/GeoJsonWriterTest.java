package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GeoJsonWriterTest
{
	private static final int WGS84 = 4326;
	private GeometryFactory factory;

	@Before public void setUp()
	{
		factory = new GeometryFactory(new PrecisionModel(), WGS84);
	}

	@Test public void writePoint()
	{
		Point g = factory.createPoint(p(1,2));
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]}" +
				"}", write(g));
	}

	@Test public void write3DPoint()
	{
		Point g = factory.createPoint(new Coordinate(1,2,3));
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2,3]}" +
				"}", write(g));
	}

	@Test public void writeLineString()
	{
		LineString g = factory.createLineString(new Coordinate[]{p(1,2), p(2,4)});
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[1,2],[2,4]]}" +
				"}", write(g));
	}

	@Test public void writeMultiPoint()
	{
		MultiPoint g = factory.createMultiPoint(new Coordinate[]{p(1,2), p(2,4)});
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[1,2],[2,4]]}" +
				"}", write(g));
	}

	@Test public void writeMultiLineString()
	{
		MultiLineString g = factory.createMultiLineString(new LineString[]{
				factory.createLineString(new Coordinate[]{p(0,0), p(4,0), p(0,4)}),
				factory.createLineString(new Coordinate[]{p(1,1), p(1,2), p(2,1)}),
		});
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"MultiLineString\"," +
				"\"coordinates\":[[[0,0],[4,0],[0,4]],[[1,1],[1,2],[2,1]]]}" +
				"}", write(g));
	}

	@Test public void writePolygon()
	{
		Polygon g = factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(0,0), p(0,4), p(4,0), p(0,0)}),
				new LinearRing[]{factory.createLinearRing(new Coordinate[] {p(1,1), p(2,1), p(1,2), p(1,1)})}
		);
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"Polygon\"," +
				"\"coordinates\":[[[0,0],[4,0],[0,4],[0,0]],[[1,1],[1,2],[2,1],[1,1]]]}" +
				"}", write(g));
	}

	@Test public void writeMultiPolygon()
	{
		MultiPolygon g = factory.createMultiPolygon(new Polygon[]{
				factory.createPolygon(new Coordinate[]{p(0,0),p(0,4),p(4,0),p(0,0)}),
				factory.createPolygon(new Coordinate[]{p(0,0),p(0,1),p(1,0),p(0,0)})
		});
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"MultiPolygon\"," +
				"\"coordinates\":[[[[0,0],[4,0],[0,4],[0,0]]],[[[0,0],[1,0],[0,1],[0,0]]]]}" +
				"}", write(g));
	}

	@Test public void writeMultiPolygonWithPolygonWithHole()
	{
		MultiPolygon g = factory.createMultiPolygon(new Polygon[]{
				factory.createPolygon(
						factory.createLinearRing(new Coordinate[]{p(0,0),p(0,4),p(4,0),p(0,0)}),
						new LinearRing[]{
								factory.createLinearRing(new Coordinate[]{p(1,1),p(3,1),p(1,3),p(1,1)})
						}
				),
				factory.createPolygon(new Coordinate[]{p(8,8),p(8,9),p(9,8),p(8,8)})
		});
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"MultiPolygon\"," +
				"\"coordinates\":[[[[0,0],[4,0],[0,4],[0,0]],[[1,1],[1,3],[3,1],[1,1]]],[[[8,8],[9,8],[8,9],[8,8]]]]}" +
				"}", write(g));
	}

	@Test public void writeEmptyFeatureCollection()
	{
		GeometryCollection g = factory.createGeometryCollection(new Geometry[]{});
		assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", write(g));
	}

	@Test public void writeFeatureCollection()
	{
		GeometryCollection g = factory.createGeometryCollection(new Geometry[]{
			factory.createPoint(p(5,10)),
			factory.createLineString(new Coordinate[]{p(5,10),p(10,5)})
		});
		assertEquals("{\"type\":\"FeatureCollection\",\"features\":[" +
				"{\"type\":\"Feature\",\"properties\":{},\"geometry\":" +
					"{\"type\":\"Point\",\"coordinates\":[5,10]}}," +
				"{\"type\":\"Feature\",\"properties\":{},\"geometry\":" +
					"{\"type\":\"LineString\",\"coordinates\":[[5,10],[10,5]]}}" +
				"]}", write(g));
	}

	@Test public void writeFeatureWithMapProperties()
	{
		Point g = factory.createPoint(p(5,5));
		Map<String,Object> props = new HashMap<>();
		props.put("a",3);
		props.put("b","blub");
		props.put("c",null);
		props.put("d",Arrays.asList(1,2));
		props.put("e",Collections.singletonMap("hi","ho"));
		g.setUserData(props);
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{" +
				"\"a\":3,\"b\":\"blub\",\"c\":null,\"d\":[1,2],\"e\":{\"hi\":\"ho\"}" +
				"}," +
				"\"geometry\":{\"type\":\"Point\",\"coordinates\":[5,5]}" +
				"}", write(g));
	}

	@Test public void writeFeatureWithInvalidProperties()
	{
		Point g = factory.createPoint(p(5,5));
		ArrayList<Object> list = new ArrayList<>();
		list.add(1);
		g.setUserData(list);
		assertEquals("{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"Point\",\"coordinates\":[5,5]}" +
				"}", write(g));
	}

	@Test public void writeFeatureCollectionWithEmptyGeometryCollection()
	{
		GeometryCollection g = factory.createGeometryCollection(new Geometry[]{
				factory.createGeometryCollection(new Geometry[]{})
		});
		assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[]}" +
				"}]}", write(g));
	}

	@Test public void writeFeatureCollectionWithGeometryCollection()
	{
		GeometryCollection g = factory.createGeometryCollection(new Geometry[]{
				factory.createGeometryCollection(new Geometry[]{
						factory.createPoint(p(0,0)),
						factory.createPoint(p(1,1))
				})
		});
		assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{" +
				"\"type\":\"Feature\",\"properties\":{}," +
				"\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[" +
				"{\"type\":\"Point\",\"coordinates\":[0,0]}," +
				"{\"type\":\"Point\",\"coordinates\":[1,1]}" +
				"]}" +
				"}]}", write(g));
	}

	private static String write(Geometry g)
	{
		return new GeoJsonWriter().write(g);
	}

	private static Coordinate p(int x, int y)
	{
		return new Coordinate(x,y);
	}
}
