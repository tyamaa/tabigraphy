package com.tabigraphy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

private const val MAPTILER_STYLE_ID = "streets-v2"

fun maptilerStyleUrl(apiKey: String): String =
    "https://api.maptiler.com/maps/$MAPTILER_STYLE_ID/style.json?key=$apiKey"

@Composable
fun MapScreen(apiKey: String = BuildConfig.MAPTILER_API_KEY, modifier: Modifier = Modifier) {
    val styleUrl = maptilerStyleUrl(apiKey)

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(modifier = Modifier.fillMaxSize(), baseStyle = BaseStyle.Uri(styleUrl))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "MapTiler style URL", style = MaterialTheme.typography.labelLarge)
                Text(text = styleUrl, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
