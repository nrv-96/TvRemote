package com.smarttv.remote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smarttv.remote.protocol.KeyCodes

@Composable
fun NavButtons(
    onSendKey: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = { onSendKey(KeyCodes.BACK) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = { onSendKey(KeyCodes.HOME) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Home")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = { onSendKey(KeyCodes.POWER) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Power")
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = { onSendKey(KeyCodes.SETTINGS) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Settings")
            }
        }
    }
}
