package com.example.timil.climateapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.timil.climateapplication.fragments.CustomArFragment
import com.example.timil.climateapplication.services.SoundService
import kotlinx.android.synthetic.main.fragment_custom_ar.*

const val AR_FRAGMENT_TAG = "ARFragment"

class ArActivity : AppCompatActivity(), CustomArFragment.FragmentCommunicator {

    val DEFAULT_THROWS = 5

    lateinit var launchPowerMeter: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_custom_ar) // inflates all the child views correctly
        startService(Intent(this@ArActivity, SoundService::class.java))

        launchPowerMeter = findViewById(R.id.launch_power_meter)
/*
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_2, CustomArFragment(), AR_FRAGMENT_TAG)
            .addToBackStack(null)
            .commit() */
    }

    override fun onStart() {
        super.onStart()

        setScore(0f)
        setThrows(DEFAULT_THROWS) //TODO: make it come from the bundle
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        startService(Intent(this@ArActivity, SoundService::class.java))
    }

    override fun onStop() {
        super.onStop()
        supportActionBar?.show()
    }

    override fun onDestroy() {
        stopService(Intent(this@ArActivity, SoundService::class.java))
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        stopService(Intent(this@ArActivity, SoundService::class.java))
    }

    override fun setScore(score: Float) {

        tv_score.text = "Score: $score"
    }
    override fun setHitPoints(hp: Int) {

        tv_hitpoints.text = "HP: $hp"
    }

    override fun setThrows(throws: Int) {

        tv_throws.text = "Throws: $throws"
    }

    override fun setWindX(windX: Float) {

        tv_wind_x.text = "Wind(x): $windX"
    }

    override fun setWindY(windY: Float) {

        tv_wind_y.text = "Wind(x): $windY"
    }

    override fun setPower(power: Int) {
        launchPowerMeter.progress = power
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                val mainIntent = Intent(this@ArActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }.show()
    }

    private var viewGroup: ViewGroup? = null

} // ArActivity