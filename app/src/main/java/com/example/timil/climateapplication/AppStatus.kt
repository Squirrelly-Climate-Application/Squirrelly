package com.example.timil.climateapplication

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

class AppStatus {

    private lateinit var connectivityManager: ConnectivityManager
    private var connected = false

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
}