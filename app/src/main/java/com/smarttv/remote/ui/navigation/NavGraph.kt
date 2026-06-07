package com.smarttv.remote.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smarttv.remote.model.ConnectionState
import com.smarttv.remote.ui.screens.DiscoveryScreen
import com.smarttv.remote.ui.screens.PairingScreen
import com.smarttv.remote.ui.screens.RemoteScreen
import com.smarttv.remote.viewmodel.RemoteViewModel

object Routes {
    const val DISCOVERY = "discovery"
    const val PAIRING = "pairing"
    const val REMOTE = "remote"
}

@Composable
fun TvRemoteNavGraph(
    viewModel: RemoteViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.connectionState, state.showPairingScreen) {
        when (state.connectionState) {
            ConnectionState.Pairing -> {
                if (navController.currentDestination?.route != Routes.PAIRING) {
                    navController.navigate(Routes.PAIRING) {
                        launchSingleTop = true
                    }
                }
            }
            ConnectionState.Connected -> {
                if (navController.currentDestination?.route != Routes.REMOTE) {
                    navController.navigate(Routes.REMOTE) {
                        popUpTo(Routes.DISCOVERY)
                    }
                }
            }
            ConnectionState.Disconnected -> {
                if (navController.currentDestination?.route != Routes.DISCOVERY) {
                    navController.popBackStack(Routes.DISCOVERY, false)
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.DISCOVERY
    ) {
        composable(Routes.DISCOVERY) {
            DiscoveryScreen(
                state = state,
                onStartDiscovery = { viewModel.startDiscovery() },
                onStopDiscovery = { viewModel.stopDiscovery() },
                onConnectToDevice = { device ->
                    viewModel.connectToDevice(device)
                },
                onDisconnect = { viewModel.disconnect() }
            )
        }
        composable(Routes.PAIRING) {
            PairingScreen(
                state = state,
                onPairingCodeChanged = { code ->
                    viewModel.submitPairingCode(code)
                },
                onBack = {
                    viewModel.disconnect()
                }
            )
        }
        composable(Routes.REMOTE) {
            RemoteScreen(
                state = state,
                onSendKey = { keyCode -> viewModel.sendKey(keyCode) },
                onSendKeyLongPress = { keyCode -> viewModel.sendKeyWithLongPress(keyCode) },
                onSetVolume = { level -> viewModel.setVolume(level) },
                onAdjustVolume = { delta -> viewModel.adjustVolume(delta) },
                onToggleMute = { viewModel.toggleMute() },
                onDisconnect = {
                    viewModel.disconnect()
                }
            )
        }
    }
}
