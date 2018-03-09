package de.westnordost.countryboundaries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RasterSpatialIndexJsonReader
{
	public RasterSpatialIndex read(String json)
	{
		JSONObject jsonObj = new JSONObject(json);
		short[] raster = createRaster(jsonObj.getJSONArray("raster"));
		int width = jsonObj.getInt("rasterWidth");
		QueryResult[] indices = createIndices(jsonObj.getJSONArray("indices"));
		return new RasterSpatialIndex(raster, width, indices);
	}

	private static short[] createRaster(JSONArray json)
	{
		short[] raster = new short[json.length()];
		for (int i = 0; i < json.length(); i++)
		{
			int n = json.getInt(i);
			if(n > Short.MAX_VALUE)
			{
				throw new IllegalArgumentException("RasterSpatialIndex only supports up to 32768 different indices");
			}
			raster[i] = (short) n;
		}
		return raster;
	}

	private static QueryResult[] createIndices(JSONArray json)
	{
		QueryResult[] indices = new QueryResult[json.length()];
		for (int i = 0; i < json.length(); i++)
		{
			JSONArray item = json.getJSONArray(i);
			indices[i] = new QueryResult(
					createStringList(item.getJSONArray(0)), createStringList(item.getJSONArray(1))
			);
		}
		return indices;
	}

	private static List<String> createStringList(JSONArray array)
	{
		if(array.length() == 0) return null;
		List<String> result = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++)
		{
			result.add(array.getString(i));
		}
		return result;
	}
}
