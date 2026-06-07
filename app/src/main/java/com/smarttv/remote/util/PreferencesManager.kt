package com.smarttv.remote.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "tv_remote_prefs"
        private const val KEY_LAST_DEVICE_NAME = "last_device_name"
        private const val KEY_LAST_DEVICE_HOST = "last_device_host"
        private const val KEY_LAST_DEVICE_PORT = "last_device_port"
        private const val KEY_CERT_PREFIX = "cert_"
        private const val KEY_PAIRED_PREFIX = "paired_"
        private const val KEYSTORE_ALIAS = "tv_remote_cert_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TAG = "PreferencesManager"

        private const val PREFS_CERT = "tv_remote_certificates"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val certPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_CERT, Context.MODE_PRIVATE)

    fun saveLastDevice(name: String, host: String, port: Int) {
        try {
            prefs.edit()
                .putString(KEY_LAST_DEVICE_NAME, name)
                .putString(KEY_LAST_DEVICE_HOST, host)
                .putInt(KEY_LAST_DEVICE_PORT, port)
                .apply()
            FileLogger.d(TAG, "Saved last device: $name @ $host:$port")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to save last device", e)
        }
    }

    fun getLastDeviceName(): String? {
        return try {
            prefs.getString(KEY_LAST_DEVICE_NAME, null)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get last device name", e)
            null
        }
    }

    fun getLastDeviceHost(): String? {
        return try {
            prefs.getString(KEY_LAST_DEVICE_HOST, null)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get last device host", e)
            null
        }
    }

    fun getLastDevicePort(): Int {
        return try {
            prefs.getInt(KEY_LAST_DEVICE_PORT, 6467)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get last device port", e)
            6467
        }
    }

    fun clearLastDevice() {
        try {
            prefs.edit()
                .remove(KEY_LAST_DEVICE_NAME)
                .remove(KEY_LAST_DEVICE_HOST)
                .remove(KEY_LAST_DEVICE_PORT)
                .apply()
            FileLogger.d(TAG, "Cleared last device")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to clear last device", e)
        }
    }

    fun isDevicePaired(host: String): Boolean {
        return try {
            certPrefs.getBoolean(KEY_PAIRED_PREFIX + host, false)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to check paired status", e)
            false
        }
    }

    fun markDevicePaired(host: String, certBase64: String) {
        try {
            certPrefs.edit()
                .putBoolean(KEY_PAIRED_PREFIX + host, true)
                .putString(KEY_CERT_PREFIX + host, certBase64)
                .apply()
            FileLogger.d(TAG, "Marked device as paired: $host")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to mark device paired", e)
        }
    }

    fun removeDevicePairing(host: String) {
        try {
            certPrefs.edit()
                .remove(KEY_PAIRED_PREFIX + host)
                .remove(KEY_CERT_PREFIX + host)
                .apply()
            FileLogger.d(TAG, "Removed pairing for: $host")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to remove device pairing", e)
        }
    }

    fun getDeviceCert(host: String): String? {
        return try {
            certPrefs.getString(KEY_CERT_PREFIX + host, null)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get device cert", e)
            null
        }
    }
}
