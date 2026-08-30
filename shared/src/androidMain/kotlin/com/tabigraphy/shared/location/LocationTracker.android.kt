package com.tabigraphy.shared.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Wraps [FusedLocationProviderClient] and [ActivityRecognitionClient] to stream location
 * samples, switching between a high-frequency interval while [MotionState.STILL] and a
 * low-frequency interval while moving to save battery.
 *
 * `context` should be an application context; the tracker is expected to live for as long as
 * the foreground service that owns it.
 */
actual class LocationTracker(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val activityRecognitionClient = ActivityRecognition.getClient(context)

    @SuppressLint("MissingPermission")
    actual fun observeLocationUpdates(): Flow<LocationUpdate> = callbackFlow {
        if (!hasRequiredPermissions()) {
            close(IllegalStateException("ACCESS_FINE_LOCATION / ACTIVITY_RECOGNITION permissions are required."))
            return@callbackFlow
        }

        val producerScope = this
        var motionState = MotionState.UNKNOWN
        var currentIntervalMillis = MOVING_INTERVAL_MILLIS

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                producerScope.trySend(
                    LocationUpdate(
                        sample = LocationSample(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestampMillis = location.time,
                            accuracyMeters = location.accuracy,
                        ),
                        motionState = motionState,
                        samplingIntervalMillis = currentIntervalMillis,
                    ),
                )
            }
        }

        fun requestLocationUpdates(intervalMillis: Long) {
            currentIntervalMillis = intervalMillis
            fusedLocationClient.removeLocationUpdates(locationCallback)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis).build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, context.mainLooper)
        }

        val activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (!ActivityRecognitionResult.hasResult(intent)) return
                val result = ActivityRecognitionResult.extractResult(intent) ?: return
                val newState = result.mostProbableActivity.toMotionStateOrNull() ?: return
                if (newState == motionState) return
                motionState = newState
                requestLocationUpdates(newState.samplingIntervalMillis())
            }
        }
        ContextCompat.registerReceiver(
            context,
            activityReceiver,
            IntentFilter(ACTION_ACTIVITY_UPDATE),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        val activityPendingIntent = PendingIntent.getBroadcast(
            context,
            ACTIVITY_UPDATE_REQUEST_CODE,
            Intent(ACTION_ACTIVITY_UPDATE).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL_MILLIS, activityPendingIntent)
        requestLocationUpdates(currentIntervalMillis)

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            activityRecognitionClient.removeActivityUpdates(activityPendingIntent)
            context.unregisterReceiver(activityReceiver)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val activityRecognitionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted && activityRecognitionGranted
    }

    private fun MotionState.samplingIntervalMillis(): Long = when (this) {
        MotionState.STILL -> STILL_INTERVAL_MILLIS
        MotionState.MOVING, MotionState.UNKNOWN -> MOVING_INTERVAL_MILLIS
    }

    private companion object {
        const val ACTION_ACTIVITY_UPDATE = "com.tabigraphy.shared.location.ACTION_ACTIVITY_UPDATE"
        const val ACTIVITY_UPDATE_REQUEST_CODE = 1001

        // Google recommends not polling activity updates faster than ~30s to limit battery use;
        // reused as the STILL location interval since both represent "responsive but not wasteful".
        const val ACTIVITY_DETECTION_INTERVAL_MILLIS = 30_000L
        const val STILL_INTERVAL_MILLIS = 30_000L
        const val MOVING_INTERVAL_MILLIS = 180_000L
    }
}

private const val MIN_ACTIVITY_CONFIDENCE = 50

private fun DetectedActivity.toMotionStateOrNull(): MotionState? {
    if (confidence < MIN_ACTIVITY_CONFIDENCE) return null
    return when (type) {
        DetectedActivity.STILL -> MotionState.STILL
        DetectedActivity.WALKING,
        DetectedActivity.RUNNING,
        DetectedActivity.ON_FOOT,
        DetectedActivity.ON_BICYCLE,
        DetectedActivity.IN_VEHICLE,
        -> MotionState.MOVING
        else -> null
    }
}
