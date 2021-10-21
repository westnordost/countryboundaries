package de.westnordost.countryboundaries;


import java.io.FileInputStream;
import java.util.Locale;
import java.util.Random;

public class PerformanceTest {

    public static void main(String[] args) throws Exception {
        CountryBoundaries boundaries = CountryBoundaries.load(new FileInputStream("data/boundaries360x180.ser"));
        Random random = new Random();
        int checks = 1_000_000;
        long time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            boundaries.getIds(random.nextDouble() * 360.0 - 180.0, random.nextDouble() * 180.0 - 90.0);
        }
        long timeSpent = System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            double x = random.nextDouble() * 360.0 - 180.0;
            x = random.nextDouble() * 180.0 - 90.0;
        }
        long timeSpentNotOnBoundaries = System.nanoTime() - time;

        long timeSpentOnBoundaries = timeSpent - timeSpentNotOnBoundaries;

        System.out.println(
                "Querying " + checks + " random locations took " +
                String.format(Locale.US, "%,.2f", timeSpentOnBoundaries * 1e-9) + " seconds " +
                "- so on average " +
                Math.round(timeSpentOnBoundaries * 1.0 / checks) + " nanoseconds"
        );
    }

}
