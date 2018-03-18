package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.junit.Test;

import static org.junit.Assert.*;

public class OgcSfsCompliantPolygonCreatorTest
{
	@Test public void createPolygonWithMergableHoles()
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel());
		OgcSfsCompliantPolygonCreator creator = new OgcSfsCompliantPolygonCreator(factory);

		Polygon p = creator.createPolygon(
			factory.createLinearRing(new Coordinate[]{p(0,0),p(4,0),p(4,4),p(0,4),p(0,0)}),
			new LinearRing[]{
					factory.createLinearRing(new Coordinate[]{p(1,1),p(1,3),p(3,3),p(1,1)}),
					factory.createLinearRing(new Coordinate[]{p(1,1),p(3,3),p(3,1),p(1,1)}),
			}
		);

		assertEquals(1,p.getNumInteriorRing());
		LinearRing expect = factory.createLinearRing(new Coordinate[]{p(1,1),p(1,3),p(3,3),p(3,1),p(1,1)});
		assertTrue(expect.isValid());
		expect.normalize();
		LineString actual = p.getInteriorRingN(0);
		actual.normalize();
		assertEquals(expect,actual);
	}

	@Test public void createMultiPolygonMergable()
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel());
		OgcSfsCompliantPolygonCreator creator = new OgcSfsCompliantPolygonCreator(factory);

		MultiPolygon mp = creator.createMultiPolygon(new Polygon[]{
			factory.createPolygon(new Coordinate[]{p(0,0),p(4,0),p(0,4),p(0,0)}),
			factory.createPolygon(new Coordinate[]{p(4,0),p(4,4),p(0,4),p(4,0)}),
			factory.createPolygon(new Coordinate[]{p(4,0),p(8,4),p(4,4),p(4,0)})
		});

		assertEquals(1, mp.getNumGeometries());
		Polygon expect = factory.createPolygon(new Coordinate[]{p(0,0),p(0,4),p(4,4),p(8,4),p(4,0),p(0,0)});
		assertTrue(expect.isValid());
		expect.normalize();
		Geometry actual = mp.getGeometryN(0);
		actual.normalize();
		assertEquals(expect,actual);
	}

	private static Coordinate p(int x, int y)
	{
		return new Coordinate(x,y);
	}

}