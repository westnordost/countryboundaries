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
        for (int c = 0; c < boundaries.raster.length; c++) {
            writeCell(boundaries.raster[c], out);
        }
    }

        out.writeInt(cell.containingIds.size());
    private void writeCell(CountryBoundariesCell cell, DataOutputStream out) throws IOException {
        for (String id : cell.containingIds) {
            out.writeUTF(id);
        }
        out.writeInt(cell.intersectingCountries.size());
        for (CountryAreas areas : cell.intersectingCountries) {
            writeAreas(areas, out);
        }
    }

    private void writeAreas(CountryAreas areas, DataOutputStream out) throws IOException {
        out.writeUTF(areas.id);
        writePolygons(areas.outer, out);
        writePolygons(areas.inner, out);
    }

        out.writeInt(polygons.length);
    private void writePolygons(Point[][] polygons, DataOutputStream out) throws IOException {
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
