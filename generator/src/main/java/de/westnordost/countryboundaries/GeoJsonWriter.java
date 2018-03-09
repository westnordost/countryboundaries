package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONException;
import org.json.JSONStringer;

import java.util.List;
import java.util.Map;

public class GeoJsonWriter
{
	private static final String
			TYPE = "type",
			FEATURES = "features",
			COORDINATES = "coordinates",
			GEOMETRIES = "geometries",
			GEOMETRY = "geometry",
			PROPERTIES = "properties";

	/**
	 * Writes a geometry to GeoJSON.
	 * Note that any data stored in getUserData() of the geometries that is not on the top level
	 * GeometryCollection, is lost in the process.
	 */
	public String write(Geometry geometry)
	{
		try
		{
			JSONStringer b = new JSONStringer();
			if (geometry.getClass().equals(GeometryCollection.class))
			{
				writeFeatureCollection(b, (GeometryCollection) geometry);
			}
			else
			{
				writeFeature(b, geometry);
			}
			return b.toString();
		}
		catch (JSONException e)
		{
			throw new GeoJsonException(e);
		}
	}

	private void writeFeatureCollection(JSONStringer b, GeometryCollection g) throws JSONException
	{
		b.object();
		b.key(TYPE).value("FeatureCollection");
		b.key(FEATURES).array();
		for (int i = 0; i < g.getNumGeometries(); i++)
		{
			writeFeature(b, g.getGeometryN(i));
		}
		b.endArray();
		b.endObject();
	}

	private void writeFeature(JSONStringer b, Geometry g) throws JSONException
	{
		b.object();
		b.key(TYPE).value("Feature");
		b.key(PROPERTIES);
		writeProperties(b, g.getUserData());
		b.key(GEOMETRY);
		writeGeometry(b, g);
		b.endObject();
	}

	private void writeProperties(JSONStringer b, Object object) throws JSONException
	{
		if (object == null || !(object instanceof Map))
		{
			b.object().endObject();
		}
		else
		{
			writeObject(b, object);
		}
	}

	private void writeObject(JSONStringer b, Object object) throws JSONException
	{
		if (object == null)
		{
			b.value(null);
		}
		else if (object instanceof Map)
		{
			b.object();
			Map props = (Map) object;
			for (Object o : props.entrySet())
			{
				Map.Entry entry = (Map.Entry) o;
				b.key(String.valueOf(entry.getKey()));
				writeObject(b, entry.getValue());
			}
			b.endObject();
		}
		else if (object instanceof List)
		{
			b.array();
			List list = (List) object;
			for (Object o : list)
			{
				writeObject(b, o);
			}
			b.endArray();
		}
		else
		{
			b.value(object);
		}
	}

	private void writeGeometry(JSONStringer b, Geometry g) throws JSONException
	{
		b.object();

		if (g instanceof Point)
		{
			b.key(TYPE).value("Point").key(COORDINATES);
			writeCoordinate(b, g.getCoordinate());
		}
		else if (g instanceof MultiPoint)
		{
			b.key(TYPE).value("MultiPoint").key(COORDINATES);
			writeCoordinates(b, g.getCoordinates());
		}
		else if (g instanceof LineString)
		{
			b.key(TYPE).value("LineString").key(COORDINATES);
			writeCoordinates(b, g.getCoordinates());
		}
		else if (g instanceof MultiLineString)
		{
			b.key(TYPE).value("MultiLineString").key(COORDINATES);
			writeMultiCoordinates(b, g);
		}
		else if (g instanceof Polygon)
		{
			b.key(TYPE).value("Polygon").key(COORDINATES);
			writePolygon(b, createReversed((Polygon) g));
		}
		else if (g instanceof MultiPolygon)
		{
			b.key(TYPE).value("MultiPolygon").key(COORDINATES);
			MultiPolygon mp = (MultiPolygon) g;
			Polygon[] reversedPolys = new Polygon[mp.getNumGeometries()];
			for (int i = 0; i < mp.getNumGeometries(); i++)
			{
				reversedPolys[i] = createReversed((Polygon) mp.getGeometryN(i));
			}
			mp = mp.getFactory().createMultiPolygon(reversedPolys);

			writeMultiMultiCoordinates(b, mp);
		}
		else if (g instanceof GeometryCollection)
		{
			b.key(TYPE).value("GeometryCollection").key(GEOMETRIES);
			b.array();
			for (int i = 0; i < g.getNumGeometries(); i++)
			{
				writeGeometry(b, g.getGeometryN(i));
			}
			b.endArray();
		}
		else
		{
			throw new GeoJsonException(
					"Converting geometry " + g.getClass().getSimpleName() + " to GeoJSON is not supported");
		}

		b.endObject();
	}

	/* The shell of a polygon in GeoJSON is defined in counterclockwise order, in JTS, it is the
	   other way round */
	private static Polygon createReversed(Polygon geometry)
	{
		Polygon r = (Polygon) geometry.clone();
		r.normalize();
		r.reverse();
		return r;
	}

	private void writeMultiMultiCoordinates(JSONStringer b, Geometry g) throws JSONException
	{
		b.array();
		for (int i = 0; i < g.getNumGeometries(); i++)
		{
			writeMultiCoordinates(b, g.getGeometryN(i));
		}
		b.endArray();
	}

	private void writeMultiCoordinates(JSONStringer b, Geometry g) throws JSONException
	{
		b.array();
		for (int i = 0; i < g.getNumGeometries(); i++)
		{
			writeCoordinates(b, g.getGeometryN(i).getCoordinates());
		}
		b.endArray();
	}

	private void writePolygon(JSONStringer b, Polygon p) throws JSONException
	{
		b.array();
		writeCoordinates(b, p.getExteriorRing().getCoordinates());
		for (int i = 0; i < p.getNumInteriorRing(); i++)
		{
			writeCoordinates(b, p.getInteriorRingN(i).getCoordinates());
		}
		b.endArray();
	}

	private void writeCoordinates(JSONStringer b, Coordinate[] coords) throws JSONException
	{
		b.array();
		for (Coordinate coord : coords)
		{
			writeCoordinate(b, coord);
		}
		b.endArray();
	}

	private void writeCoordinate(JSONStringer b, Coordinate coord) throws JSONException
	{
		b.array().value(coord.x).value(coord.y);
		if (!Double.isNaN(coord.z)) b.value(coord.z);
		b.endArray();
	}
}
