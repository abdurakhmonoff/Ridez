package com.abdurakhmanov.ridez.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.abdurakhmanov.ridez.R
import com.abdurakhmanov.ridez.data.repository.LocationRepository
import com.abdurakhmanov.ridez.data.source.local.LocationUpdate
import com.abdurakhmanov.ridez.ui.MainActivity
import com.abdurakhmanov.ridez.utils.hasLocationPermission
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundLocationService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "location_updates"
        private const val NOTIFICATION_ID = 1337
    }

    @Inject
    lateinit var locationRepository: LocationRepository
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val job: Job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> stopForegroundService()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled || !isNetworkEnabled) {
            throw Exception("Gps or network is disabled")
        }

        startLiveLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startLiveLocation() {
        val locationRequest = LocationRequest.Builder(2000)
            .setWaitForAccurateLocation(false)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.lastOrNull()?.let { location ->
                    val locationUpdate = LocationUpdate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time
                    )
                    Log.e("LocationService", "${location.latitude} : ${location.longitude}")
                    scope.launch {
                        locationRepository.insertLocationUpdate(locationUpdate)
                    }
                }
            }
        }

        if (applicationContext.hasLocationPermission()) {
            locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            throw Exception("Missing location permission")
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name_location_updates),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                val flag =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    else
                        PendingIntent.FLAG_UPDATE_CURRENT
                PendingIntent.getActivity(
                    this,
                    0, notificationIntent, flag
                )
            }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_live_location_black_24)
            .setContentTitle("Живое местоположение")
            .setContentText("Собираем данные о вашем местоположении...")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationProviderClient.removeLocationUpdates(locationCallback)
        scope.cancel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}