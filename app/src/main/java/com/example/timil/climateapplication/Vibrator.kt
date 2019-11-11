package com.example.timil.climateapplication

import android.app.Activity
import android.content.Context

class Vibrator {

    private lateinit var vibrator: android.os.Vibrator

    companion object {
        const val VIBRATION_TIME_SHORT: Long = 100
        const val VIBRATION_TIME_REGULAR: Long = 500
        const val VIBRATION_TIME_LONG: Long = 1000
    }

    fun vibrate(activity: Activity, time: Long) {
        vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        vibrator.vibrate(time)
    }
}