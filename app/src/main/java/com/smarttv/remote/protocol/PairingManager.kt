package com.smarttv.remote.protocol

import com.smarttv.remote.util.FileLogger
import kotlinx.coroutines.delay

class PairingManager(
    private val protocol: RemoteProtocol
) {
    companion object {
        private const val TAG = "PairingManager"
        private const val MAX_PAIRING_RETRIES = 3
    }

    suspend fun performPairing(code: String): Result<Boolean> {
        try {
            FileLogger.i(TAG, "Starting pairing with code: $code")

            for (attempt in 1..MAX_PAIRING_RETRIES) {
                try {
                    val result = protocol.sendPairingCode(code)
                    if (result.isFailure) {
                        FileLogger.w(TAG, "Pairing attempt $attempt failed: ${result.exceptionOrNull()?.message}")
                        if (attempt < MAX_PAIRING_RETRIES) {
                            delay(1000)
                        }
                        continue
                    }

                    delay(500)

                    val response = protocol.readMessage()
                    if (response.isSuccess && response.getOrNull() != null) {
                        val msg = response.getOrNull()!!
                        FileLogger.i(TAG, "Received response during pairing: type=${msg.messageTypeCase}")
                    }

                    FileLogger.i(TAG, "Pairing completed successfully")
                    return Result.success(true)

                } catch (e: Exception) {
                    FileLogger.w(TAG, "Pairing attempt $attempt threw exception", e)
                    if (attempt < MAX_PAIRING_RETRIES) {
                        delay(1000)
                    } else {
                        throw e
                    }
                }
            }

            return Result.failure(Exception("Pairing failed after $MAX_PAIRING_RETRIES attempts"))
        } catch (e: Exception) {
            FileLogger.e(TAG, "Pairing failed", e)
            return Result.failure(e)
        }
    }

    suspend fun initializeSession(): Result<Boolean> {
        try {
            FileLogger.i(TAG, "Initializing remote session")

            val configureResult = protocol.sendConfigure()
            if (configureResult.isFailure) {
                FileLogger.e(TAG, "Failed to send configure")
                return Result.failure(configureResult.exceptionOrNull()!!)
            }
            delay(200)

            val activeResult = protocol.setActive(true)
            if (activeResult.isFailure) {
                FileLogger.e(TAG, "Failed to set active")
                return Result.failure(activeResult.exceptionOrNull()!!)
            }
            delay(200)

            FileLogger.i(TAG, "Session initialized successfully")
            return Result.success(true)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Session initialization failed", e)
            return Result.failure(e)
        }
    }
}
