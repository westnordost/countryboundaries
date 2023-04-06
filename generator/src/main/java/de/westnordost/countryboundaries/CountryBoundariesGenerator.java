package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryBoundariesGenerator
{
	private static final int WGS84 = 4326;
	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), WGS84);

	private ProgressListener listener;

	public interface ProgressListener
	{
		void onProgress(float progress);
	}

	public void setProgressListener(ProgressListener listener)
	{
		this.listener = listener;
	}

	public CountryBoundaries generate(int width, int height, List<Geometry> boundaries)
	{
		Map<String, Double> geometrySizes = calculateGeometryAreas(boundaries);

		STRtree index = buildIndex(boundaries);

		CountryBoundariesCell[] raster = new CountryBoundariesCell[width * height];

		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				double lonMin = -180.0 + 360.0 * x / width;
				double latMax = +90.0 - 180.0 * y / height;
				double lonMax = -180.0 + 360.0 * (x + 1) / width;
				double latMin = +90.0 - 180.0 * (y + 1) / height;

				raster[x + y * width] = createCell(index, lonMin, latMin, lonMax, latMax);

				if(listener != null) listener.onProgress((float)(y*width+x)/(width*height));
			}
		}
		if(listener != null) listener.onProgress(1);

		return new CountryBoundaries(raster, width, geometrySizes);
	}

	private CountryBoundariesCell createCell(
			STRtree index, double lonMin, double latMin, double lonMax, double latMax)
	{
		Polygon bounds = createBounds(lonMin, latMin, lonMax, latMax);

		List<String> containingIds = new ArrayList<>();
		List<CountryAreas> intersectingAreas = new ArrayList<>();

		List<Geometry> geometries = index.query(bounds.getEnvelopeInternal());
		for (Geometry g : geometries)
		{
			String areaId = getAreaId(g);
			if(areaId == null) continue;

			IntersectionMatrix im = g.relate(bounds);
			if(im.isCovers())
			{
				containingIds.add(areaId);
			}
			else if(!im.isDisjoint())
			{
				Geometry intersection = g.intersection(bounds);
				if(!(intersection instanceof Polygonal))
				{
					continue;
				}
				intersection.normalize();
				intersectingAreas.add(createCountryAreas(areaId, intersection, lonMin, latMin, lonMax, latMax));
			}
		}
		return new CountryBoundariesCell(containingIds, intersectingAreas);
	}

	private CountryAreas createCountryAreas(
			String areaId, Geometry intersection, double lonMin, double latMin, double lonMax, double latMax)
	{
		List<Point[]> outer = new ArrayList<>(), inner = new ArrayList<>();

		if(intersection instanceof Polygon)
		{
			Polygon p = (Polygon) intersection;
			outer.add(createPoints(p.getExteriorRing(), lonMin, latMin, lonMax, latMax));
			for (int j = 0; j < p.getNumInteriorRing(); j++)
			{
				inner.add(createPoints(p.getInteriorRingN(j), lonMin, latMin, lonMax, latMax));
			}
		}
		else
		{
			MultiPolygon mp = (MultiPolygon) intersection;
			for (int i = 0; i < mp.getNumGeometries(); i++)
			{
				Polygon p = (Polygon) mp.getGeometryN(i);
				outer.add(createPoints(p.getExteriorRing(), lonMin, latMin, lonMax, latMax));
				for (int j = 0; j < p.getNumInteriorRing(); j++)
				{
					inner.add(createPoints(p.getInteriorRingN(j), lonMin, latMin, lonMax, latMax));
				}
			}
		}
		return new CountryAreas(areaId,outer.toArray(new Point[][]{}), inner.toArray(new Point[][]{}));
	}

	private Point[] createPoints(LineString ring, double lonMin, double latMin, double lonMax, double latMax)
	{
		Coordinate[] coords = ring.getCoordinates();
		// leave out last - not necessary
		Point[] result = new Point[coords.length-1];
		for (int i = 0; i < coords.length-1; i++)
		{
			Coordinate coord = coords[i];
			int x = (int) ((coord.x - lonMin) * 0xffff / (lonMax - lonMin));
			int y = (int) ((coord.y - latMin) * 0xffff / (latMax - latMin));
			result[i] = new Point(x, y);
		}
		return result;
	}

	private STRtree buildIndex(List<Geometry> geometries)
	{
		STRtree index = new STRtree();
		for (Geometry g : geometries)
		{
			String areaId = getAreaId(g);
			if(areaId != null)
			{
				index.insert(g.getEnvelopeInternal(), g);
			}
		}
		return index;
	}

	private Map<String,Double> calculateGeometryAreas(List<Geometry> geometries)
	{
		Map<String, Double> geometryAreas = new HashMap<>(geometries.size());
		for (Geometry g : geometries)
		{
			String areaId = getAreaId(g);
			if(areaId != null)
			{
				geometryAreas.put(areaId, g.getArea());
			}
		}
		return geometryAreas;
	}

	private String getAreaId(Geometry g)
	{
		if(g instanceof Polygonal)
		{
			Object data = g.getUserData();
			if(data != null && data instanceof String)
			{
				return (String) data;
			}
		}
		return null;
	}

	private Polygon createBounds(double lonMin, double latMin, double lonMax, double latMax)
	{
		return factory.createPolygon(new Coordinate[]
		{
			new Coordinate(lonMin, latMin),
			new Coordinate(lonMin, latMax),
			new Coordinate(lonMax, latMax),
			new Coordinate(lonMax, latMin),
			new Coordinate(lonMin, latMin)
		});
	}
}
