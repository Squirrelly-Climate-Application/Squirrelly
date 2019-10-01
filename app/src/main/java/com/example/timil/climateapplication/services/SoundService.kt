package com.example.timil.climateapplication.services

import android.content.Intent
import android.app.Service
import android.media.MediaPlayer
import android.os.IBinder
import com.example.timil.climateapplication.R


class SoundService : Service() {

    private lateinit var mp: MediaPlayer

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        mp = MediaPlayer.create(this, R.raw.background_music)
        mp.isLooping = true
    }

    override fun onDestroy() {
        mp.stop()
    }

    override fun onStart(intent: Intent, startid: Int) {
        mp.start()
    }
}