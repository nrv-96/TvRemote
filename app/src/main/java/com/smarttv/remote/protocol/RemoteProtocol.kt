package com.smarttv.remote.protocol

import com.smarttv.remote.proto.RemoteAdjustVolumeLevel
import com.smarttv.remote.proto.RemoteConfigure
import com.smarttv.remote.proto.RemoteKeyAction
import com.smarttv.remote.proto.RemoteKeyCode
import com.smarttv.remote.proto.RemoteKeyInject
import com.smarttv.remote.proto.RemoteMessage
import com.smarttv.remote.proto.RemotePairingKeyInject
import com.smarttv.remote.proto.RemoteRoleType
import com.smarttv.remote.proto.RemoteSetActive
import com.smarttv.remote.proto.RemoteSetVolumeLevel
import com.smarttv.remote.util.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RemoteProtocol(
    private val deviceName: String = "Smart TV Remote"
) {
    companion object {
        private const val TAG = "RemoteProtocol"
        private const val DEFAULT_TIMEOUT_MS = 10000
        private const val PROTOCOL_VERSION = 2
    }

    private var socket: SSLSocket? = null
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var isConnected = false
    private val random = SecureRandom()

    suspend fun connect(
        host: String,
        port: Int = 6467,
        trustAllCerts: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            FileLogger.i(TAG, "Connecting to $host:$port")

            val trustManager = if (trustAllCerts) {
                arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?, authType: String?
                    ) {}
                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?, authType: String?
                    ) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
            } else {
                null
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustManager, SecureRandom())
            val factory = sslContext.socketFactory

            val rawSocket = factory.createSocket()
            rawSocket as SSLSocket
            rawSocket.connect(InetSocketAddress(host, port), DEFAULT_TIMEOUT_MS)
            rawSocket.soTimeout = DEFAULT_TIMEOUT_MS
            rawSocket.startHandshake()

            socket = rawSocket
            inputStream = DataInputStream(rawSocket.inputStream)
            outputStream = DataOutputStream(rawSocket.outputStream)
            isConnected = true

            FileLogger.i(TAG, "Connected to $host:$port successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            isConnected = false
            FileLogger.e(TAG, "Failed to connect to $host:$port", e)
            Result.failure(e)
        }
    }

    suspend fun sendConfigure(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val deviceId = "android-${random.nextInt()}"
            val configure = RemoteConfigure.newBuilder()
                .setDeviceName(deviceName)
                .setProtocolVersion(PROTOCOL_VERSION)
                .setUnknown3(true)
                .setDeviceId(deviceId)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteConfigure(configure)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent configure (device=$deviceName, id=$deviceId)")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send configure", e)
            Result.failure(e)
        }
    }

    suspend fun setActive(active: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val setActive = RemoteSetActive.newBuilder()
                .setActive(active)
                .setRole(RemoteRoleType.ROLE_APP)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteSetActive(setActive)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent setActive=$active")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send setActive", e)
            Result.failure(e)
        }
    }

    suspend fun sendPairingCode(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val pairingMsg = RemotePairingKeyInject.newBuilder()
                .setPairingCode(code)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemotePairingKeyInject(pairingMsg)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent pairing code")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send pairing code", e)
            Result.failure(e)
        }
    }

    suspend fun sendKey(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val protoKeyCode = RemoteKeyCode.forNumber(keyCode)
                ?: return@withContext Result.failure(IllegalArgumentException("Unknown key code: $keyCode"))

            val keyInject = RemoteKeyInject.newBuilder()
                .setKeyCode(protoKeyCode)
                .setKeyAction(RemoteKeyAction.KEY_ACTION_SHORT)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteKeyInject(keyInject)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent key: $keyCode (${RemoteKeyCode.forNumber(keyCode)})")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send key $keyCode", e)
            Result.failure(e)
        }
    }

    suspend fun sendLongKeyStart(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val protoKeyCode = RemoteKeyCode.forNumber(keyCode)
                ?: return@withContext Result.failure(IllegalArgumentException("Unknown key code: $keyCode"))
            val keyInject = RemoteKeyInject.newBuilder()
                .setKeyCode(protoKeyCode)
                .setKeyAction(RemoteKeyAction.KEY_ACTION_START_LONG)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteKeyInject(keyInject)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent long key start: $keyCode")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send long key start $keyCode", e)
            Result.failure(e)
        }
    }

    suspend fun sendLongKeyEnd(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val protoKeyCode = RemoteKeyCode.forNumber(keyCode)
                ?: return@withContext Result.failure(IllegalArgumentException("Unknown key code: $keyCode"))
            val keyInject = RemoteKeyInject.newBuilder()
                .setKeyCode(protoKeyCode)
                .setKeyAction(RemoteKeyAction.KEY_ACTION_END_LONG)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteKeyInject(keyInject)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Sent long key end: $keyCode")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to send long key end $keyCode", e)
            Result.failure(e)
        }
    }

    suspend fun setVolume(level: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val clampedLevel = level.coerceIn(0, 100)
            val volumeMsg = RemoteSetVolumeLevel.newBuilder()
                .setLevel(clampedLevel)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteSetVolumeLevel(volumeMsg)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Set volume to $clampedLevel")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to set volume", e)
            Result.failure(e)
        }
    }

    suspend fun adjustVolume(delta: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val adjustMsg = RemoteAdjustVolumeLevel.newBuilder()
                .setDelta(delta)
                .build()
            val message = RemoteMessage.newBuilder()
                .setRemoteAdjustVolumeLevel(adjustMsg)
                .build()
            sendMessage(message)
            FileLogger.d(TAG, "Adjusted volume by $delta")
            Result.success(Unit)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to adjust volume", e)
            Result.failure(e)
        }
    }

    suspend fun readMessage(): Result<RemoteMessage?> = withContext(Dispatchers.IO) {
        try {
            checkConnected()
            val length = inputStream!!.readInt()
            if (length <= 0 || length > 1024 * 1024) {
                FileLogger.w(TAG, "Invalid message length: $length")
                return@withContext Result.success(null)
            }
            val data = ByteArray(length)
            inputStream!!.readFully(data)
            val message = RemoteMessage.parseFrom(data)
            FileLogger.d(TAG, "Received message type: ${describeMessage(message)}")
            Result.success(message)
        } catch (e: java.net.SocketTimeoutException) {
            Result.success(null)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to read message", e)
            Result.failure(e)
        }
    }

    fun isActive(): Boolean = isConnected && socket?.isConnected == true && !socket!!.isClosed

    suspend fun disconnect() {
        try {
            isConnected = false
            outputStream?.close()
            inputStream?.close()
            socket?.close()
            FileLogger.i(TAG, "Disconnected")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error during disconnect", e)
        } finally {
            outputStream = null
            inputStream = null
            socket = null
        }
    }

    private fun checkConnected() {
        if (!isActive()) {
            throw IllegalStateException("Not connected to TV")
        }
    }

    private fun describeMessage(msg: RemoteMessage): String = when {
        msg.hasRemoteConfigure() -> "RemoteConfigure"
        msg.hasRemoteSetActive() -> "RemoteSetActive"
        msg.hasRemoteError() -> "RemoteError"
        msg.hasRemotePingRequest() -> "RemotePingRequest"
        msg.hasRemotePingResponse() -> "RemotePingResponse"
        msg.hasRemoteKeyInject() -> "RemoteKeyInject"
        msg.hasRemoteImeKeyInject() -> "RemoteImeKeyInject"
        msg.hasRemoteImeBatchEdit() -> "RemoteImeBatchEdit"
        msg.hasRemoteImeShowRequest() -> "RemoteImeShowRequest"
        msg.hasRemoteVoiceBegin() -> "RemoteVoiceBegin"
        msg.hasRemoteVoicePayload() -> "RemoteVoicePayload"
        msg.hasRemoteVoiceEnd() -> "RemoteVoiceEnd"
        msg.hasRemoteStart() -> "RemoteStart"
        msg.hasRemoteSetVolumeLevel() -> "RemoteSetVolumeLevel"
        msg.hasRemoteAdjustVolumeLevel() -> "RemoteAdjustVolumeLevel"
        msg.hasRemoteSetPreferredAudioDevice() -> "RemoteSetPreferredAudioDevice"
        msg.hasRemoteResetPreferredAudioDevice() -> "RemoteResetPreferredAudioDevice"
        msg.hasRemoteAppLinkLaunchRequest() -> "RemoteAppLinkLaunchRequest"
        msg.hasRemotePairingKeyInject() -> "RemotePairingKeyInject"
        msg.hasRemoteRelativePointer() -> "RemoteRelativePointer"
        msg.hasRemotePointerSetPosition() -> "RemotePointerSetPosition"
        else -> "Unknown"
    }

    private fun sendMessage(message: RemoteMessage) {
        val data = message.toByteArray()
        outputStream!!.writeInt(data.size)
        outputStream!!.write(data)
        outputStream!!.flush()
    }
}
