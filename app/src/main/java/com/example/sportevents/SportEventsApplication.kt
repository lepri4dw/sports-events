package com.example.sportevents

import android.app.Application
import android.util.Log
import com.example.sportevents.util.AuthManager

class SportEventsApplication : Application() {
    companion object {
        private const val TAG = "SportEventsApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize AuthManager
            Log.d(TAG, "Initializing AuthManager...")
            AuthManager.init(applicationContext)
            Log.d(TAG, "AuthManager initialized successfully: ${AuthManager.isInitialized()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AuthManager: ${e.message}", e)
        }
    }
} 