package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;

/** JTS MultiPolygon imposes a restriction on the contained Polygons that a GeoJson MultiPolygon
 *  does not impose. From the JTS documentation:
 *  "As per the OGC SFS specification, the Polygons in a MultiPolygon may not overlap, and
 *  may only touch at single points. This allows the topological point-set semantics to be
 *  well-defined."
 *  The same is the case for polygons with multiple touching holes.
 *
 *  This factory makes sure to create all polygons and multipolygons OGC SFS compliant.
 *
 *  Finding and merging linear rings that touch each other in a line is computationally
 *  quite expensive, so only use this factory if the input cannot be guaranteed to be compliant
 *  already.
 *  */
public class OgcSfsCompliantPolygonCreator
{
	private final GeometryFactory factory;

	public OgcSfsCompliantPolygonCreator(GeometryFactory factory)
	{
		this.factory = factory;
	}

	public MultiPolygon createMultiPolygon(Polygon[] polygons)
	{
		ArrayList<Polygon> polygonList = new ArrayList<>(Arrays.asList(polygons));
		mergePolygons(polygonList);
		return factory.createMultiPolygon(polygonList.toArray(new Polygon[]{}));
	}

	public Polygon createPolygon(LinearRing shell, LinearRing[] holes)
	{
		if(holes == null)
		{
			return factory.createPolygon(shell, null);
		}
		ArrayList<LinearRing> holesList = new ArrayList<>(Arrays.asList(holes));
		mergeHoles(holesList);
		return factory.createPolygon(shell, holesList.toArray(new LinearRing[]{}));
	}

	private void mergeHoles(ArrayList<LinearRing> rings)
	{
		if (rings.size() == 1) return;

		// need to be converted to polygons and back because linearring is a lineal data structure,
		// we want to merge by area
		ArrayList<Polygon> polygons = new ArrayList<>(rings.size());
		for (LinearRing ring : rings)
		{
			polygons.add(factory.createPolygon(ring));
		}
		mergePolygons(polygons);

		// something was merged. Convert polygons back to rings
		if (polygons.size() != rings.size())
		{
			rings.clear();
			for (Polygon polygon : polygons)
			{
				rings.add((LinearRing) polygon.getExteriorRing());
			}
		}
	}

	private static void mergePolygons(ArrayList<Polygon> polygons)
	{
		if (polygons.size() == 1) return;

		for (int i1 = 0; i1 < polygons.size() - 1; ++i1)
		{
			Polygon p1 = polygons.get(i1);
			for (int i2 = i1 + 1; i2 < polygons.size(); ++i2)
			{
				Polygon p2 = polygons.get(i2);
				// Geometry.union() seems to not have this optimization (bbox check)
				if (!p1.getEnvelopeInternal().intersects(p2.getEnvelopeInternal())) continue;

				Geometry p1p2Union = p1.union(p2);
				// if p1 and p2 wouldn't intersect, p1p2Union would be a GeometryCollection or MultiPolygon
				if (p1p2Union instanceof Polygon)
				{
					polygons.remove(i2);
					polygons.set(i1, (Polygon) p1p2Union);
					--i1; // start again at i1
					break;
				}
			}
		}
	}
}
