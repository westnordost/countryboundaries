package de.westnordost.countryboundaries;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class SerializationJavaTest {

    @Test
    public void serialization_to_and_from_file_of_real_data_works_with_java_api() throws IOException {
        // get the data first...
        File file = new File("src/commonTest/resources/boundaries180x90.ser");
        CountryBoundaries boundaries;
        try(InputStream in = new FileInputStream(file)) {
            boundaries = CountryBoundariesUtils.deserializeFrom(in);
        }

        // serialize to file again...
        File file2 = new File("src/commonTest/resources/boundariesTemp.ser");
        try {
            try (FileOutputStream fos = new FileOutputStream(file2)) {
                CountryBoundariesUtils.serializeTo(fos, boundaries);
            }

            // and now deserialize again...
            CountryBoundaries boundaries2;
            try (FileInputStream in = new FileInputStream(file2)) {
                boundaries2 = CountryBoundariesUtils.deserializeFrom(in);
            }

            assertEquals(
                    boundaries,
                    boundaries2
            );
        } finally {
            file2.delete();
        }
    }
}

