package com.example.timil.climateapplication.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timil.climateapplication.R
import kotlinx.android.synthetic.main.fragment_settings.*
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.timil.climateapplication.AppStatus
import com.example.timil.climateapplication.AppStatus.Companion.SOUNDS_STATUS_TAG
import com.example.timil.climateapplication.AppStatus.Companion.VIBRATION_STATUS_TAG
import com.example.timil.climateapplication.Vibrator
import com.example.timil.climateapplication.Vibrator.Companion.VIBRATION_TIME_REGULAR

class SettingsFragment: Fragment() {

    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        editor = settings.edit()
        val vibrationStatus = AppStatus().vibrationOn(context!!)
        val soundsStatus = AppStatus().soundsOn(context!!)
        if (vibrationStatus) { vibration.isChecked = true }
        if (soundsStatus) { sounds.isChecked = true }

        vibration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean(VIBRATION_STATUS_TAG, true).apply()
                Vibrator().vibrate(activity!!, VIBRATION_TIME_REGULAR)
            }
            else {
                editor.putBoolean(VIBRATION_STATUS_TAG, false).apply()
            }
        }
        sounds.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean(SOUNDS_STATUS_TAG, true).apply()
            }
            else {
                editor.putBoolean(SOUNDS_STATUS_TAG, false).apply()
            }
        }
    }
}