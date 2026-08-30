package com.tabigraphy.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Permissions requestable together in a single system dialog: fine location, activity
 * recognition, and (from API 33) the notification permission needed to show the foreground
 * service's notification.
 */
fun requiredCorePermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACTIVITY_RECOGNITION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

fun missingCorePermissions(context: Context): List<String> =
    requiredCorePermissions().filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

/**
 * Background location must be requested in its own dialog after foreground location is
 * granted; Android 11+ silently rejects requesting it alongside other permissions.
 */
fun hasBackgroundLocationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

fun hasAllRequiredPermissions(context: Context): Boolean =
    missingCorePermissions(context).isEmpty() && hasBackgroundLocationPermission(context)
