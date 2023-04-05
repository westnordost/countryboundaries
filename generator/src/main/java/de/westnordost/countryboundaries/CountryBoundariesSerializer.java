package de.westnordost.countryboundaries;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class CountryBoundariesSerializer {
    public void write(CountryBoundaries boundaries, DataOutputStream out) throws IOException {
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

        out.writeInt(point.x);
        out.writeInt(point.y);
    private void writePoint(Point point, DataOutputStream out) throws IOException {
    }
}
