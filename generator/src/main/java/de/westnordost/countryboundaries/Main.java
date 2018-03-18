package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class Main
{
	public static void main(String[] args) throws Exception {

		if(args.length < 3) {
			System.err.println("Missing parameters. I.e. 'boundaries.osm 360 180' ");
			return;
		}

		String filename = args[0];
		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);

		FileInputStream is = new FileInputStream(filename);

		GeometryCollection geometries;
		if(filename.endsWith(".json") || filename.endsWith(".geojson"))
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1)
			{
				baos.write(buffer, 0, length);
			}
			geometries = (GeometryCollection) new GeoJsonReader().read(baos.toString("UTF-8"));
		}
		else if(filename.endsWith(".osm"))
		{
			geometries = new JosmCountryBoundariesReader().read(new InputStreamReader(is, "UTF-8"));
		}
		else
		{
			System.err.println("Input file must be a OSM XML (.osm) or a GeoJSON (.json/.geojson)");
			return;
		}

		//String geojson = new GeoJsonWriter().write(geometries);
		//try(Writer writer = new OutputStreamWriter(new FileOutputStream("boundaries.json"), "UTF-8"))
		//{
		//	writer.write(geojson);
		//}

		for (int i = 0; i < geometries.getNumGeometries(); i++)
		{
			Geometry g = geometries.getGeometryN(i);
			g.setUserData(((Map)g.getUserData()).get("id"));
		}

		System.out.print("Generating index...");

		CountryBoundariesGenerator generator = new CountryBoundariesGenerator();
		generator.setProgressListener(new CountryBoundariesGenerator.ProgressListener()
		{
			String percentDone = "";

			@Override public void onProgress(float progress)
			{
				char[] chars = new char[percentDone.length()];
				Arrays.fill(chars, '\b');
				System.out.print(chars);
				percentDone = "" + String.format(Locale.US, "%.1f", 100*progress) + "%";
				System.out.print(percentDone);
			}
		});

		CountryBoundaries boundaries = generator.generate(width, height, geometries);
		try(FileOutputStream fos = new FileOutputStream("boundaries.ser"))
		{
			try(ObjectOutputStream oos = new ObjectOutputStream(fos))
			{
				boundaries.write(oos);
			}
		}
	}
}
