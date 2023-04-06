package de.westnordost.countryboundaries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CountryBoundariesIntegrationTest {

    @Test public void returnCorrectResultsAtCellEdges() throws Exception {
        CountryBoundaries boundaries = CountryBoundaries.load(new FileInputStream("../data/boundaries360x180.ser"));


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
}
