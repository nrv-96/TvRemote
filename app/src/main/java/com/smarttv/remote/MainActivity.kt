package com.smarttv.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smarttv.remote.ui.navigation.TvRemoteNavGraph
import com.smarttv.remote.ui.theme.TvRemoteTheme
import com.smarttv.remote.util.FileLogger

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FileLogger.i(TAG, "Activity onCreate")
            enableEdgeToEdge()
            setContent {
                TvRemoteTheme {
                    TvRemoteNavGraph()
                }
            }
            FileLogger.d(TAG, "Activity UI set")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error in onCreate", e)
            throw e
        }
    }

    override fun onStart() {
        super.onStart()
        FileLogger.d(TAG, "Activity onStart")
    }

    override fun onResume() {
        super.onResume()
        FileLogger.d(TAG, "Activity onResume")
    }

    override fun onPause() {
        super.onPause()
        FileLogger.d(TAG, "Activity onPause")
    }

    override fun onStop() {
        super.onStop()
        FileLogger.d(TAG, "Activity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            FileLogger.d(TAG, "Activity onDestroy")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in onDestroy", e)
        }
    }
}
