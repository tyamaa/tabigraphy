package com.tabigraphy.shared.location

/** A single point sample from the device's location provider. */
data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long,
    val accuracyMeters: Float,
)

/** Coarse motion state derived from Activity Recognition, used to pick the sampling interval. */
enum class MotionState {
    STILL,
    MOVING,
    UNKNOWN,
}

/** A [LocationSample] paired with the motion state and interval that produced it. */
data class LocationUpdate(
    val sample: LocationSample,
    val motionState: MotionState,
    val samplingIntervalMillis: Long,
)
