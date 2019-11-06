package com.example.timil.climateapplication.fragments

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.timil.climateapplication.AppStatus
import com.example.timil.climateapplication.MainActivity
import com.example.timil.climateapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment: Fragment() {

    private val fireBaseAuth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null
    private var viewGroup: ViewGroup? = null

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions

    companion object {
        private const val USER_EMAIL = "USER_EMAIL"
        private const val USER_PASSWORD = "USER_PASSWORD"
        private const val RC_SIGN_IN: Int = 1
        private const val wait: Long = 1000
        private const val ACTION = "Action"
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, mGoogleSignInOptions)
    }

    private fun signIn() {
        configureGoogleSignIn()
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        showLoadingDialog(context!!.applicationContext.getString(R.string.registering_user))
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                fireBaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Handler().post { dialog?.dismiss() }
            }
        }
    }

    private fun fireBaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        fireBaseAuth.signInWithCredential(credential).addOnCompleteListener {
            Handler().post { dialog?.dismiss() }
            if (it.isSuccessful) {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity!!).toBundle())
                object : CountDownTimer(wait, wait) {
                    override fun onTick(millisUntilFinished: Long) {
                    }
                    override fun onFinish() {
                        activity!!.finish()
                    }
                }.start()
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.google_sign_in_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        google_button.setOnClickListener {
            if (AppStatus().isOnline(context!!)) {
                signIn()
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.check_internet), Toast.LENGTH_SHORT).show()
            }
        }

        button_register.setOnClickListener {
            if (AppStatus().isOnline(context!!)) {
                createAccount(edit_text_email.text.toString(), edit_text_password.text.toString(), edit_text_confirm_password.text.toString())
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.check_internet), Toast.LENGTH_SHORT).show()
            }
        }

        text_view_already.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment_container_sign_in, SignInFragment()).commit()
        }
        view.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            when {
                bottom == oldBottom -> {  }
                bottom < oldBottom -> { softKeyboardIsActive(true) }
                bottom > oldBottom -> { softKeyboardIsActive(false) }
            }
        }
    }

    private fun softKeyboardIsActive(isActive: Boolean) {
        if (isActive) {
            image_view_helsinki_logo.visibility = ImageView.INVISIBLE
            google_button.visibility = SignInButton.INVISIBLE
        } else {
            image_view_helsinki_logo.visibility = ImageView.VISIBLE
            google_button.visibility = SignInButton.VISIBLE
        }
    }

    private fun createAccount(email: String, password: String, confirmPassword: String) {
        if (!validateForm(email, password, confirmPassword)) {
            return
        }
        showLoadingDialog(context!!.applicationContext.getText(R.string.registering_user).toString())
        fireBaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Handler().post { dialog?.dismiss() }
                if (task.isSuccessful) {
                    Toast.makeText(context!!, context!!.applicationContext.getText(R.string.registration_succeeded), Toast.LENGTH_SHORT).show()
                    fireBaseAuth.currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                val signInFragment = SignInFragment()
                                val b = Bundle()
                                b.putString(USER_EMAIL, "${edit_text_email.text}")
                                b.putString(USER_PASSWORD, "${edit_text_password.text}")
                                signInFragment.arguments = b
                                fragmentManager!!.beginTransaction().replace(R.id.fragment_container_sign_in, signInFragment).commit()
                                Snackbar.make(view!!, context!!.applicationContext.getText(R.string.verification_email), Snackbar.LENGTH_LONG)
                                    .setAction(ACTION, null).show()
                            } else {
                                Toast.makeText(context!!, task2.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    try {
                        throw task.exception!!
                    } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context!!, context!!.applicationContext.getText(R.string.invalid_email_form), Toast.LENGTH_SHORT).show()
                    } catch (existEmail: FirebaseAuthUserCollisionException) {
                        Toast.makeText(context!!, context!!.applicationContext.getText(R.string.already_registered), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun validateForm(email: String, password: String, confirmPassword: String): Boolean {
        when {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return false
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.enter_password), Toast.LENGTH_SHORT).show()
                return false
            }
            password.length < 6 -> {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.password_too_short), Toast.LENGTH_SHORT).show()
                return false
            }
            password != confirmPassword -> {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    private fun showLoadingDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_progress, viewGroup)
        val dialogTxtView = dialogView.findViewById<TextView>(R.id.txtUploadProgress)
        dialogTxtView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
    }

}