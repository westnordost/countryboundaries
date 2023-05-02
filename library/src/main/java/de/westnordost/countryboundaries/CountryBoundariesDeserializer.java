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
        int version = in.readUnsignedShort();
        if (version == 44269) { // old/unversioned boundaries file format
            throw new IOException("The file serialization format changed. If you are using the default data, get the updated data from https://github.com/westnordost/countryboundaries/tree/master/data , otherwise, you need to re-generate the data with the current version of the generator found in the same repository.");
        } else if (version != 2) {
            throw new IOException("Wrong version number '" + version + "' of the file serialization format (expected: '2'). You may need to get the current version of the data.");
        }

        int geometrySizesCount = in.readInt();
        if (geometrySizesCount < 0) throw new IOException("Invalid data");
        Map<String, Double> geometrySizes = new HashMap<>(geometrySizesCount);
        for (int i = 0; i < geometrySizesCount; i++) {
            geometrySizes.put(in.readUTF().intern(), in.readDouble());
        }
        int rasterWidth = in.readInt();
        if (rasterWidth < 0) throw new IOException("Invalid data");
        int rasterSize = in.readInt();
        if (rasterSize < 0) throw new IOException("Invalid data");
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
        int ringSize = in.readInt();
        if (ringSize < 0) throw new IOException("Invalid data");
        Point[] ring = new Point[ringSize];
        for (int j = 0; j < ring.length; j++) {
            ring[j] = readPoint(in);
        }
        return ring;
    }

    private Point readPoint(DataInputStream in) throws IOException {
        return new Point(in.readUnsignedShort(), in.readUnsignedShort());
    }
}
