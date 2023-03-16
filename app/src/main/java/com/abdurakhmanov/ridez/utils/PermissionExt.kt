package com.abdurakhmanov.ridez.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Checks whether user granted location permission or not.
 *
 * @return true if location permission is granted or false if the permission is not granted
 */
fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Checks whether user granted notification permission or not.
 *
 * @return true if notification permission is granted or false if the permission is not granted
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Context.hasNotificationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Checks whether user granted the permission or not after runtime permission request.
 *
 * @return true if permission is granted or false if permission is not granted
 */
fun String.isPermissionGranted(
    grantPermissions: Array<String>,
    grantResults: IntArray
): Boolean {
    for (i in grantPermissions.indices) {
        if (this == grantPermissions[i]) {
            return grantResults[i] == PackageManager.PERMISSION_GRANTED
        }
    }
    return false
}

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
 * Shows explanation dialog about location usage.
 */
fun Context.askLocationPermissionDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Требуется разрешение на местоположение")
        .setMessage("Вам нужно принять разрешение на использование местоположения, чтобы использовать Ridez.")
        .setPositiveButton("Ладно") { _, _ ->
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            })
        }
        .setNegativeButton("Нет", null)
        .show()
}

/**
 * Shows up when location is disabled
 */
fun Context.locationIsDisabledDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Включите местоположение")
        .setMessage("Вам нужно включить местоположение, чтобы использовать Ridez.")
        .setPositiveButton("Понял", null)
        .show()
}

/**
 * Shows explanation dialog about notifications.
 */
fun Context.askNotificationPermissionDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Разрешение на уведомления")
        .setMessage("Мы используем уведомления, чтобы информировать вас о том, что ваше текущее местоположение отслеживается.")
        .setPositiveButton("Ладно") { _, _ ->
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            })
        }
        .setNegativeButton("Не надо", null)
        .show()
}