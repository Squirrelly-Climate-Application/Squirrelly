package com.example.timil.climateapplication.services

import android.content.Intent
import android.app.Service
import android.content.Context
import android.media.MediaPlayer
import android.os.IBinder
import com.example.timil.climateapplication.R


class SoundService : Service() {

    private lateinit var mp: MediaPlayer

    companion object {
        const val SOUND_EFFECT_INVALID = R.raw.invalid
        const val SOUND_EFFECT_CORRECT = R.raw.correct
        const val SOUND_EFFECT_INCORRECT = R.raw.incorrect
        const val SOUND_EFFECT_THROW = R.raw.throwing
        const val SOUND_EFFECT_HIT_PLASTIC = R.raw.hit_plastic
        const val SOUND_EFFECT_HIT_CO2 = R.raw.hit_co2
        const val SOUND_EFFECT_HIT_OIL = R.raw.hit_oil
        const val SOUND_EFFECT_WIN= R.raw.win
        const val SOUND_EFFECT_LOSE= R.raw.lose
    }

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

    fun soundEffect(context: Context, soundEffect: Int) {
        MediaPlayer.create(context, soundEffect).start()
        /*
        when (soundEffect) {
            SOUND_EFFECT_INVALID -> { MediaPlayer.create(context, R.raw.invalid).start() }
            SOUND_EFFECT_CORRECT -> { MediaPlayer.create(context, R.raw.correct).start() }
            SOUND_EFFECT_INCORRECT -> { MediaPlayer.create(context, R.raw.incorrect).start() }
            SOUND_EFFECT_THROW -> { MediaPlayer.create(context, R.raw.throwing).start() }
            SOUND_EFFECT_HIT_PLASTIC -> { MediaPlayer.create(context, R.raw.hit_plastic).start() }
            SOUND_EFFECT_HIT_CO2 -> { MediaPlayer.create(context, R.raw.hit_co2).start() }
            SOUND_EFFECT_HIT_OIL -> { MediaPlayer.create(context, R.raw.hit_oil).start() }
            SOUND_EFFECT_WIN -> { MediaPlayer.create(context, R.raw.win).start() }
            SOUND_EFFECT_LOSE -> { MediaPlayer.create(context, R.raw.lose).start() }
        }
        */
    }
}