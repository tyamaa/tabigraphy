package com.tabigraphy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tabigraphy.location.LocationTrackingStatus
import com.tabigraphy.shared.location.MotionState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

private const val MAPTILER_STYLE_ID = "streets-v2"

fun maptilerStyleUrl(apiKey: String): String =
    "https://api.maptiler.com/maps/$MAPTILER_STYLE_ID/style.json?key=$apiKey"

private fun MotionState.debugLabel(): String = when (this) {
    MotionState.STILL -> "静止中"
    MotionState.MOVING -> "移動中"
    MotionState.UNKNOWN -> "不明"
}

@Composable
fun MapScreen(apiKey: String = BuildConfig.MAPTILER_API_KEY, modifier: Modifier = Modifier) {
    val styleUrl = maptilerStyleUrl(apiKey)
    val trackingState by LocationTrackingStatus.state.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(modifier = Modifier.fillMaxSize(), baseStyle = BaseStyle.Uri(styleUrl))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "MapTiler style URL", style = MaterialTheme.typography.labelLarge)
                Text(text = styleUrl, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "位置情報デバッグ表示", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "受信件数: ${trackingState.receivedCount}",
                    style = MaterialTheme.typography.bodySmall,
                )
                val lastUpdate = trackingState.lastUpdate
                Text(
                    text = if (lastUpdate != null) {
                        "モード: ${lastUpdate.motionState.debugLabel()} " +
                            "(サンプリング間隔: ${lastUpdate.samplingIntervalMillis / 1000}秒)"
                    } else {
                        "モード: 受信待ち"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
