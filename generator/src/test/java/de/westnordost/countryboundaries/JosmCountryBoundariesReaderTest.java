package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.*;

public class JosmCountryBoundariesReaderTest
{
	private static final int WGS84 = 4326;
	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	@Test public void ignoresPolygonsWithoutIso3166Tag()
	{
		GeometryCollection g = parse(simpleClosedWay(""));
		assertTrue(g.isEmpty());
	}

	@Test public void readIso3166Tag()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-1:alpha2' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		Object userData = g.getGeometryN(0).getUserData();
		assertTrue(userData instanceof Map);
		assertEquals("ABC",((Map)userData).get("id"));
	}

	@Test public void readIso3166_2Tag()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-2' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		Object userData = g.getGeometryN(0).getUserData();
		assertTrue(userData instanceof Map);
		assertEquals("ABC",((Map)userData).get("id"));
	}

	@Test public void Iso3166_1_winsOverIso3166_2Tag()
	{
		GeometryCollection g = parse(simpleClosedWay(
				"<tag k='ISO3166-2' v='ABC'/><tag k='ISO3166-1:alpha2' v='DEF'/>"));
		assertEquals(1,g.getNumGeometries());
		Object userData = g.getGeometryN(0).getUserData();
		assertTrue(userData instanceof Map);
		assertEquals("DEF",((Map)userData).get("id"));
	}

	@Test public void readSimplePolygon()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-2' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(new Coordinate[] {
				p(0,0), p(1,1), p(1,0), p(0,0)});
		assertEquals(expected, actual);
	}

	@Test public void readSimplePolygonReversed()
	{
		GeometryCollection g = parse(
				"<node id='0' lon='0' lat='0' />"+
				"<node id='1' lon='1' lat='1' />"+
				"<node id='2' lon='1' lat='0' />"+
				"<way id='0'>"+
				"  <nd ref='0'/>"+
				"  <nd ref='2'/>"+
				"  <nd ref='1'/>"+
				"  <nd ref='0'/>"+
				"<tag k='ISO3166-2' v='ABC'/>"+
				"</way>");
		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(new Coordinate[] {
				p(0,0), p(1,1), p(1,0), p(0,0)});
		assertEquals(expected, actual);
	}

	@Test public void readPolygonWithHoles()
	{
		GeometryCollection g = parse(
				"<node id='0' lon='0' lat='0' />"+
				"<node id='1' lon='0' lat='10' />"+
				"<node id='2' lon='10' lat='10' />"+
				"<node id='3' lon='10' lat='0' />"+
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='3' lat='4' />"+
				"<node id='8' lon='5' lat='2' />"+
				"<node id='9' lon='5' lat='4' />"+
				"<way id='0'><nd ref='0'/><nd ref='1'/><nd ref='2'/><nd ref='3'/><nd ref='0'/></way>"+
				"<way id='1'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='2'><nd ref='7'/><nd ref='8'/><nd ref='9'/><nd ref='7'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='0' role='outer'/>" +
				"  <member type='way' ref='1' role='inner'/>" +
				"  <member type='way' ref='2' role='inner'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(0,0),p(0,10),p(10,10),p(10,0),p(0,0)}),
				new LinearRing[]{
						factory.createLinearRing(new Coordinate[]{p(2,2),p(4,2),p(2,4),p(2,2)}),
						factory.createLinearRing(new Coordinate[]{p(3,4),p(5,2),p(5,4),p(3,4)})
				});
		assertEquals(expected, actual);
	}

	@Test public void readPolygonWithMergableHoles()
	{
		GeometryCollection g = parse(
				"<node id='0' lon='0' lat='0' />"+
				"<node id='1' lon='0' lat='10' />"+
				"<node id='2' lon='10' lat='10' />"+
				"<node id='3' lon='10' lat='0' />"+
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='4' lat='4' />"+
				"<way id='0'><nd ref='0'/><nd ref='1'/><nd ref='2'/><nd ref='3'/><nd ref='0'/></way>"+
				"<way id='1'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='2'><nd ref='5'/><nd ref='6'/><nd ref='7'/><nd ref='5'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='0' role='outer'/>" +
				"  <member type='way' ref='1' role='inner'/>" +
				"  <member type='way' ref='2' role='inner'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(0,0),p(0,10),p(10,10),p(10,0),p(0,0)}),
				new LinearRing[]{
						factory.createLinearRing(new Coordinate[]{p(2,2),p(4,2),p(4,4),p(2,4),p(2,2)})
				});
		assertEquals(expected, actual);
	}

	@Test public void readSimpleMultipolygon()
	{
		GeometryCollection g = parse(
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='3' lat='4' />"+
				"<node id='8' lon='5' lat='2' />"+
				"<node id='9' lon='5' lat='4' />"+
				"<way id='1'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='2'><nd ref='7'/><nd ref='8'/><nd ref='9'/><nd ref='7'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='1' role='outer'/>" +
				"  <member type='way' ref='2' role='outer'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		MultiPolygon expected = factory.createMultiPolygon(new Polygon[]{
				factory.createPolygon(new Coordinate[]{p(2,2),p(2,4),p(4,2),p(2,2)}),
				factory.createPolygon(new Coordinate[]{p(3,4),p(5,4),p(5,2),p(3,4)})
		});
		assertEquals(expected, actual);
	}

	@Test public void readMergeableMultipolygon()
	{
		GeometryCollection g = parse(
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='4' lat='4' />"+
				"<way id='1'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='2'><nd ref='5'/><nd ref='6'/><nd ref='7'/><nd ref='5'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='1' role='outer'/>" +
				"  <member type='way' ref='2' role='outer'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(new Coordinate[]{p(2,2),p(2,4),p(4,4),p(4,2),p(2,2)});
		assertEquals(expected, actual);
	}

	@Test public void readMultiPolygonWithPolygonsWithHoles()
	{
		GeometryCollection g = parse(
				"<node id='-1' lon='1' lat='7' />"+
				"<node id='-2' lon='8' lat='0' />"+
				"<node id='-3' lon='8' lat='7' />"+
				"<node id='0' lon='1' lat='0' />"+
				"<node id='1' lon='1' lat='6' />"+
				"<node id='2' lon='7' lat='0' />"+
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='5' lat='4' />"+
				"<node id='8' lon='7' lat='2' />"+
				"<node id='9' lon='7' lat='4' />"+
				"<way id='0'><nd ref='-1'/><nd ref='-2'/><nd ref='-3'/><nd ref='-1'/></way>"+
				"<way id='1'><nd ref='0'/><nd ref='1'/><nd ref='2'/><nd ref='0'/></way>"+
				"<way id='2'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='3'><nd ref='7'/><nd ref='8'/><nd ref='9'/><nd ref='7'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='0' role='outer'/>" +
				"  <member type='way' ref='1' role='outer'/>" +
				"  <member type='way' ref='2' role='inner'/>" +
				"  <member type='way' ref='3' role='inner'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		MultiPolygon expected = factory.createMultiPolygon(new Polygon[]{
				factory.createPolygon(
						factory.createLinearRing(new Coordinate[]{p(1,0),p(1,6),p(7,0),p(1,0)}),
						new LinearRing[]{factory.createLinearRing(new Coordinate[]{p(2,2),p(4,2),p(2,4),p(2,2)})}
				),
				factory.createPolygon(
						factory.createLinearRing(new Coordinate[]{p(1,7),p(8,7),p(8,0),p(1,7)}),
						new LinearRing[]{factory.createLinearRing(new Coordinate[]{p(5,4),p(7,2),p(7,4),p(5,4)})}
				),
		});
		assertEquals(expected, actual);
	}

	@Test public void readMergableMultiPolygonWithPolygonsWithHoles()
	{
		GeometryCollection g = parse(
				"<node id='0' lon='1' lat='0' />"+
				"<node id='1' lon='1' lat='6' />"+
				"<node id='2' lon='7' lat='0' />"+
				"<node id='3' lon='7' lat='6' />"+
				"<node id='4' lon='2' lat='2' />"+
				"<node id='5' lon='4' lat='2' />"+
				"<node id='6' lon='2' lat='4' />"+
				"<node id='7' lon='4' lat='4' />"+
				"<node id='8' lon='6' lat='2' />"+
				"<node id='9' lon='6' lat='4' />"+
				"<way id='0'><nd ref='0'/><nd ref='1'/><nd ref='2'/><nd ref='0'/></way>"+
				"<way id='1'><nd ref='1'/><nd ref='2'/><nd ref='3'/><nd ref='1'/></way>"+
				"<way id='2'><nd ref='4'/><nd ref='5'/><nd ref='6'/><nd ref='4'/></way>"+
				"<way id='3'><nd ref='7'/><nd ref='8'/><nd ref='9'/><nd ref='7'/></way>"+
				"<relation id='0'>" +
				"  <member type='way' ref='0' role='outer'/>" +
				"  <member type='way' ref='1' role='outer'/>" +
				"  <member type='way' ref='2' role='inner'/>" +
				"  <member type='way' ref='3' role='inner'/>" +
				"  <tag k='ISO3166-2' v='ABC'/>"+
				"</relation>");

		assertEquals(1,g.getNumGeometries());
		Geometry actual = g.getGeometryN(0);
		Polygon expected = factory.createPolygon(
				factory.createLinearRing(new Coordinate[]{p(1,0),p(1,6),p(7,6),p(7,0),p(1,0)}),
				new LinearRing[]{
						factory.createLinearRing(new Coordinate[]{p(2,2),p(4,2),p(2,4),p(2,2)}),
						factory.createLinearRing(new Coordinate[]{p(4,4),p(6,2),p(6,4),p(4,4)})
				});
		assertEquals(expected, actual);
	}

	private static String simpleClosedWay(String withTags)
	{
		return "<node id='0' lon='0' lat='0' />"+
				"<node id='1' lon='1' lat='1' />"+
				"<node id='2' lon='1' lat='0' />"+
				"<way id='0'>"+
				"  <nd ref='0'/>"+
				"  <nd ref='1'/>"+
				"  <nd ref='2'/>"+
				"  <nd ref='0'/>"+
				withTags +
				"</way>";
	}

	private static GeometryCollection parse(String xml)
	{
		return new JosmCountryBoundariesReader().read(new StringReader(xml));
	}

	private static Coordinate p(double x, double y) { return new Coordinate(x,y); }

}