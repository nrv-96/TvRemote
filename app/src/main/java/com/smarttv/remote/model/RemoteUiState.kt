package com.smarttv.remote.model

data class RemoteUiState(
    val discoveredDevices: List<TvDevice> = emptyList(),
    val connectedDevice: TvDevice? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val pairingCode: String = "",
    val pairingError: String? = null,
    val volumeLevel: Int = 50,
    val isMuted: Boolean = false,
    val errorMessage: String? = null,
    val showPairingScreen: Boolean = false,
    val discoveryErrorMessage: String? = null
)
