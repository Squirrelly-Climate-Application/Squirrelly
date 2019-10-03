package com.example.timil.climateapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.timil.climateapplication.fragments.CustomArFragment
import kotlinx.android.synthetic.main.fragment_custom_ar.*

const val AR_FRAGMENT_TAG = "ARFragment"

class ArActivity : AppCompatActivity(), CustomArFragment.FragmentCommunicator {

    val DEFAULT_THROWS = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_custom_ar) // inflates all the child views correctly
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
    }

    override fun onStop() {
        super.onStop()
        supportActionBar?.show()
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

} // ArActivity