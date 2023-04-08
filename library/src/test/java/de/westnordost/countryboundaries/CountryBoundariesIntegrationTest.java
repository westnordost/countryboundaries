package de.westnordost.countryboundaries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class CountryBoundariesIntegrationTest {

    @Test public void returnCorrectResultsAtCellEdges() throws Exception {
        CountryBoundaries boundaries = load();

        // in clockwise order...
        assertEquals(Collections.singletonList("HR"), boundaries.getIds(16.0, 45.5));
        assertEquals(Collections.singletonList("HR"), boundaries.getIds(16.0, 46.0));
        assertEquals(Collections.singletonList("HR"), boundaries.getIds(16.5, 46.0));
        assertEquals(Collections.singletonList("HR"), boundaries.getIds(17.0, 46.0));
        assertEquals(Collections.singletonList("HR"), boundaries.getIds(17.0, 45.5));

        assertEquals(Collections.singletonList("BA"), boundaries.getIds(17.0, 45.0));
        assertEquals(Collections.singletonList("BA"), boundaries.getIds(16.5, 45.0));
        assertEquals(Collections.singletonList("BA"), boundaries.getIds(16.0, 45.0));
    }


    @Test public void getContainingIdsAt180thMeridian() throws Exception {
        HashSet<String> s = new HashSet<>();
        s.add("RU");
        assertEquals(s, load().getContainingIds(178, 66, -178, 68));
    }

    @Test public void getIntersectingIdsAt180thMeridian() throws Exception {
        HashSet<String> s = new HashSet<>();
        s.add("RU");
        s.add("US-AK");
        s.add("US");
        assertEquals(s, load().getIntersectingIds(163, 50, -150, 67));
    }

    @Test public void isInAny() throws Exception {
        // The given position is in Myanmar and not in any of these countries
        assertFalse(load().isInAny(96, 21, Arrays.asList("BD", "DJ", "IR", "PS")));
    }

    @Test public void buesingen() throws Exception {
        assertTrue(load().isIn(8.6910, 47.6973, "DE"));
    }

    @Test public void getIdsSortsBySizeAscending() throws Exception {
        assertEquals(
                Arrays.asList("US-TX", "US"),
                load().getIds(-97, 33)
        );
    }

    private CountryBoundaries load() throws IOException  {
        return CountryBoundaries.load(new FileInputStream("../data/boundaries360x180.ser"));
    }
}
