package com.example.timil.climateapplication

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




class MainActivity : AppCompatActivity(), StartFragment.OnGameStart {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var user: FirebaseUser? = null
    val RC_SIGN_IN = 42

    private var startFragment: StartFragment? = null
    private var quizFragment: QuizFragment? = null

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
