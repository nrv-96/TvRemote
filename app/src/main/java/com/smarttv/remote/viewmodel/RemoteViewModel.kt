package com.smarttv.remote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smarttv.remote.discovery.TvDiscoveryService
import com.smarttv.remote.model.ConnectionState
import com.smarttv.remote.model.RemoteUiState
import com.smarttv.remote.model.TvDevice
import com.smarttv.remote.protocol.KeyCodes
import com.smarttv.remote.protocol.PairingManager
import com.smarttv.remote.protocol.RemoteProtocol
import com.smarttv.remote.util.FileLogger
import com.smarttv.remote.util.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RemoteViewModel"
    }

    private val _state = MutableStateFlow(RemoteUiState())
    val state: StateFlow<RemoteUiState> = _state.asStateFlow()

    private val discoveryService = TvDiscoveryService(application)
    private var protocol: RemoteProtocol? = null
    private var pairingManager: PairingManager? = null
    private val preferencesManager = PreferencesManager(application)
    private var messageReaderJob: Job? = null
    private var reconnectJob: Job? = null

    init {
        FileLogger.d(TAG, "ViewModel initialized")
        setupDiscovery()
        tryAutoConnect()
    }

    private fun setupDiscovery() {
        viewModelScope.launch {
            try {
                discoveryService.discoveredDevices.collect { devices ->
                    _state.update { it.copy(discoveredDevices = devices, discoveryErrorMessage = null) }
                    FileLogger.d(TAG, "Discovered ${devices.size} devices")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error collecting discovered devices", e)
                _state.update { it.copy(discoveryErrorMessage = "Discovery error: ${e.message}") }
            }
        }
    }

    fun startDiscovery() {
        try {
            FileLogger.i(TAG, "Starting TV discovery")
            _state.update { it.copy(connectionState = ConnectionState.Discovering) }
            discoveryService.startDiscovery()
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to start discovery", e)
            _state.update { it.copy(
                connectionState = ConnectionState.Disconnected,
                discoveryErrorMessage = "Failed to start discovery: ${e.message}"
            )}
        }
    }

    fun stopDiscovery() {
        try {
            discoveryService.stopDiscovery()
            _state.update { it.copy(connectionState = ConnectionState.Disconnected) }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error stopping discovery", e)
        }
    }

    fun connectToDevice(device: TvDevice) {
        viewModelScope.launch {
            try {
                FileLogger.i(TAG, "Connecting to device: ${device.name} (${device.host}:${device.port})")
                _state.update { it.copy(
                    connectionState = ConnectionState.Connecting,
                    connectedDevice = device,
                    errorMessage = null
                )}

                val proto = RemoteProtocol()
                protocol = proto
                pairingManager = PairingManager(proto)

                val connectResult = proto.connect(device.host, device.port, trustAllCerts = true)
                if (connectResult.isFailure) {
                    val error = connectResult.exceptionOrNull()
                    FileLogger.e(TAG, "Connection failed", error)
                    _state.update { it.copy(
                        connectionState = ConnectionState.Disconnected,
                        errorMessage = "Connection failed: ${error?.message}",
                        connectedDevice = null
                    )}
                    return@launch
                }

                val sessionResult = pairingManager!!.initializeSession()
                if (sessionResult.isFailure) {
                    val error = sessionResult.exceptionOrNull()
                    FileLogger.e(TAG, "Session init failed", error)
                    _state.update { it.copy(
                        connectionState = ConnectionState.Disconnected,
                        errorMessage = "Session init failed: ${error?.message}",
                        connectedDevice = null
                    )}
                    protocol?.disconnect()
                    return@launch
                }

                val isPaired = preferencesManager.isDevicePaired(device.host)
                if (isPaired) {
                    _state.update { it.copy(
                        connectionState = ConnectionState.Connected,
                        showPairingScreen = false
                    )}
                    FileLogger.i(TAG, "Device already paired, connected directly")
                    startMessageReader()
                    preferencesManager.saveLastDevice(device.name, device.host, device.port)
                } else {
                    _state.update { it.copy(
                        connectionState = ConnectionState.Pairing,
                        showPairingScreen = true
                    )}
                    FileLogger.i(TAG, "Device needs pairing")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Unexpected error during connection", e)
                _state.update { it.copy(
                    connectionState = ConnectionState.Disconnected,
                    errorMessage = "Unexpected error: ${e.message}",
                    connectedDevice = null
                )}
            }
        }
    }

    fun submitPairingCode(code: String) {
        viewModelScope.launch {
            try {
                FileLogger.i(TAG, "Submitting pairing code")
                _state.update { it.copy(pairingError = null) }

                val pm = pairingManager ?: run {
                    _state.update { it.copy(pairingError = "Not connected") }
                    return@launch
                }

                val result = pm.performPairing(code)
                if (result.isSuccess) {
                    val device = _state.value.connectedDevice ?: return@launch
                    preferencesManager.markDevicePaired(device.host, "")
                    preferencesManager.saveLastDevice(device.name, device.host, device.port)
                    _state.update { it.copy(
                        connectionState = ConnectionState.Connected,
                        showPairingScreen = false,
                        pairingCode = ""
                    )}
                    FileLogger.i(TAG, "Pairing successful")
                    startMessageReader()
                } else {
                    val error = result.exceptionOrNull()
                    FileLogger.e(TAG, "Pairing failed", error)
                    _state.update { it.copy(
                        pairingError = "Pairing failed: ${error?.message}"
                    )}
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Pairing error", e)
                _state.update { it.copy(pairingError = "Error: ${e.message}") }
            }
        }
    }

    fun sendKey(keyCode: Int) {
        viewModelScope.launch {
            try {
                FileLogger.d(TAG, "Sending key: $keyCode")
                val proto = protocol ?: return@launch
                proto.sendKey(keyCode)
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to send key $keyCode", e)
            }
        }
    }

    fun sendKeyWithLongPress(keyCode: Int) {
        viewModelScope.launch {
            try {
                FileLogger.d(TAG, "Sending key with long press: $keyCode")
                val proto = protocol ?: return@launch
                proto.sendLongKeyStart(keyCode)
                delay(50)
                proto.sendLongKeyEnd(keyCode)
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to send long key $keyCode", e)
            }
        }
    }

    fun setVolume(level: Int) {
        viewModelScope.launch {
            try {
                val proto = protocol ?: return@launch
                proto.setVolume(level)
                _state.update { it.copy(volumeLevel = level) }
                FileLogger.d(TAG, "Volume set to $level")
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to set volume", e)
            }
        }
    }

    fun adjustVolume(delta: Int) {
        viewModelScope.launch {
            try {
                val proto = protocol ?: return@launch
                proto.adjustVolume(delta)
                val newLevel = (_state.value.volumeLevel + delta).coerceIn(0, 100)
                _state.update { it.copy(volumeLevel = newLevel) }
                FileLogger.d(TAG, "Volume adjusted by $delta to $newLevel")
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to adjust volume", e)
            }
        }
    }

    fun toggleMute() {
        viewModelScope.launch {
            try {
                val proto = protocol ?: return@launch
                proto.sendKey(KeyCodes.MUTE)
                _state.update { it.copy(isMuted = !it.isMuted) }
                FileLogger.d(TAG, "Mute toggled: ${_state.value.isMuted}")
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to toggle mute", e)
            }
        }
    }

    fun disconnect() {
        try {
            messageReaderJob?.cancel()
            reconnectJob?.cancel()
            viewModelScope.launch {
                protocol?.disconnect()
                protocol = null
                pairingManager = null
                _state.update { it.copy(
                    connectionState = ConnectionState.Disconnected,
                    connectedDevice = null,
                    volumeLevel = 50,
                    isMuted = false,
                    showPairingScreen = false
                )}
                FileLogger.i(TAG, "Disconnected from device")
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error during disconnect", e)
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null, pairingError = null) }
    }

    fun returnToDiscovery() {
        try {
            disconnect()
            startDiscovery()
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error returning to discovery", e)
        }
    }

    private fun tryAutoConnect() {
        try {
            val lastHost = preferencesManager.getLastDeviceHost() ?: return
            val lastName = preferencesManager.getLastDeviceName() ?: "Unknown TV"
            val lastPort = preferencesManager.getLastDevicePort()

            if (preferencesManager.isDevicePaired(lastHost)) {
                FileLogger.i(TAG, "Attempting auto-connect to $lastName ($lastHost)")
                val device = TvDevice(
                    id = lastHost,
                    name = lastName,
                    host = lastHost,
                    port = lastPort,
                    isPaired = true
                )
                connectToDevice(device)
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Auto-connect failed", e)
        }
    }

    private fun startMessageReader() {
        messageReaderJob?.cancel()
        messageReaderJob = viewModelScope.launch {
            try {
                while (isActive) {
                    val proto = protocol ?: break
                    if (!proto.isActive()) {
                        FileLogger.w(TAG, "Connection lost during message read")
                        _state.update { it.copy(
                            connectionState = ConnectionState.Disconnected,
                            errorMessage = "Connection lost"
                        )}
                        break
                    }
                    val result = proto.readMessage()
                    if (result.isSuccess) {
                        val msg = result.getOrNull()
                        if (msg != null) {
                            handleServerMessage(msg)
                        }
                    }
                    delay(100)
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Message reader stopped", e)
            }
        }
    }

    private fun handleServerMessage(msg: com.smarttv.remote.proto.RemoteMessage) {
        try {
            when (msg.messageTypeCase) {
                com.smarttv.remote.proto.RemoteMessage.MessageTypeCase.REMOTE_SET_VOLUME_LEVEL -> {
                    val level = msg.remoteSetVolumeLevel.level
                    _state.update { it.copy(volumeLevel = level) }
                    FileLogger.d(TAG, "TV reported volume: $level")
                }
                com.smarttv.remote.proto.RemoteMessage.MessageTypeCase.REMOTE_START -> {
                    val name = msg.remoteStart.name
                    FileLogger.i(TAG, "TV session started: $name")
                }
                com.smarttv.remote.proto.RemoteMessage.MessageTypeCase.REMOTE_ERROR -> {
                    val errorMsg = msg.remoteError.errorMessage
                    val errorCode = msg.remoteError.errorCode
                    FileLogger.e(TAG, "TV error: code=$errorCode, message=$errorMsg")
                    _state.update { it.copy(errorMessage = "TV error: $errorMsg") }
                }
                else -> {
                    FileLogger.d(TAG, "Unhandled message type: ${msg.messageTypeCase}")
                }
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error handling server message", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            messageReaderJob?.cancel()
            reconnectJob?.cancel()
            discoveryService.destroy()
            viewModelScope.launch {
                protocol?.disconnect()
            }
            FileLogger.d(TAG, "ViewModel cleared")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error in onCleared", e)
        }
    }
}
