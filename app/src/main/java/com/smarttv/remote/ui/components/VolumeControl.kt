package com.smarttv.remote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VolumeControl(
    volumeLevel: Int,
    isMuted: Boolean,
    onVolumeChanged: (Int) -> Unit,
    onVolumeStep: (Int) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeDown,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Slider(
                value = if (isMuted) 0f else volumeLevel.toFloat(),
                onValueChange = { onVolumeChanged(it.toInt()) },
                valueRange = 0f..100f,
                steps = 99,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (isMuted) "M" else "$volumeLevel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            IconButton(onClick = { onVolumeStep(10) }) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Volume up",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
