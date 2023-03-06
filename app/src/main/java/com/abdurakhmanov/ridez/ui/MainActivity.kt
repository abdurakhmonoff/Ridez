package com.abdurakhmanov.ridez.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abdurakhmanov.ridez.databinding.ActivityMainBinding
import com.mapbox.maps.Style

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
    }
}