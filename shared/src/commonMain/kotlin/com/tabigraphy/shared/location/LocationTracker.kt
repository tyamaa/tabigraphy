package com.tabigraphy.shared.location

import kotlinx.coroutines.flow.Flow

/**
 * Streams [LocationUpdate]s, sampling more frequently while the device is still and less
 * frequently while moving. The platform actual is responsible for permission checks, activity
 * recognition, and picking the concrete sampling intervals.
 */
expect class LocationTracker {
    fun observeLocationUpdates(): Flow<LocationUpdate>
}
