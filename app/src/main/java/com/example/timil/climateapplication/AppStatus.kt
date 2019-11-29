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
        const val MUSIC_STATUS_TAG = "music"
        const val SOUNDS_STATUS_TAG = "sounds"
        const val GUIDELINES_STATUS_TAG = "guidelines"
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

    fun musicOn(context: Context): Boolean {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(MUSIC_STATUS_TAG, true)
    }

    fun soundsOn(context: Context): Boolean {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(SOUNDS_STATUS_TAG, true)
    }

    fun showGuidelines(context: Context): Boolean {
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        return settings.getBoolean(GUIDELINES_STATUS_TAG, true)
    }
}