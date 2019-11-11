package com.example.timil.climateapplication

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.util.Log

class AppStatus {

    private lateinit var connectivityManager: ConnectivityManager
    private var connected = false
    private lateinit var settings: SharedPreferences

    companion object {
        const val VIBRATION_STATUS_TAG = "vibration"
        const val SOUNDS_STATUS_TAG = "sounds"
    }

    fun isOnline(context: Context): Boolean {
        try {
            connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkInfo = connectivityManager.activeNetworkInfo
            connected = networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
            return connected

        } catch (e: Exception) {
            Log.d("connectivity", e.toString())
        }
        return connected
    }

    fun vibrationOn(context: Context): Boolean {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(VIBRATION_STATUS_TAG, true)
    }

    fun soundsOn(context: Context): Boolean {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(SOUNDS_STATUS_TAG, true)
    }
}