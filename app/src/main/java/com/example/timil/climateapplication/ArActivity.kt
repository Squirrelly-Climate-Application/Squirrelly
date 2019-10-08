package com.example.timil.climateapplication

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.example.timil.climateapplication.fragments.CustomArFragment
import com.example.timil.climateapplication.services.SoundService
import kotlinx.android.synthetic.main.fragment_custom_ar.*

/**
 * This Activity is only needed because the CustomArFragment's contained UI views cannot be accessed
 * directly from it (due to null object references).
 * @author Ville Lohkovuori, Leo Partanen
 * */

const val AR_FRAGMENT_TAG = "ARFragment"

private const val DEFAULT_THROWS = 5

class ArActivity : AppCompatActivity() {

    // the game score
    var score = 0.0f
        set(value) {
            field = value
            setUIScore(value)
        }

    //TODO: the throw number should arrive from the bundle
    // note: these could be in a 'Game' class, but for now I don't think that's necessary
    var numOfThrows = DEFAULT_THROWS
        set(value) {
            field = value
            setUIThrows(value)
            if (value == 0) {
                endGame(false) // if the monster is dead, the game ends before this call
            }
        }

    // we need this reference to manage saving and restoring the Fragment
    private var customArFragmentInstance: Fragment? = null

    // monitored also from the CustomArFragment (projectiles cannot be launched if it's true)
    var gamePaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar of the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.fragment_custom_ar) // inflates all the child views correctly

        // it's only a sub-view in the xml file, but this still works for saving a reference
        // to the correct Fragment
        customArFragmentInstance = supportFragmentManager.findFragmentById(R.id.ar_fragment)

        startService(Intent(this@ArActivity, SoundService::class.java))

        // we're restoring the Activity due to minimizing the app, etc
        if (savedInstanceState != null) {

            // Restore the fragment's instance
            customArFragmentInstance = supportFragmentManager.getFragment(savedInstanceState, AR_FRAGMENT_TAG)
        }

        btn_pause.setOnClickListener {

            val arFragment = (customArFragmentInstance as CustomArFragment)

            if (!gamePaused) {
                arFragment.pauseGame()
                gamePaused = true
                btn_pause.text = "Resume"
            } else {
                arFragment.resumeGame()
                gamePaused = false
                btn_pause.text = "Pause"
            }
        } // onClickListener

        // doesn't work, which causes a whole slew of problems
/*
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_2, CustomArFragment(), AR_FRAGMENT_TAG)
            .addToBackStack(null)
            .commit() */
    } // onCreate

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the fragment's instance (it should always exist)
        supportFragmentManager.putFragment(outState, AR_FRAGMENT_TAG, customArFragmentInstance!!)
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

    private fun setUIScore(score: Float) {

        if (tv_score == null) return
        tv_score.text = "Score: $score"
    }

    private fun setUIThrows(throws: Int) {

        if (tv_throws == null) return
        tv_throws.text = "Throws: $throws"
    }

    // these we need to do from the fragment so they're public
    fun setUIHitPoints(hp: Int) {

        if (tv_hitpoints == null) return
        tv_hitpoints.text = "HP: $hp"
    }

    fun setUIWindX(windX: Float) {

        if (tv_wind_x == null) return
        tv_wind_x.text = "Wind(x): $windX"
    }

    fun setUIWindY(windY: Float) {

        if (tv_wind_y == null) return
        tv_wind_y.text = "Wind(x): $windY"
    }

    fun setUIPower(power: Int) {

        if (launch_power_meter == null) return
        launch_power_meter.progress = power
    }

    fun endGame(monsterDead: Boolean) {

        if (monsterDead) score *= 1.5f

        Log.d("HUUH", "final points: $score")
        //TODO: move to the reward screen and send the points there
    } // endGame

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
    } // onBackPressed

    private var viewGroup: ViewGroup? = null

} // ArActivity