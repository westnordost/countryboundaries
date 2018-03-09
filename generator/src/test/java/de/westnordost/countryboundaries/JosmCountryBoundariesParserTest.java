package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Test;

import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.*;

public class JosmCountryBoundariesParserTest
{
	@Test public void ignoresPolygonsWithoutIso3166Tag()
	{
		GeometryCollection g = parse(simpleClosedWay(""));
		assertTrue(g.isEmpty());
	}

	@Test public void readIso3166Tag()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-1:alpha' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		assertEquals("ABC",g.getGeometryN(0).getUserData());
	}

	@Test public void readIso3166_2Tag()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-2' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		assertEquals("ABC",g.getGeometryN(0).getUserData());
	}

	@Test public void Iso3166_1_winsOverIso3166_2Tag()
	{
		GeometryCollection g = parse(simpleClosedWay(
				"<tag k='ISO3166-2' v='ABC'/><tag k='ISO3166-1:alpha' v='DEF'/>"));
		assertEquals(1,g.getNumGeometries());
		assertEquals("DEF",g.getGeometryN(0).getUserData());
	}

	@Test public void readSimplePolygon()
	{
		GeometryCollection g = parse(simpleClosedWay("<tag k='ISO3166-2' v='ABC'/>"));
		assertEquals(1,g.getNumGeometries());
		Geometry gn = g.getGeometryN(0);
		assertTrue(gn instanceof Polygon);
		Polygon p = (Polygon) gn;
		Coordinate[] coords = p.getCoordinates();
		assertArrayEquals(new Coordinate[] {
				new Coordinate(0,0), new Coordinate(5,5), new Coordinate(5,0), new Coordinate(0,5)},
				coords);
	}

	// TODO continue...

	private static String simpleClosedWay(String withTags)
	{
		return "<node id='0' lat='0' lon='0' />"+
				"<node id='1' lat='5' lon='5' />"+
				"<node id='2' lat='5' lon='0' />"+
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
		return new JosmCountryBoundariesParser().read(new StringReader(xml));
	}
}