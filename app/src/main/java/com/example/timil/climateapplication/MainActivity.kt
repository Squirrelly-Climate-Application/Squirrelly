package com.example.timil.climateapplication

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.CountDownTimer
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog

import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.timil.climateapplication.adapters.DiscountsRecyclerAdapter
import com.example.timil.climateapplication.fragments.*
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_COMPANY_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_INFORMATION_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_POINTS_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.EXPIRING_DATE_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.SHARED_ELEMENT_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.USER_POINTS_KEY
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.io.Serializable


class MainActivity : AppCompatActivity(), StartFragment.OnGameStart, QuizFragment.OnButtonClick, DiscountsRecyclerAdapter.OnDiscountClick, NavigationView.OnNavigationItemSelectedListener {

    private var providers: List<AuthUI.IdpConfig>? = null
    private var user: FirebaseUser? = null

    private lateinit var startFragment: StartFragment
    private lateinit var scanFragment: ScanFragment
    private lateinit var tabDiscountsFragment: TabLayoutFragment
    private lateinit var viewDiscountFragment: ViewDiscountFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var googleMapFragment: GoogleMapFragment

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private var viewGroup: ViewGroup? = null

    private val fireBaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val RC_SIGN_IN = 42
        const val RECORD_REQUEST_CODE = 1
        const val START_FRAGMENT_TAG = "StartFragment"
        const val SCAN_FRAGMENT_TAG = "ScanFragment"
        const val QUIZ_FRAGMENT_TAG = "QuizFragment"
        const val GOOGLEMAP_FRAGMENT_TAG = "GoogleMapFragment"
        const val TAB_DISCOUNTS_FRAGMENT_TAG = "TabDiscountsFragment"
        const val VIEW_DISCOUNT_FRAGMENT = "ViewDiscountFragment"
        const val SETTINGS_FRAGMENT_TAG = "SettingsFragment"
        private const val wait: Long = 1000
        const val MONSTER_TYPE = "MonsterType"
    }

    /*
    override fun onStart() {
        super.onStart()
        // Choose authentication providers
        providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startFragment = StartFragment()
        scanFragment = ScanFragment()
        tabDiscountsFragment = TabLayoutFragment()
        viewDiscountFragment = ViewDiscountFragment()
        settingsFragment = SettingsFragment()
        googleMapFragment = GoogleMapFragment()

        setupFragment(startFragment, START_FRAGMENT_TAG, true)

        // intent should have extra data if the Ar game ends and the user has clicked the Discounts button
        val intentExtra = intent.getStringExtra("discountsFragment")
        if (intentExtra != null && intentExtra.isNotEmpty()) {
            setupFragment(tabDiscountsFragment, TAB_DISCOUNTS_FRAGMENT_TAG, false)
        }

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)

        drawer = findViewById(R.id.drawer_layout)

        toggle = ActionBarDrawerToggle(this, drawer, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

    }

    /*
    override fun onResume() {
        super.onResume()
        user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Create and launch sign-in intent
            startSignIn()
        }
    }
    */

    private fun makeRequestCamera() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    */

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_home -> {
                setupFragment(startFragment, START_FRAGMENT_TAG, false)
            }
            R.id.nav_discounts -> {
                setupFragment(tabDiscountsFragment, TAB_DISCOUNTS_FRAGMENT_TAG, false)
            }
            R.id.nav_discounts_map -> {
                setupFragment(googleMapFragment, GOOGLEMAP_FRAGMENT_TAG, false)
            }
            R.id.nav_settings -> {
                setupFragment(settingsFragment, SETTINGS_FRAGMENT_TAG, false)
            }
            R.id.nav_logout -> {
                logOut()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logOut() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = getText(R.string.log_out)
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                fireBaseAuth.signOut()
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this@MainActivity).toBundle())
                object : CountDownTimer(wait, wait) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        finish()
                    }
                }.start()
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }.show()
    }

    override fun startQRscan() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequestCamera()
        }
        else {
            setupFragment(scanFragment, SCAN_FRAGMENT_TAG, true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupFragment(scanFragment, SCAN_FRAGMENT_TAG, true)
        }
    }

    override fun startArActivity(quizAnswerCorrect: Boolean?, monsterType: Serializable) {

        val mainIntent = Intent(this@MainActivity, ArActivity::class.java)
        mainIntent.putExtra(getString(R.string.quiz_answer_correct_key), quizAnswerCorrect)
        mainIntent.putExtra(MONSTER_TYPE, monsterType)
        startActivity(mainIntent)
        finish()

        // arFragment = CustomArFragment()
        // setContentView(R.layout.fragment_custom_ar) // inflates all the child views correctly
        // setupFragment(arFragment, AR_FRAGMENT_TAG) // should be used, but it loses the child views
    }

    // called when opening a discount for more information
    override fun showDiscount(view: View, document: QueryDocumentSnapshot, userPoints: Int) {
        val bundle = Bundle()
        bundle.putString(SHARED_ELEMENT_KEY, view.transitionName)

        bundle.putString(DISCOUNT_COMPANY_KEY, document.data[DISCOUNT_COMPANY_KEY].toString())
        bundle.putString(DISCOUNT_INFORMATION_KEY, document.data[DISCOUNT_INFORMATION_KEY].toString())
        bundle.putInt(DISCOUNT_POINTS_KEY, document.data[DISCOUNT_POINTS_KEY].toString().toInt())
        bundle.putInt(USER_POINTS_KEY, userPoints)
        bundle.putString(EXPIRING_DATE_KEY, document.data[EXPIRING_DATE_KEY].toString())

        bundle.putString("discountId", document.id)

        viewDiscountFragment.arguments = bundle
        //setupFragment(viewDiscountFragment, VIEW_DISCOUNT_FRAGMENT, true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, viewDiscountFragment, VIEW_DISCOUNT_FRAGMENT)
            .addSharedElement(view, view.transitionName)
            .addToBackStack(null)
            .commit()
    }

    /*
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
    */

    private fun setupFragment(fragment: Fragment, tag: String, addBackStack: Boolean) {
        if(addBackStack) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
        }
    }

    private fun findFragment(tag: String): Boolean = (supportFragmentManager.findFragmentByTag(tag) != null
            && supportFragmentManager.findFragmentByTag(tag)!!.isVisible)


    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> drawer.closeDrawer(GravityCompat.START)
            findFragment(SCAN_FRAGMENT_TAG) -> super.onBackPressed()
            findFragment(VIEW_DISCOUNT_FRAGMENT) -> super.onBackPressed()
            else -> {
                val builder = AlertDialog.Builder(this)
                val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
                builder.setView(dialogView)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        if (findFragment(START_FRAGMENT_TAG)
                            || findFragment(TAB_DISCOUNTS_FRAGMENT_TAG)
                            || findFragment(SETTINGS_FRAGMENT_TAG)
                            || findFragment(GOOGLEMAP_FRAGMENT_TAG)){
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
    }

} // MainActivity
