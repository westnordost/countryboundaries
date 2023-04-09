package de.westnordost.countryboundaries;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Random;

public class PerformanceTest {

    public static void main(String[] args) throws Exception {
        long time = System.nanoTime();
        byte[] bytes = Files.readAllBytes(new File("data/boundaries360x180.ser").toPath());
        CountryBoundaries boundaries = CountryBoundaries.load(new ByteArrayInputStream(bytes));
        long timeSpentOnLoading = System.nanoTime() - time;
        System.out.println(
                "Loading data took " +
                String.format(Locale.US, "%,.2f", timeSpentOnLoading * 1e-9) +
                " seconds"
        );

        Random random = new Random();
        int checks = 10_000_000;

        time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            boundaries.getIds(random.nextDouble() * 360.0 - 180.0, random.nextDouble() * 180.0 - 90.0);
        }
        long timeSpent = System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            double x = random.nextDouble() * 360.0 - 180.0 + random.nextDouble() * 180.0 - 90.0;
        }
        long timeSpentOnRandom = System.nanoTime() - time;
        long timeSpentOnBoundaries = timeSpent - timeSpentOnRandom;

        System.out.println(
                "Querying " + checks + " random locations took " +
                String.format(Locale.US, "%,.2f", timeSpentOnBoundaries * 1e-9) + " seconds " +
                "- so on average " +
                Math.round(timeSpentOnBoundaries * 1.0 / checks) + " nanoseconds"
        );
    }

}
