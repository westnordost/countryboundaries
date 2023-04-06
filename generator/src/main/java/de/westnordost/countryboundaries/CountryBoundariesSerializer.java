package de.westnordost.countryboundaries;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class CountryBoundariesSerializer {
    public void write(CountryBoundaries boundaries, DataOutputStream out) throws IOException {
        // version
        out.writeShort(2);

        out.writeInt(boundaries.geometrySizes.size());
        for (Map.Entry<String, Double> e : boundaries.geometrySizes.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeDouble(e.getValue());
        }
        out.writeInt(boundaries.rasterWidth);
        out.writeInt(boundaries.raster.length);
        for (CountryBoundariesCell cell : boundaries.raster) {
            writeCell(cell, out);
        }
    }

    private void writeCell(CountryBoundariesCell cell, DataOutputStream out) throws IOException {
        int containingIdsSize = cell.containingIds.size();
        if (containingIdsSize > 255) {
            throw new IllegalArgumentException("At most 255 different areas per cell are supported (try a bigger raster)");
        }
        out.writeByte(containingIdsSize);
        for (String id : cell.containingIds) {
            out.writeUTF(id);
        }
        int intersectingCountriesSize = cell.intersectingCountries.size();
        if (intersectingCountriesSize > 255) {
            throw new IllegalArgumentException("At most 255 different areas per cell are supported (try a bigger raster)");
        }
        out.writeByte(intersectingCountriesSize);
        for (CountryAreas areas : cell.intersectingCountries) {
            writeAreas(areas, out);
        }
    }

    private void writeAreas(CountryAreas areas, DataOutputStream out) throws IOException {
        out.writeUTF(areas.id);
        writePolygons(areas.outer, out);
        writePolygons(areas.inner, out);
    }

    private void writePolygons(Point[][] polygons, DataOutputStream out) throws IOException {
        int polygonsCount = polygons.length;
        if (polygonsCount > 255) {
            throw new IllegalArgumentException("At most 255 different polygons are supported per area (try a bigger raster)");
        }
        out.writeByte(polygons.length);
        for (Point[] ring : polygons) {
            writeRing(ring, out);
        }
    }

    private void writeRing(Point[] points, DataOutputStream out) throws IOException {
        out.writeInt(points.length);
        for (Point point : points) {
            writePoint(point, out);
        }
    }

    private void writePoint(Point point, DataOutputStream out) throws IOException {
        if (point.x < 0 || point.x > 0xffff || point.y < 0 || point.y > 0xffff) {
            throw new RuntimeException("A Point must contain only unsigned shorts (this is very likely a bug in this program)");
        }
        out.writeShort(point.x);
        out.writeShort(point.y);
    }
}
