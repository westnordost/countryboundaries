package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.GeometryCollection;

import org.json.JSONStringer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.westnordost.countryboundaries.geojson.GeoJsonReader;
import de.westnordost.countryboundaries.geojson.GeoJsonWriter;

public class Generator
{
	public static void main(String[] args) throws Exception {

		if(args.length < 3) {
			System.err.println("Missing parameters. I.e. 'myinput.json 360 180' ");
			return;
		}

		FileInputStream is = new FileInputStream(args[0]);
		String inGeoJson = IOStreamUtils.readToString(is);

		GeoJsonReader geoJsonReader = new GeoJsonReader();
		GeometryCollection geometryCollection = (GeometryCollection) geoJsonReader.read(inGeoJson);
		CountryBoundaries countryBoundaries = new CountryBoundaries(
				geometryCollection, new CountrySTRtree(geometryCollection));

		ArrayList<CountryQueryResult> indexToCountryMap = new ArrayList<>();
		Map<CountryQueryResult, Integer> countryToIndexMap = new HashMap<>();

		int xSteps = Integer.valueOf(args[1]), ySteps = Integer.valueOf(args[2]);
		BufferedImage bi = new BufferedImage(xSteps, ySteps, BufferedImage.TYPE_INT_RGB);

		for(int y=0; y < ySteps; ++y)
		{
			for(int x=0; x < xSteps; ++x)
			{
				double lonMin = -180.0 + 360.0 * x/xSteps;
				double latMin = +90.0 - 180.0 * y/ySteps;
				double lonMax = -180.0 + 360.0 * (x+1)/xSteps;
				double latMax = +90.0 - 180.0 * (y+1)/ySteps;

				CountryQueryResult box = countryBoundaries.getIsoCodes(lonMin, latMin, lonMax, latMax);
				int index;
				// already index assigned
				if(countryToIndexMap.containsKey(box))
				{
					index = countryToIndexMap.get(box);
				}
				// no index assigned yet
				else
				{
					indexToCountryMap.add(box);
					index = indexToCountryMap.size()-1;
					countryToIndexMap.put(box, index);
				}

				bi.setRGB(x, y, index);
			}
		}

		File bitmap = new File("bitmap.png");
		ImageIO.write(bi, "png", bitmap);

		CountryQueryResult[] bitmapIndices = new CountryQueryResult[indexToCountryMap.size()];
		indexToCountryMap.toArray(bitmapIndices);
		IOStreamUtils.writeStream(toJson(bitmapIndices), new FileOutputStream("bitmapIndices.json"));

		String outGeoJson = new GeoJsonWriter().write(geometryCollection);
		IOStreamUtils.writeStream(outGeoJson, new FileOutputStream("boundaries.json"));
	}

	private static String toJson(CountryQueryResult[] map)
	{
		JSONStringer json = new JSONStringer();
		json.array();
		for(CountryQueryResult box : map)
		{
			json.array();
			for (String cc : box.getContainingCountryCodes())
			{
				json.value(cc);
			}
			json.endArray();
			json.array();
			for (String cc : box.getIntersectingCountryCodes())
			{
				json.value(cc);
			}
			json.endArray();
		}
		json.endArray();
		return json.toString();
	}
}
