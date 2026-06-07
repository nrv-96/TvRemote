package com.smarttv.remote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartTv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttv.remote.model.ConnectionState
import com.smarttv.remote.model.RemoteUiState
import com.smarttv.remote.model.TvDevice

@Composable
fun DiscoveryScreen(
    state: RemoteUiState,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onConnectToDevice: (TvDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    var showDisconnectDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart TV Remote",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )

        Text(
            text = "Find your Android TV on the network",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.discoveryErrorMessage != null) {
            Text(
                text = state.discoveryErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (state.connectionState == ConnectionState.Discovering) {
                    onStopDiscovery()
                } else {
                    onStartDiscovery()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.connectionState == ConnectionState.Discovering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanning...")
            } else {
                Icon(Icons.Filled.Wifi, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Find TVs")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.discoveredDevices.isEmpty() && state.connectionState == ConnectionState.Discovering) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Searching for Android TVs...\nMake sure your TV is on and connected to the same WiFi network.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else if (state.discoveredDevices.isEmpty() && state.connectionState != ConnectionState.Discovering) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.SmartTv,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No TVs found.\nTap \"Find TVs\" to scan your network.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            Text(
                text = "${state.discoveredDevices.size} TV${if (state.discoveredDevices.size != 1) "s" else ""} found",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.discoveredDevices) { device ->
                    TvDeviceCard(
                        device = device,
                        isConnecting = state.connectionState == ConnectionState.Connecting &&
                                state.connectedDevice?.host == device.host,
                        onConnect = { onConnectToDevice(device) }
                    )
                }
            }
        }

        if (state.connectedDevice != null) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { showDisconnectDialog = true }) {
                Text("Disconnect from ${state.connectedDevice.name}")
            }
        }
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect") },
            text = { Text("Disconnect from ${state.connectedDevice?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDisconnectDialog = false
                    onDisconnect()
                }) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TvDeviceCard(
    device: TvDevice,
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Devices,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = device.host,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (device.isPaired) {
                    Text(
                        text = "Paired",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = onConnect,
                enabled = !isConnecting
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Connect")
                }
            }
        }
    }
}
