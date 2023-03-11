package com.abdurakhmanov.ridez.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abdurakhmanov.ridez.databinding.ActivityMainBinding
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.mapView.apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            attribution.enabled = false
            logo.enabled = false
            compass.enabled = false
            scalebar.enabled = false
        }

        binding.btnZoomIn.setOnClickListener {
            binding.mapView.camera.flyTo(
                CameraOptions.Builder().zoom(binding.mapView.getMapboxMap().cameraState.zoom + 1.0)
                    .build()
            )
        }
        binding.btnZoomOut.setOnClickListener {
            binding.mapView.camera.flyTo(
                CameraOptions.Builder().zoom(binding.mapView.getMapboxMap().cameraState.zoom - 1.0)
                    .build()
            )
        }
    }
}