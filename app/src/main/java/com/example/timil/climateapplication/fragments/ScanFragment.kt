package com.example.timil.climateapplication.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.timil.climateapplication.R
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.example.timil.climateapplication.MainActivity.Companion.MONSTER_TYPE
import com.example.timil.climateapplication.MainActivity.Companion.QUIZ_FRAGMENT_TAG
import com.example.timil.climateapplication.MonsterType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.util.concurrent.TimeUnit


class ScanFragment: Fragment(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView
    private lateinit var quizFragment: QuizFragment

    private val fireBaseDatabase = FirebaseDatabase.getInstance()
    private val fireBaseAuth = FirebaseAuth.getInstance()
    private var uid = ""
    private var rawResultText = ""

    private var secondsLeft: Long = 0
    private var minutesLeft: Long = 0
    private var hoursLeft: Long = 0

    private var dialog: AlertDialog? = null
    private var viewGroup: ViewGroup? = null

    companion object {
        private const val expiringTime = 30L
        private const val PLACE_HOLDER = "%s"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        quizFragment = QuizFragment()
        mScannerView = ZXingScannerView(context)
        uid = fireBaseAuth.currentUser!!.uid
        return mScannerView
    }

    override fun onStart() {
        super.onStart()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun handleResult(rawResult: Result) {
        rawResultText = rawResult.text
        when (rawResultText) {
            "OIL" -> { setupFragment(MonsterType.OIL) }
            "PLASTIC" -> { setupFragment(MonsterType.PLASTIC) }
            "CO2" -> { setupFragment(MonsterType.CO2) }
            else -> {
                Toast.makeText(context, "Unidentified code, please try again", Toast.LENGTH_SHORT).show()
                onResume()
            }
        }
    }

    private fun setupFragment(monsterType: MonsterType) {
        val b = Bundle()
        b.putSerializable(MONSTER_TYPE, monsterType)
        quizFragment.arguments = b
        getInitialTimeFromDatabase()
    }

    private fun saveCurrentStartTimeToDatabase() {
        val ref = fireBaseDatabase.getReference("/users/$uid/$MONSTER_TYPE/$rawResultText")

        ref.setValue(Date().time)
            .addOnSuccessListener {
                Log.d("TAG", "Successfully uploaded to the database")
            }
            .addOnFailureListener {
                Log.d("TAG", "${it.message}")
            }
    }

    private fun getInitialTimeFromDatabase() {
        showLoadingDialog(context!!.applicationContext.getText(R.string.scanning).toString())
        val ref = fireBaseDatabase.getReference("/users/$uid/$MONSTER_TYPE/$rawResultText")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Handler().post { dialog?.dismiss() }
                val initial = if (dataSnapshot.value != null) {
                    dataSnapshot.value!! as Long
                } else { 0 }
                if (isQRCodeAlreadyUsed(initial)) {
                    onResume()
                    if (minutesLeft == 0L) {
                        Toast.makeText(context!!, context!!.applicationContext.getText(R.string.until_next_time)
                            .replace(PLACE_HOLDER.toRegex(), "$secondsLeft s"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context!!, context!!.applicationContext.getText(R.string.until_next_time)
                            .replace(PLACE_HOLDER.toRegex(), "$minutesLeft min"), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    saveCurrentStartTimeToDatabase()
                    fragmentManager!!.beginTransaction().replace(R.id.fragment_container, quizFragment, QUIZ_FRAGMENT_TAG).addToBackStack(null).commit()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Handler().post { dialog?.dismiss() }
                onResume()
            }
        })
    }

    private fun isQRCodeAlreadyUsed(initial: Long): Boolean {
        secondsLeft = TimeUnit.MINUTES.toSeconds(expiringTime) - TimeUnit.MILLISECONDS.toSeconds(Date().time - initial)
        hoursLeft = TimeUnit.SECONDS.toHours(secondsLeft)
        secondsLeft -= TimeUnit.HOURS.toSeconds(hoursLeft)
        minutesLeft = TimeUnit.SECONDS.toMinutes(secondsLeft)
        secondsLeft -= TimeUnit.MINUTES.toSeconds(minutesLeft)
        return false//Date().time - initial <= TimeUnit.MINUTES.toMillis(expiringTime) //temporarily return false here to skip expiring time
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