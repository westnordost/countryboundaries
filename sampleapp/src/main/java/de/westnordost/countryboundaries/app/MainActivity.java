package de.westnordost.countryboundaries.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;

import java.io.IOException;
import java.util.List;

import de.westnordost.countryboundaries.CountryBoundaries;

public class MainActivity extends Activity implements MapEventsReceiver
{
    private static final int WRITE_STORAGE_PERMISSION_REQUEST = 123;

    private MapView mapView;
    private CountryBoundaries countryBoundaries;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_PERMISSION_REQUEST);
        } else {
            onMayWriteExternalStorage();
        }

        try
		{
			countryBoundaries = CountryBoundaries.load(
					getAssets().open("boundaries.json"),
					getAssets().open("boundaries_index.json")
			);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

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
		long t = System.currentTimeMillis();
		List<String> isoCodes = countryBoundaries.getIds(p.getLongitude(), p.getLatitude());
		t = System.currentTimeMillis() - t;
        Toast.makeText(this, TextUtils.join(",",isoCodes) + " (in " + t + "ms)",
                Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) { return false; }

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
        mapView.setBuiltInZoomControls(true);
        mapView.setFlingEnabled(true);
        mapView.setTilesScaledToDpi(true);
        mapView.getOverlays().add(new CopyrightOverlay(this));
        mapView.getOverlays().add(new MapEventsOverlay(this));
        mapView.getController().setCenter(new GeoPoint(45.0,10.0));
        mapView.getController().setZoom(3);
        setContentView(mapView);
    }
}
