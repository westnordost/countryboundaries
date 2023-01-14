package de.westnordost.countryboundaries;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.FileInputStream;
import java.util.List;

public class CountryBoundariesIntegrationTest {

    @Test public void issue12() throws Exception {
        CountryBoundaries boundaries = CountryBoundaries.load(new FileInputStream("../data/boundaries360x180.ser"));
        List<String> ids = boundaries.getIds(16, 45.8);
        assertTrue(ids.contains("HR"));
    }
}
