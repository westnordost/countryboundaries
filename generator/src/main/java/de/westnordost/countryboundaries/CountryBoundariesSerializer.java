package de.westnordost.countryboundaries;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class CountryBoundariesSerializer {
    public void write(CountryBoundaries boundaries, ObjectOutputStream out) throws IOException {
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

    private void writeCell(CountryBoundariesCell cell, ObjectOutputStream out) throws IOException {
        out.writeInt(cell.containingIds.size());
        for (String id : cell.containingIds) {
            out.writeUTF(id);
        }
        out.writeInt(cell.intersectingCountries.size());
        for (CountryAreas areas : cell.intersectingCountries) {
            writeAreas(areas, out);
        }
    }

    private void writeAreas(CountryAreas areas, ObjectOutputStream out) throws IOException {
        out.writeUTF(areas.id);
        writePolygons(areas.outer, out);
        writePolygons(areas.inner, out);
    }

    private void writePolygons(Point[][] polygons, ObjectOutputStream out) throws IOException {
        out.writeInt(polygons.length);
        for (Point[] ring : polygons) {
            writeRing(ring, out);
        }
    }

    private void writeRing(Point[] points, ObjectOutputStream out) throws IOException {
        out.writeInt(points.length);
        for (Point point : points) {
            writePoint(point, out);
        }
    }

    private void writePoint(Point point, ObjectOutputStream out) throws IOException {
        out.writeInt(point.x);
        out.writeInt(point.y);
    }
}
