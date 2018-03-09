package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.GeometryCollection;

import org.json.JSONStringer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
				geometryCollection, new STRtreeIndex(geometryCollection));



		String outGeoJson = new GeoJsonWriter().write(geometryCollection);
		IOStreamUtils.writeStream(outGeoJson, new FileOutputStream("boundaries.json"));
	}

	private static String toJson(QueryResult[] map)
	{
		JSONStringer json = new JSONStringer();
		json.array();
		for(QueryResult box : map)
		{
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
		}
		json.endArray();
		return json.toString();
	}

	public static void writeStream(String string, OutputStream os) throws IOException
	{
		Writer writer = null;
		try
		{
			writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(string);
		}
		finally
		{
			if(writer != null) writer.close();
		}
	}
}
