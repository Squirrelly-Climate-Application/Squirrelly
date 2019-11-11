package com.example.timil.climateapplication.fragments

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.timil.climateapplication.AppStatus
import com.example.timil.climateapplication.MainActivity
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.Vibrator
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_signin.*
import kotlinx.android.synthetic.main.fragment_signin.edit_text_email
import kotlinx.android.synthetic.main.fragment_signin.edit_text_password
import kotlinx.android.synthetic.main.fragment_signin.image_view_helsinki_logo

class SignInFragment: Fragment() {

    private val fireBaseAuth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null
    private var viewGroup: ViewGroup? = null

    companion object {
        private const val USER_EMAIL = "USER_EMAIL"
        private const val USER_PASSWORD = "USER_PASSWORD"
        private const val PLACE_HOLDER = "%s"
        private const val PASSWORD = "password"
        private const val wait: Long = 1000
        private const val ACTION = "Action"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(USER_EMAIL)?.let { edit_text_email.text = SpannableStringBuilder(it) }
        arguments?.getString(USER_PASSWORD)?.let { edit_text_password.text = SpannableStringBuilder(it) }

        button_sign_in.setOnClickListener {
            if (AppStatus().isOnline(context!!)) {
                signIn("${edit_text_email.text}", "${edit_text_password.text}")
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.check_internet), Toast.LENGTH_SHORT).show()
                shakeButton(button_sign_in)
            }
        }

        text_view_create_account.setOnClickListener {
            fragmentManager!!.beginTransaction().replace(R.id.fragment_container_sign_in, RegisterFragment()).commit()
        }

        if (fireBaseAuth.currentUser == null) {
            button_send_verification_email.isEnabled = false
            button_send_verification_email.alpha = 0.3f
        }
        button_send_verification_email.setOnClickListener {
            if (AppStatus().isOnline(context!!)) {
                if (fireBaseAuth.currentUser != null) {
                    sendVerificationEmail()
                }
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.check_internet), Toast.LENGTH_SHORT).show()
                shakeButton(button_send_verification_email)
            }
        }

        text_view_forgot_password.setOnClickListener {
            if (AppStatus().isOnline(context!!)) {
                verifyEmailExists()
            } else {
                Toast.makeText(context!!, context!!.applicationContext.getText(R.string.check_internet), Toast.LENGTH_SHORT).show()
            }
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
            bottom_linear_layout.orientation = LinearLayout.HORIZONTAL
        } else {
            image_view_helsinki_logo.visibility = ImageView.VISIBLE
            bottom_linear_layout.orientation = LinearLayout.VERTICAL
        }
    }

    private fun signIn(email: String, password: String) {
        if (!validateForm(email, password)) {
            shakeButton(button_sign_in)
            return
        }
        showLoadingDialog(context!!.applicationContext.getText(R.string.singing_in).toString())
        fireBaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Handler().post { dialog?.dismiss() }
                    if (fireBaseAuth.currentUser!!.isEmailVerified) {
                        val intent = Intent(context, MainActivity::class.java)
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity!!).toBundle())
                        object : CountDownTimer(wait, wait) {
                            override fun onTick(millisUntilFinished: Long) {
                            }
                            override fun onFinish() {
                                activity!!.finish()
                            }
                        }.start()
                    }
                    else {
                        Toast.makeText(context, context!!.applicationContext.getText(R.string.email_not_verified), Toast.LENGTH_SHORT).show()
                        button_send_verification_email.isEnabled = true
                        button_send_verification_email.alpha = 1f
                    }
                } else {
                    Handler().post { dialog?.dismiss() }
                    Toast.makeText(context!!, context!!.applicationContext.getText(R.string.incorrect_email_address_or_password), Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun validateForm(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context!!, context!!.applicationContext.getText(R.string.enter_email), Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context!!, context!!.applicationContext.getText(R.string.enter_password), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendVerificationEmail() {
        showLoadingDialog(context!!.applicationContext.getText(R.string.sending_verification).toString())
        button_send_verification_email.isEnabled = false
        button_send_verification_email.alpha = 0.3f
        fireBaseAuth.currentUser!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                Handler().post { dialog?.dismiss() }
                if (task.isSuccessful) {
                    Snackbar.make(view!!, context!!.applicationContext.getText(R.string.verification_email), Snackbar.LENGTH_LONG)
                        .setAction(ACTION, null).show()
                } else {
                    Toast.makeText(context!!, context!!.applicationContext.getText(R.string.already_sent), Toast.LENGTH_SHORT).show()
                    button_send_verification_email.isEnabled = true
                    button_send_verification_email.alpha = 1f
                    shakeButton(button_send_verification_email)
                }
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

    private fun verifyEmailExists() {
        if (TextUtils.isEmpty("${edit_text_email.text}")) {
            Toast.makeText(context!!, context!!.applicationContext.getText(R.string.enter_email), Toast.LENGTH_SHORT).show()
            return
        }
        showLoadingDialog(context!!.applicationContext.getText(R.string.verifying_email).toString())
        fireBaseAuth.createUserWithEmailAndPassword("${edit_text_email.text}", PASSWORD)
            .addOnCompleteListener { task ->
                Handler().post { dialog?.dismiss() }
                if (task.isSuccessful) {
                    fireBaseAuth.currentUser!!.delete()
                    Toast.makeText(context!!, context!!.applicationContext.getText(R.string.no_registration), Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        throw task.exception!!
                    } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context!!, context!!.applicationContext.getText(R.string.invalid_email_form), Toast.LENGTH_SHORT).show()
                    }catch (existEmail: FirebaseAuthUserCollisionException) {
                        showDialog()
                    }
                }
            }
    }

    private fun sendPasswordResetEmail() {
        showLoadingDialog(context!!.applicationContext.getText(R.string.sending_reset_password_email).toString())
        fireBaseAuth.sendPasswordResetEmail("${edit_text_email.text}")
            .addOnCompleteListener { task ->
                Handler().post { dialog?.dismiss()
                    if (task.isSuccessful) {
                        Snackbar.make(view!!, context!!.applicationContext.getText(R.string.reset_password_email), Snackbar.LENGTH_LONG)
                            .setAction(ACTION, null).show()
                    } else {
                        Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(context)
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = context!!.applicationContext.getText(R.string.reset_password)
            .replace(PLACE_HOLDER.toRegex(), "${edit_text_email.text}")
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                sendPasswordResetEmail()
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }.show()
    }

    private fun shakeButton(button: Button) {
        if (AppStatus().vibrationOn(context!!)) {
            Vibrator().vibrate(activity!!, Vibrator.VIBRATION_TIME_SHORT)
        }
        val shake: Animation = AnimationUtils.loadAnimation(activity!!.applicationContext,
            R.anim.shake
        )
        button.startAnimation(shake)
    }
}