package com.tabigraphy

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.tabigraphy.location.LocationTrackingService
import com.tabigraphy.permission.PermissionScreen
import com.tabigraphy.permission.hasAllRequiredPermissions
import com.tabigraphy.permission.hasBackgroundLocationPermission
import com.tabigraphy.permission.missingCorePermissions
import com.tabigraphy.permission.requiredCorePermissions

class MainActivity : ComponentActivity() {

    private var allPermissionsGranted by mutableStateOf(false)

    private val requestCorePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (missingCorePermissions(this).isEmpty() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !hasBackgroundLocationPermission(this)
            ) {
                requestBackgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                refreshPermissionState()
            }
        }

    private val requestBackgroundPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshPermissionState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissionState()

        setContent {
            MaterialTheme {
                if (allPermissionsGranted) {
                    MapScreen()
                } else {
                    PermissionScreen(onRequestPermissions = { requestCorePermissions.launch(requiredCorePermissions()) })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    private fun refreshPermissionState() {
        val granted = hasAllRequiredPermissions(this)
        allPermissionsGranted = granted
        if (granted) {
            ContextCompat.startForegroundService(this, Intent(this, LocationTrackingService::class.java))
        }
    }
}
