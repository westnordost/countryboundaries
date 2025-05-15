package de.westnordost.countryboundaries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Main
{
	public static void main(String[] args) throws Exception {

		if(args.length != 3 && args.length != 2) {
			System.err.println("Missing parameters. F.e. 'boundaries.osm 360 180' or 'boundaries.osm boundaries.json' ");
			return;
		}
		String filename = args[0];
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

		Set<String> excludeCountries = new HashSet<>();
		excludeCountries.add("FX"); // "metropolitan France" is not a country
		excludeCountries.add("EU"); // not a country
		excludeCountries.add("AQ"); // not a country

		List<Geometry> geometryList = new ArrayList<>(geometries.getNumGeometries());
		for (int i = 0; i < geometries.getNumGeometries(); i++)
		{
			Geometry g = geometries.getGeometryN(i);
			Object id = ((Map)g.getUserData()).get("id");
			if (id instanceof String && !excludeCountries.contains(id)) {
				geometryList.add(g);
			}
		}

		if(args.length == 2) {
			Geometry[] geometryArray = new Geometry[geometryList.size()];
			geometryList.toArray(geometryArray);
			GeometryCollection collection = new GeometryCollection(geometryArray, new GeometryFactory());
			String geojson = new GeoJsonWriter().write(collection);
			try(Writer writer = new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8")) {
				writer.write(geojson);
			}
			return;
		}

		for (Geometry geometry : geometryList) {
			Object id = ((Map)geometry.getUserData()).get("id");
			geometry.setUserData(id);
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

		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);
		CountryBoundaries boundaries = generator.generate(width, height, geometryList);
		try(FileOutputStream fos = new FileOutputStream("boundaries.ser"))
		{
			CountryBoundariesUtils.serializeTo(fos, boundaries);
		}
	}
}
