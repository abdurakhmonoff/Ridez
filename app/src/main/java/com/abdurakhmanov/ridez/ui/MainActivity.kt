package com.abdurakhmanov.ridez.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.abdurakhmanov.ridez.R
import com.abdurakhmanov.ridez.databinding.ActivityMainBinding
import com.abdurakhmanov.ridez.services.ForegroundLocationService
import com.abdurakhmanov.ridez.utils.*
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 234
        private const val LOCATION_PERMISSION_STARTUP_REQUEST_CODE = 456
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 345
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationComponentPlugin: LocationComponentPlugin

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mapboxMap = binding.mapView.getMapboxMap()

        binding.btnZoomIn.setOnClickListener {
            binding.mapView.camera.flyTo(
                CameraOptions.Builder().zoom(mapboxMap.cameraState.zoom + 1.0)
                    .build()
            )
        }
        binding.btnZoomOut.setOnClickListener {
            binding.mapView.camera.flyTo(
                CameraOptions.Builder().zoom(mapboxMap.cameraState.zoom - 1.0)
                    .build()
            )
        }
        binding.btnTrackLocation.setOnClickListener {
            if (hasLocationPermission()) {
                onCameraTrackingStarted()
                binding.mapView.camera.flyTo(
                    CameraOptions.Builder()
                        .zoom(if (mapboxMap.cameraState.zoom <= 12.0) 14.0 else null)
                        .build()
                )
            }
        }
        binding.btnLiveLocation.setOnClickListener {
            if (!hasLocationPermission()) {
                requestLocationPermissions(false)
            } else if (!isLocationEnabled()) {
                locationIsDisabledDialog()
            } else if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
                requestNotificationPermission()
            } else {
                binding.btnLiveLocation.apply {
                    if (isExtended) {
                        startLocationService()
                        binding.currentAddress.visibility = View.VISIBLE
                        showCurrentAddress()
                        iconTint = ColorStateList.valueOf(
                            ContextCompat.getColor(this@MainActivity, R.color.aqua_green)
                        )
                        shrink()
                    } else {
                        stopLocationService()
                        binding.currentAddress.visibility = View.INVISIBLE
                        iconTint =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(this@MainActivity, R.color.valencia_red)
                            )
                        setTextColor(
                            ContextCompat.getColor(this@MainActivity, R.color.valencia_red)
                        )
                        extend()
                    }
                }
            }
        }

        if (hasLocationPermission()) {
            onMapReady()
        } else {
            requestLocationPermissions(true)
            onMapReady()
        }
    }

    private fun onMapReady() {
        binding.mapView.apply {
            getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .zoom(14.0)
                    .build()
            )
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    getMapboxMap().loadStyleUri(
                        Style.MAPBOX_STREETS
                    ) {
                        initLocationComponent()
                        setupGesturesListener()
                    }
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    getMapboxMap().loadStyleUri(
                        Style.DARK
                    ) {
                        initLocationComponent()
                        setupGesturesListener()
                    }
                }
            }
            attribution.enabled = false
            logo.enabled = false
            compass.enabled = false
            scalebar.enabled = false
        }
    }

    private fun startLocationService() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this,
                    ForegroundLocationService::class.java
                ).apply {
                    action = ForegroundLocationService.ACTION_START
                })
        } else {
            startService(Intent(this, ForegroundLocationService::class.java).apply {
                action = ForegroundLocationService.ACTION_START
            })
        }
        viewModel.showLiveLocation(true)
    }

    private fun stopLocationService() {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this,
                    ForegroundLocationService::class.java
                ).apply {
                    action = ForegroundLocationService.ACTION_STOP
                })
        } else {
            startService(Intent(this, ForegroundLocationService::class.java).apply {
                action = ForegroundLocationService.ACTION_STOP
            })
        }
        viewModel.showLiveLocation(false)
    }

    private fun showCurrentAddress() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.liveLocation.collect { location ->
                    location?.let {
                        viewModel.getCurrentAddress(
                            GEOCODER_API_KEY,
                            location.latitude,
                            location.longitude
                        )
                    }
                }
                viewModel.currentAddress.collect { currentAddress ->
                    if (currentAddress != null) {
                        binding.currentAddress.visibility = View.VISIBLE
                        binding.currentAddress.text = currentAddress.postaladdress
                    } else {
                        binding.currentAddress.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private fun setupGesturesListener() {
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        locationComponentPlugin = binding.mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_taxi_48,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }

        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    private fun onCameraTrackingStarted() {
        binding.mapView.location
            .addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.location
            .addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun onCameraTrackingDismissed() {
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun requestLocationPermissions(appStartup: Boolean) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            askLocationPermissionDialog()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            if (appStartup) LOCATION_PERMISSION_STARTUP_REQUEST_CODE else LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestNotificationPermission() {
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ), NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE or LOCATION_PERMISSION_STARTUP_REQUEST_CODE -> {
                val coarseLocationGranted =
                    Manifest.permission.ACCESS_COARSE_LOCATION.isPermissionGranted(
                        permissions,
                        grantResults
                    )
                val fineLocationGranted =
                    Manifest.permission.ACCESS_FINE_LOCATION.isPermissionGranted(
                        permissions,
                        grantResults
                    )

                if (requestCode == LOCATION_PERMISSION_STARTUP_REQUEST_CODE) {
                    if (coarseLocationGranted || fineLocationGranted) {
                        onMapReady()
                    } else {
                        requestLocationPermissions(true)
                    }
                } else {
                    if (!coarseLocationGranted && !fineLocationGranted) {
                        askLocationPermissionDialog()
                    }
                }
            }
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    val postNotificationGranted =
                        Manifest.permission.POST_NOTIFICATIONS.isPermissionGranted(
                            permissions,
                            grantResults
                        )
                    if (!postNotificationGranted) {
                        askNotificationPermissionDialog()
                    } else {
                        binding.btnLiveLocation.callOnClick()
                    }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        binding.mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        binding.mapView.gestures.removeOnMoveListener(onMoveListener)
    }
}