package com.smarttv.remote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttv.remote.model.RemoteUiState
import com.smarttv.remote.protocol.KeyCodes
import com.smarttv.remote.ui.components.BrightnessControl
import com.smarttv.remote.ui.components.DPad
import com.smarttv.remote.ui.components.KeyboardDialog
import com.smarttv.remote.ui.components.NavButtons
import com.smarttv.remote.ui.components.Touchpad
import com.smarttv.remote.ui.components.VolumeControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    state: RemoteUiState,
    onSendKey: (Int) -> Unit,
    onSendKeyLongPress: (Int) -> Unit,
    onSetVolume: (Int) -> Unit,
    onAdjustVolume: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onDisconnect: () -> Unit
) {
    var showKeyboard by remember { mutableStateOf(false) }
    var showTouchpad by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDisconnectConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.connectedDevice?.name ?: "Connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showKeyboard = true }) {
                        Icon(Icons.Filled.Keyboard, contentDescription = "Keyboard")
                    }
                    IconButton(onClick = { showTouchpad = !showTouchpad }) {
                        Icon(Icons.Filled.TouchApp, contentDescription = "Touchpad")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Disconnect") },
                                onClick = {
                                    showMenu = false
                                    showDisconnectConfirm = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Apps") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Voice") },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // D-Pad
            DPad(
                onDirectionPressed = { keyCode -> onSendKey(keyCode) },
                onCenterPressed = { onSendKey(KeyCodes.DPAD_CENTER) },
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // Navigation Buttons
            NavButtons(
                onSendKey = { keyCode -> onSendKey(keyCode) }
            )

            // Volume Control
            VolumeControl(
                volumeLevel = state.volumeLevel,
                isMuted = state.isMuted,
                onVolumeChanged = { level -> onSetVolume(level) },
                onVolumeStep = { delta -> onAdjustVolume(delta) },
                onToggleMute = onToggleMute
            )

            // Brightness Control
            BrightnessControl(
                onBrightnessUp = { onSendKey(KeyCodes.BRIGHTNESS_UP) },
                onBrightnessDown = { onSendKey(KeyCodes.BRIGHTNESS_DOWN) }
            )

            // Touchpad
            if (showTouchpad) {
                Touchpad(
                    onSwipeUp = { onSendKey(KeyCodes.DPAD_UP) },
                    onSwipeDown = { onSendKey(KeyCodes.DPAD_DOWN) },
                    onSwipeLeft = { onSendKey(KeyCodes.DPAD_LEFT) },
                    onSwipeRight = { onSendKey(KeyCodes.DPAD_RIGHT) },
                    onTap = { onSendKey(KeyCodes.DPAD_CENTER) }
                )
            }

            // Extra action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledTonalButton(
                    onClick = { showKeyboard = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Keyboard,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Keyboard")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { onSendKey(KeyCodes.SEARCH) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voice")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { onSendKey(KeyCodes.APP_SWITCH) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apps")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Keyboard Dialog
    if (showKeyboard) {
        KeyboardDialog(
            onDismiss = { showKeyboard = false },
            onSendText = { text ->
                onSendKey(KeyCodes.SEARCH)
                showKeyboard = false
            }
        )
    }

    // Disconnect confirmation
    if (showDisconnectConfirm) {
        AlertDialog(
            onDismissRequest = { showDisconnectConfirm = false },
            title = { Text("Disconnect") },
            text = { Text("Disconnect from ${state.connectedDevice?.name ?: "TV"}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDisconnectConfirm = false
                    onDisconnect()
                }) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
