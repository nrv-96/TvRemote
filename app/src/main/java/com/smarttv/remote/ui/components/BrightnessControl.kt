package com.smarttv.remote.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrightnessControl(
    onBrightnessUp: () -> Unit,
    onBrightnessDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.BrightnessLow,
            contentDescription = "Brightness",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        FilledTonalButton(
            onClick = onBrightnessDown,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Filled.BrightnessLow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("-")
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalButton(
            onClick = onBrightnessUp,
            modifier = Modifier.weight(1f)
        ) {
            Text("+")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Filled.BrightnessHigh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
