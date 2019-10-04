package com.example.timil.climateapplication

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.*
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog

import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.example.timil.climateapplication.adapters.DiscountsRecyclerAdapter
import com.example.timil.climateapplication.fragments.*
import com.example.timil.climateapplication.services.SoundService
import kotlinx.android.synthetic.main.fragment_custom_ar.*


class MainActivity : AppCompatActivity(), StartFragment.OnGameStart, QuizFragment.OnButtonClick, DiscountsRecyclerAdapter.OnDiscountClick {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var user: FirebaseUser? = null

    private lateinit var startFragment: StartFragment
    private lateinit var scanFragment: ScanFragment
    // private lateinit var arFragment: CustomArFragment
    private lateinit var discountsFragment: DiscountsFragment

    private var viewGroup: ViewGroup? = null

    companion object {
        const val RC_SIGN_IN = 42
        const val RECORD_REQUEST_CODE = 1
        const val START_FRAGMENT_TAG = "StartFragment"
        const val SCAN_FRAGMENT_TAG = "ScanFragment"
        const val QUIZ_FRAGMENT_TAG = "QuizFragment"
        const val DISCOUNTS_FRAGMENT_TAG = "DiscountsFragment"
    }

    override fun onStart() {
        super.onStart()
        // Choose authentication providers
        providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startFragment = StartFragment()
        scanFragment = ScanFragment()
        discountsFragment = DiscountsFragment()

        setupFragment(startFragment, START_FRAGMENT_TAG)

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)

        val fabDiscounts = findViewById<FloatingActionButton>(R.id.fabDiscounts)
        fabDiscounts.setOnClickListener {
            setupFragment(discountsFragment, DISCOUNTS_FRAGMENT_TAG)
        }
    }
    
    override fun onResume() {
        super.onResume()
        user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Create and launch sign-in intent
            startSignIn()
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
        return when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                user = null
                startSignIn()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun startQRscan() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequestCamera()
        }
        else {
            setupFragment(scanFragment, SCAN_FRAGMENT_TAG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupFragment(scanFragment, SCAN_FRAGMENT_TAG)
        }
    }

    override fun startArActivity() {

        val mainIntent = Intent(this@MainActivity, ArActivity::class.java)
        startActivity(mainIntent)
        finish()

        // arFragment = CustomArFragment()
        // setContentView(R.layout.fragment_custom_ar) // inflates all the child views correctly
        // setupFragment(arFragment, AR_FRAGMENT_TAG) // should be used, but it loses the child views
    }

    override fun showDiscount() {
        // called when opening a discount for more information

        Log.d("Tester", "discount click")
    }

    private fun startSignIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers as MutableList<AuthUI.IdpConfig>)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.drawable.helsinki_logo)
                .build(),
            RC_SIGN_IN
        )
    }

    private fun setupFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    private fun findFragment(tag: String): Boolean = (supportFragmentManager.findFragmentByTag(tag) != null
            && supportFragmentManager.findFragmentByTag(tag)!!.isVisible)

    override fun onBackPressed() {
        if (findFragment(SCAN_FRAGMENT_TAG)){
            super.onBackPressed()
        }
        else {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
            builder.setView(dialogView)
                .setPositiveButton(R.string.yes) { _, _ ->
                    if (findFragment(START_FRAGMENT_TAG)){
                        finish()
                    }
                    else {
                        super.onBackPressed()
                    }
                }
                .setNegativeButton(R.string.no) { _, _ ->
                }.show()
        }
    }

} // MainActivity
