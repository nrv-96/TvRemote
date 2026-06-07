package com.smarttv.remote.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {

    private const val TAG = "TvRemote"
    private var logFile: File? = null
    private var initialized = false

    fun init(context: Context) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                logFile = File(downloadsDir, "tvremote_$dateStr.log")
                if (!logFile!!.exists()) {
                    logFile!!.createNewFile()
                }
                initialized = true
                d(TAG, "FileLogger initialized: ${logFile!!.absolutePath}")
            } else {
                initialized = false
                Log.w(TAG, "Cannot access Downloads directory, file logging disabled")
            }
        } catch (e: Exception) {
            initialized = false
            Log.e(TAG, "FileLogger init failed", e)
        }
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeToFile("D", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        writeToFile("I", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        writeToFile("W", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stackTrace = throwable?.let { Log.getStackTraceString(it) } ?: ""
        writeToFile("E", tag, if (stackTrace.isNotEmpty()) "$message\n$stackTrace" else message)
    }

    fun getLogFilePath(): String? {
        return logFile?.absolutePath
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        if (!initialized || logFile == null) return
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            val line = "$timestamp $level/$tag: $message\n"
            logFile!!.appendText(line)
            if (logFile!!.length() > 5 * 1024 * 1024) {
                rotateLogs()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log", e)
        }
    }

    private fun rotateLogs() {
        try {
            val oldFile = logFile ?: return
            val rotatedFile = File(oldFile.parentFile, "${oldFile.nameWithoutExtension}_old.log")
            oldFile.renameTo(rotatedFile)
            oldFile.createNewFile()
            Log.i(TAG, "Log rotated to ${rotatedFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Log rotation failed", e)
        }
    }
}
