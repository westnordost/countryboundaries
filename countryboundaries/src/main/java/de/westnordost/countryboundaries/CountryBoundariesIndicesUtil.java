package de.westnordost.countryboundaries;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CountryBoundariesIndicesUtil
{
	public static CountryQueryResult[] createFromJson(String json)
	{
		JSONArray jsonArray = new JSONArray(json);
		CountryQueryResult[] result = new CountryQueryResult[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++)
		{
			JSONArray item = jsonArray.getJSONArray(i);
			result[i] = new CountryQueryResult(
					toStringList(item.getJSONArray(0)), toStringList(item.getJSONArray(1))
			);
		}
		return result;
	}

	private static List<String> toStringList(JSONArray array)
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
