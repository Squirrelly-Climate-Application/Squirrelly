package com.example.timil.climateapplication.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.ar.Static
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private var paused = false
    private val wait: Long = 1500
    private val delay: Long = 3000

    private var mDelayHandler: Handler? = null

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            if (!paused) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    if (user.isEmailVerified) {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                    } else {
                        val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                    }
                } else {
                    val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@SplashActivity).toBundle())
                }

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // not sure if this works or not, but I guess we need some other context than this activity,
        // as it will soon be destroyed
        Static.load3dModelResources(applicationContext)

        //Initialize the Handler
        mDelayHandler = Handler()

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

}