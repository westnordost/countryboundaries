package de.westnordost.countryboundaries;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CountryBoundariesDeserializer {
    public CountryBoundaries read(DataInputStream in) throws IOException {
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

    private CountryBoundariesCell readCell(DataInputStream in) throws IOException {
        List<String> containingIds = Collections.emptyList();
        List<CountryAreas> intersectingCountries = Collections.emptyList();

        int containingIdsSize = in.readUnsignedByte();
        if(containingIdsSize > 0) {
            containingIds = new ArrayList<>(containingIdsSize);
            for (int i = 0; i < containingIdsSize; i++) {
                containingIds.add(in.readUTF().intern());
            }
        }
        int intersectingAreasSize = in.readUnsignedByte();
        if(intersectingAreasSize > 0) {
            intersectingCountries = new ArrayList<>(intersectingAreasSize);
            for (int i = 0; i < intersectingAreasSize; i++) {
                intersectingCountries.add(readAreas(in));
            }
        }
        return new CountryBoundariesCell(containingIds, intersectingCountries);
    }

    private CountryAreas readAreas(DataInputStream in) throws IOException {
        String id = in.readUTF().intern();
        Point[][] outer = readPolygons(in);
        Point[][] inner = readPolygons(in);
        return new CountryAreas(id, outer, inner);
    }

    private Point[][] readPolygons(DataInputStream in) throws IOException {
        Point[][] polygons = new Point[in.readUnsignedByte()][];
        for (int i = 0; i < polygons.length; i++) {
            polygons[i] = readRing(in);
        }
        return polygons;
    }

    private Point[] readRing(DataInputStream in) throws IOException {
        Point[] ring = new Point[in.readInt()];
        for (int j = 0; j < ring.length; j++) {
            ring[j] = readPoint(in);
        }
        return ring;
    }

        return new Point(in.readInt(), in.readInt());
    private Point readPoint(DataInputStream in) throws IOException {
    }
}
