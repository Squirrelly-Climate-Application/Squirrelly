package com.example.timil.climateapplication

import android.Manifest
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.timil.climateapplication.fragments.QuizFragment
import com.example.timil.climateapplication.fragments.StartFragment
import java.util.*
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.example.timil.climateapplication.fragments.ScanFragment


class MainActivity : AppCompatActivity(), StartFragment.OnGameStart, QuizFragment.OnButtonClick {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var user: FirebaseUser? = null
    val RC_SIGN_IN = 42

    private lateinit var startFragment: StartFragment
    private lateinit var scanFragment: ScanFragment

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
        startFragment = StartFragment()
        scanFragment = ScanFragment()

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, startFragment).commit()

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {

                FirebaseAuth.getInstance().signOut()

                user = null

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers as MutableList<AuthUI.IdpConfig>)
                        .build(),
                    RC_SIGN_IN
                )

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun startQRscan() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequestCamera()
        }
        else {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit()
        }
    }

    override fun startArFragment() {
        // start the AR game/fragment at this point
        Log.d("TESTER", "ar fragment here")
    }
}
