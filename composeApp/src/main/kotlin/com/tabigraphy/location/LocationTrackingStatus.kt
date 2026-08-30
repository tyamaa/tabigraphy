package com.tabigraphy.location

import com.tabigraphy.shared.location.LocationUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-process bridge between [LocationTrackingService] (the producer) and the debug UI in
 * MapScreen (the consumer). Both run in the same process, so a plain singleton StateFlow is
 * enough; no persistence or cross-process IPC is needed for this task.
 */
object LocationTrackingStatus {

    data class State(
        val receivedCount: Int = 0,
        val lastUpdate: LocationUpdate? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun onUpdate(update: LocationUpdate) {
        _state.update { current ->
            current.copy(receivedCount = current.receivedCount + 1, lastUpdate = update)
        }
    }
}
