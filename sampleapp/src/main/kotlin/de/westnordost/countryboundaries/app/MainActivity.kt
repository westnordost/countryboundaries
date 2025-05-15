package de.westnordost.countryboundaries.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.deserializeFrom
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap.OnMapClickListener
import org.maplibre.android.maps.MapView
import kotlin.math.round
import kotlin.random.Random

class MainActivity : Activity(), OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var countryBoundaries: CountryBoundaries

    private lateinit var resultText: TextView
    private lateinit var performanceTestButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.activity_main)

        resultText = findViewById(R.id.resultText)
        mapView = findViewById(R.id.mapView)
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle("https://demotiles.maplibre.org/style.json")
            mapLibreMap.addOnMapClickListener(this)
        }
        performanceTestButton = findViewById(R.id.performanceTestButton)
        performanceTestButton.setOnClickListener { startPerformanceTest() }


        var t = System.currentTimeMillis()
        countryBoundaries = deserializeFrom(assets.open("boundaries180x90.ser"))
        t = System.currentTimeMillis() - t

        resultText.text = "Loading took $t ms"
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onMapClick(latLng: LatLng): Boolean {
        var t = System.nanoTime()
        val ids = countryBoundaries.getIds(latLng.longitude, latLng.latitude)
        t = System.nanoTime() - t

        val idsString = if (ids.isEmpty()) "is nowhere" else "is in ${ids.joinToString()}"
        resultText.text =
            "${"%.5f".format(latLng.latitude)}${"%.5f".format(latLng.longitude)} " +
            idsString +
            " (in ${"%.2f".format(t * 1e-6)}ms)"
        return true
    }

    private fun startPerformanceTest() {
        // jupp, do it on the main thread, so user can't do anything :-P

        val random = Random.Default
        val checks = 1000000
        var time = System.nanoTime()
        repeat (checks) {
            countryBoundaries.getIds(random.nextDouble() * 360.0 - 180.0, random.nextDouble() * 180.0 - 90.0)
        }
        val timeSpent = System.nanoTime() - time

        time = System.nanoTime()
        repeat (checks) {
            var x = random.nextDouble() * 360.0 - 180.0
            x = random.nextDouble() * 180.0 - 90.0
        }
        val timeSpentNotOnBoundaries = System.nanoTime() - time

        val timeSpentOnBoundaries = timeSpent - timeSpentNotOnBoundaries

        resultText.text =
            "Querying $checks random locations took ${"%,.2f".format(timeSpentOnBoundaries * 1e-9)}" +
            " seconds - so on average " +
            round(timeSpentOnBoundaries * 1.0 / checks) + " nanoseconds"
    }
}
