package com.abdurakhmanov.ridez.utils

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat

/**
 * Checks whether user enabled the location or not.
 *
 * @return true if location is enabled or false if location is disabled
 */
fun Context.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

/**
 * Gets current address of the user using latitude and longitude.
 *
 * @return user's current address
 */
@Suppress("DEPRECATION")
fun Geocoder.getAddress(
    latitude: Double,
    longitude: Double,
    address: (android.location.Address?) -> Unit
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
        return
    }

    try {
        address(getFromLocation(latitude, longitude, 1)!!.firstOrNull())
    } catch (e: Exception) {
        address(null)
    }
}