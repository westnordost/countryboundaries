package de.westnordost.countryboundaries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CountryBoundariesDeserializer {
    public CountryBoundaries read(ObjectInputStream in) throws IOException  {
        int geometrySizesCount = in.readInt();
        Map<String, Double> geometrySizes = new HashMap<>(geometrySizesCount);
        for (int i = 0; i < geometrySizesCount; i++) {
            geometrySizes.put(in.readUTF().intern(), in.readDouble());
        }
        int rasterWidth = in.readInt();
        int rasterSize = in.readInt();
        CountryBoundariesCell[] raster = new CountryBoundariesCell[rasterSize];
        for (int i= 0; i < rasterSize; i++) {
            raster[i] = readCell(in);
        }
        return new CountryBoundaries(raster, rasterWidth, geometrySizes);
    }

    private CountryBoundariesCell readCell(ObjectInputStream in) throws IOException {
        List<String> containingIds = Collections.emptyList();
        List<CountryAreas> intersectingCountries = Collections.emptyList();

        int containingIdsSize = in.readInt();
        if(containingIdsSize > 0) {
            containingIds = new ArrayList<>(containingIdsSize);
            for (int i = 0; i < containingIdsSize; i++) {
                containingIds.add(in.readUTF().intern());
            }
        }
        int intersectingPolygonsSize = in.readInt();
        if(intersectingPolygonsSize > 0) {
            intersectingCountries = new ArrayList<>(intersectingPolygonsSize);
            for (int i = 0; i < intersectingPolygonsSize; i++) {
                intersectingCountries.add(readAreas(in));
            }
        }
        return new CountryBoundariesCell(containingIds, intersectingCountries);
    }

    private CountryAreas readAreas(ObjectInputStream in) throws IOException {
        String id = in.readUTF().intern();
        Point[][] outer = new Point[in.readInt()][];
        for (int i = 0; i < outer.length; i++) {
            outer[i] = readRing(in);
        }
        Point[][] inner = new Point[in.readInt()][];
        for (int i = 0; i < inner.length; i++) {
            inner[i] = readRing(in);
        }
        return new CountryAreas(id, outer, inner);
    }

    private Point[] readRing(ObjectInputStream in) throws IOException {
        Point[] ring = new Point[in.readInt()];
        for (int j = 0; j < ring.length; j++) {
            ring[j] = readPoint(in);
        }
        return ring;
    }

    private Point readPoint(ObjectInputStream in) throws IOException {
        return new Point(in.readInt(), in.readInt());
    }
}
