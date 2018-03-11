package de.westnordost.countryboundaries;

import org.json.JSONStringer;

public class RasterSpatialIndexJsonWriter
{
	public String write(RasterSpatialIndex index)
	{
		JSONStringer json = new JSONStringer();
		json.object();
		json.key("raster");
		createRasterJson(json, index.getRaster());
		json.key("rasterWidth");
		json.value(index.getRasterWidth());
		json.key("indices");
		createIndicesJson(json, index.getIndices());
		json.endObject();
		return json.toString();
	}

	private static void createRasterJson(JSONStringer json, short[] raster)
	{
		json.array();
		for (short i : raster)
		{
			json.value(i);
		}
		json.endArray();
	}

	private static void createIndicesJson(JSONStringer json, QueryResult[] indices)
	{
		json.array();
		for(QueryResult box : indices)
		{
			json.array();

			json.array();
			for (String cc : box.getContainingIds())
			{
				json.value(cc);
			}
			json.endArray();
			json.array();
			for (String cc : box.getIntersectingIds())
			{
				json.value(cc);
			}
			json.endArray();

			json.endArray();
		}
		json.endArray();
	}
}
