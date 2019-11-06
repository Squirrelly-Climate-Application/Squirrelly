package com.example.timil.climateapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.timil.climateapplication.fragments.RegisterFragment
import com.google.firebase.auth.FirebaseAuth
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.transition.TransitionInflater
import android.view.ViewGroup
import com.example.timil.climateapplication.fragments.SignInFragment

class SignInActivity : AppCompatActivity() {

    private var viewGroup: ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            setupFragment(RegisterFragment())
        } else {
            val signInFragment = SignInFragment()
            val b = Bundle()
            b.putString("USER_EMAIL", user.email)
            signInFragment.arguments = b
            setupFragment(signInFragment)
        }
    }

    private fun setupFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_sign_in, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }.show()
    }
}