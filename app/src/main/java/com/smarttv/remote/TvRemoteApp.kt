package com.smarttv.remote

import android.app.Application
import com.smarttv.remote.util.FileLogger

class TvRemoteApp : Application() {

    companion object {
        private const val TAG = "TvRemoteApp"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            FileLogger.init(this)
            FileLogger.i(TAG, "Application started")
            FileLogger.i(TAG, "Log file: ${FileLogger.getLogFilePath() ?: "N/A"}")

            val currentClassLoader = javaClass.classLoader
            FileLogger.d(TAG, "App initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to initialize application", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            FileLogger.i(TAG, "Application terminated")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error during termination", e)
        }
    }
}
