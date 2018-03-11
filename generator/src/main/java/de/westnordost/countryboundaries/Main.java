package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
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

		FileInputStream is = new FileInputStream(args[0]);
		GeometryCollection geometries = new JosmCountryBoundariesReader().read(new InputStreamReader(is, "UTF-8"));

		String geojson = new GeoJsonWriter().write(geometries);
		StreamUtils.writeStream(geojson, new FileOutputStream("boundaries.json"));

		for (int i = 0; i < geometries.getNumGeometries(); i++)
		{
			Geometry g = geometries.getGeometryN(i);
			g.setUserData(((Map)g.getUserData()).get("id"));
		}
		CountryBoundaries boundaries = new CountryBoundaries(geometries, new STRtreeIndex(geometries));

		System.out.print("Generating index...");
		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);

		RasterSpatialIndexGenerator generator = new RasterSpatialIndexGenerator();
		generator.setProgressListener(new RasterSpatialIndexGenerator.ProgressListener()
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


		RasterSpatialIndex index = generator.generate(width, height, boundaries);
		String indexJson = new RasterSpatialIndexJsonWriter().write(index);
		StreamUtils.writeStream(indexJson, new FileOutputStream("boundaries_index.json"));
	}
}
