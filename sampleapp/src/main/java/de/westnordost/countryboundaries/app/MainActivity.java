package de.westnordost.countryboundaries.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import de.westnordost.countryboundaries.CountryBoundariesKt;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.westnordost.countryboundaries.CountryBoundaries;
import org.maplibre.android.MapLibre;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;

public class MainActivity extends Activity implements MapLibreMap.OnMapClickListener {

    private MapView mapView;
    private CountryBoundaries countryBoundaries;

    private TextView resultText;
    private Button performanceTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(mapLibreMap -> {
            mapLibreMap.setStyle("https://demotiles.maplibre.org/style.json");
            mapLibreMap.addOnMapClickListener(this);
            //mapLibreMap.cameraPosition = CameraPosition.Builder().target(LatLng(0.0,0.0)).zoom(1.0).build();
        });

        performanceTestButton = findViewById(R.id.performanceTestButton);
        performanceTestButton.setOnClickListener(v -> startPerformanceTest());

        try {
			long t = System.currentTimeMillis();

			countryBoundaries = CountryBoundariesKt.deserializeFrom(getAssets().open("boundaries180x90.ser"));

			t = System.currentTimeMillis() - t;

            resultText.setText("Loading took " + t + "ms");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    @Override protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMapClick(LatLng latLng) {
        long t = System.nanoTime();
        List<String> ids = countryBoundaries.getIds(latLng.getLongitude(), latLng.getLatitude());
        t = System.nanoTime() - t;
        resultText.setText(
                String.format(Locale.US, "%,.5f, %,.5f\n", latLng.getLatitude(), latLng.getLongitude()) +
                        getToastString(ids) +
                        " (in " + String.format(Locale.US, "%,.2f", t*1e-6) + "ms)"
        );
        return true;
    }

    private void startPerformanceTest() {
        // jupp, do it on the main thread, so user can't do anything :-P

        Random random = new Random();
        int checks = 1_000_000;
        long time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            countryBoundaries.getIds(random.nextDouble() * 360.0 - 180.0, random.nextDouble() * 180.0 - 90.0);
        }
        long timeSpent = System.nanoTime() - time;

        time = System.nanoTime();
        for (int i = 0; i < checks; i++) {
            double x = random.nextDouble() * 360.0 - 180.0;
            x = random.nextDouble() * 180.0 - 90.0;
        }
        long timeSpentNotOnBoundaries = System.nanoTime() - time;

        long timeSpentOnBoundaries = timeSpent - timeSpentNotOnBoundaries;

        resultText.setText(
            "Querying " + checks + " random locations took " +
                    String.format(Locale.US, "%,.2f", timeSpentOnBoundaries * 1e-9) + " seconds " +
                    "- so on average " +
                    Math.round(timeSpentOnBoundaries * 1.0 / checks) + " nanoseconds"
        );
    }

	private static String getToastString(List<String> ids)
	{
		if(ids.isEmpty())
		{
			return "is nowhere";
		}
		return "is in " + TextUtils.join(", ",ids);
	}
}
