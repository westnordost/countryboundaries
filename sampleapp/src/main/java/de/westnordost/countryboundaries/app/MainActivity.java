package de.westnordost.countryboundaries.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.westnordost.countryboundaries.CountryBoundaries;

public class MainActivity extends Activity implements MapEventsReceiver
{
    private static final int WRITE_STORAGE_PERMISSION_REQUEST = 123;

    private MapView mapView;
    private CountryBoundaries countryBoundaries;

    private TextView resultText;
    private FrameLayout mapContainer;
    private Button performanceTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.resultText);
        mapContainer = findViewById(R.id.mapContainer);
        performanceTestButton = findViewById(R.id.performanceTestButton);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST);
        } else {
            onMayWriteExternalStorage();
        }

        performanceTestButton.setOnClickListener(v -> startPerformanceTest());

        try
		{
			long t = System.currentTimeMillis();
			countryBoundaries = CountryBoundaries.load(getAssets().open("boundaries360x180.ser"));

			t = System.currentTimeMillis() - t;

            resultText.setText("Loading took " + t + "ms");

		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy()
	{
        super.onDestroy();
        if(mapView != null) mapView.onDetach();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p)
	{
		long t = System.nanoTime();
		List<String> ids = countryBoundaries.getIds(p.getLongitude(), p.getLatitude());
		t = System.nanoTime() - t;
        resultText.setText(getToastString(ids) + " (in " + String.format(Locale.US, "%,.2f", t*1e-6) + "ms)");
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) { return false; }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
        if(requestCode == WRITE_STORAGE_PERMISSION_REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                onMayWriteExternalStorage();
            }
        }
    }

    private void onMayWriteExternalStorage()
	{
        mapView = new MapView(this);

        mapView.setMultiTouchControls(true);
        mapView.setFlingEnabled(true);
        mapView.setTilesScaledToDpi(true);
        mapView.getOverlays().add(new CopyrightOverlay(this));
        mapView.getOverlays().add(new MapEventsOverlay(this));
        mapView.getController().setCenter(new GeoPoint(45.0,10.0));
        mapView.getController().setZoom(3.0);
        mapContainer.addView(mapView);
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
