package de.westnordost.countryboundaries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryBoundariesSerializerTest {

    @Test
    public void serializationWorks() throws IOException {
        Map<String,Double> sizes = new HashMap<>();
        sizes.put("A",123.0);
        sizes.put("B",64.4);

        Point[] A = polygon(p(0,0),p(0,1),p(1,0));
        Point[] B = polygon(p(0,0),p(0,3),p(3,3),p(3,0));
        Point[] Bh = polygon(p(1,1),p(2,1),p(2,2),p(1,2));

        CountryBoundaries boundaries = new CountryBoundaries(
                cells(
                        cell(null, null),
                        cell(arrayOf("A","B"), null),
                        cell(arrayOf("B"), countryAreas(new CountryAreas("A",polygons(A),polygons()))),
                        cell(null, countryAreas(
                                new CountryAreas("B",polygons(B), polygons(Bh)),
                                new CountryAreas("C",polygons(B,A), polygons(Bh))
                        ))
                ), 2, sizes
        );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        new CountryBoundariesSerializer().write(boundaries, os);
        os.close();
        bos.close();

        byte[] bytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(bis);
        CountryBoundaries boundaries2 = new CountryBoundariesDeserializer().read(is);

        assertEquals(boundaries, boundaries2);
    }

    private static CountryBoundariesCell[] cells(CountryBoundariesCell ...cells) { return cells; }

    private static CountryBoundariesCell cell(String[] containingIds, CountryAreas[] intersecting) {
        return new CountryBoundariesCell(
                containingIds == null ? listOf() : listOf(containingIds),
                intersecting == null ? listOf() : listOf(intersecting)
        );
    }

    private static Point p(double x, double y) {
        return new Point(Fixed1E7.doubleToFixed(x),Fixed1E7.doubleToFixed(y));
    }

    private static Point[] polygon(Point ...points) { return points; }
    private static Point[][] polygons(Point[] ...polygons) { return polygons; }

    private static CountryAreas[] countryAreas(CountryAreas ...areas) { return areas; }

    private static String[] arrayOf(String ...elements) { return elements; }

    private static <T> List<T> listOf(T ...elements) { return Arrays.asList(elements); }
}
