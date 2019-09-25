package com.example.timil.climateapplication

import android.Manifest
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.timil.climateapplication.fragments.QuizFragment
import com.example.timil.climateapplication.fragments.StartFragment
import java.util.*
import java.util.Arrays.asList
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), StartFragment.OnGameStart {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var user: FirebaseUser? = null
    val RC_SIGN_IN = 42

    private var startFragment: StartFragment? = null
    private var quizFragment: QuizFragment? = null

    companion object {
        const val RECORD_REQUEST_CODE = 1
    }

    override fun onStart() {
        super.onStart()
        // Choose authentication providers
        providers = Arrays.asList(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (startFragment == null) {
            startFragment = StartFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, startFragment!!).commit()

        scanResult.text = if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.getString("Result")
        } else {
            //savedInstanceState.getSerializable("Tool") as String
            savedInstanceState.getString("Result")
        }

        scanButton.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                makeRequestCamera()
            }
            else {
                val intent = Intent(this, ScanActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers as MutableList<AuthUI.IdpConfig>)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    private fun makeRequestCamera() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    override fun startQRscan() {
        Log.d("tester", "testing button")
        if (quizFragment == null) {
            quizFragment = QuizFragment()
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, quizFragment!!).addToBackStack(null).commit()
    }
}
