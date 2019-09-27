package com.example.timil.climateapplication

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private var paused = false
    private val wait: Long = 1000
    private val delay: Long = 3000

    private var mDelayHandler: Handler? = null

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            //val intent = Intent(applicationContext, MainActivity::class.java)
            //startActivity(intent)
            //finish()

            if (!paused) {
                val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(mainIntent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                object : CountDownTimer(wait, wait) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        finish()
                    }
                }.start()
            }
            else {
                finish()
            }
            /*
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mainIntent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
            object : CountDownTimer(wait, wait) {
                override fun onTick(millisUntilFinished: Long) {
                }
                override fun onFinish() {
                    finish()
                }
            }.start()
            */
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Initialize the Handler
        mDelayHandler = Handler()

        //Navigate with delay
        mDelayHandler!!.postDelayed(mRunnable, delay)

    }

    override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }



    /*
    private val wait: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        load3DModels()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


    private fun load3DModels() {

        //TODO: load 3D models here

        val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(mainIntent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
        object : CountDownTimer(wait, wait) {
            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                finish()
            }
        }.start()
    }
    */

}