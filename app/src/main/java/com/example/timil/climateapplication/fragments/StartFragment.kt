package com.example.timil.climateapplication.fragments


import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.timil.climateapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList
import android.text.format.DateUtils
import android.widget.Button
import com.example.timil.climateapplication.activities.MainActivity.Companion.START_FRAGMENT_TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_start.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class StartFragment : Fragment() {

    private var root: View? = null
    private var activityCallBack: OnGameStart? = null

    private val fireBaseDatabase = FirebaseDatabase.getInstance()
    private val fireBaseAuth = FirebaseAuth.getInstance()
    private var uid = ""

    private lateinit var buttonStart: Button

    interface OnGameStart {
        fun startQRscan()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            activityCallBack = activity as OnGameStart
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnGameStart interface.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_start, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()
        uid = fireBaseAuth.currentUser!!.uid


        squirrelly_view.setOnClickListener {
            squirrelly_view.setBackgroundResource(R.drawable.squirrelly_shake_head)
            val squirrelAnimation = squirrelly_view.background as AnimationDrawable
            squirrelAnimation.stop()
            squirrelAnimation.isOneShot = true
            squirrelAnimation.start()
        }

        buttonStart = root!!.findViewById(R.id.button_start)
        buttonStart.setOnClickListener {
            activityCallBack!!.startQRscan()
        }
        /* buttonStart = root!!.findViewById(R.id.custom_button_frame)
        buttonStart.setOnStartGameListener(object : OnStartGameListener {
            override fun onStartGame() {
                activityCallBack!!.startQRscan()
            }
        })*/
        getInitialTimeFromDatabase()
    }

    private fun getDailyTips(position: Int){
        val dailyTips = ArrayList<QueryDocumentSnapshot>()

        val db = FirebaseFirestore.getInstance()
        db.collection("tips").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    dailyTips.add(document)
                }
                if (fragmentManager!!.findFragmentByTag(START_FRAGMENT_TAG) != null
                    && fragmentManager!!.findFragmentByTag(START_FRAGMENT_TAG)!!.isVisible) {
                    val tvDaily = root!!.findViewById<TextView>(R.id.tvDailyTipInfo)
                    tvDaily.background = context!!.applicationContext.getDrawable(R.drawable.speech_bubble)
                    squirrelly_view.setBackgroundResource(R.drawable.squirrelly_talk)
                    squirrelly_view.post {
                        val squirrelAnimation = squirrelly_view.background as AnimationDrawable
                        squirrelAnimation.isOneShot = true
                        squirrelAnimation.start()
                    }
                    if (dailyTips.size > position) {
                        tvDaily.text = dailyTips[position].data.getValue("information").toString()
                        saveDailyTipPositionToDatabase(position)
                    } else {
                        tvDaily.text = dailyTips[0].data.getValue("information").toString()
                        saveDailyTipPositionToDatabase(0)
                    }
                }
            } else {
                Log.w("Error", "Error getting questions.", task.exception)
            }
        }
    }

    private fun saveCurrentStartTimeToDatabase() {
        val ref = fireBaseDatabase.getReference("/users/$uid/daily tip/used")
        ref.setValue(Date().time)
            .addOnSuccessListener {
                Log.d("TAG", "Successfully uploaded to the database")
            }
            .addOnFailureListener {
                Log.d("TAG", "${it.message}")
            }
    }

    private fun getInitialTimeFromDatabase() {
        val ref = fireBaseDatabase.getReference("/users/$uid/daily tip/used")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val initial = if (dataSnapshot.value != null) {
                    dataSnapshot.value!! as Long
                } else { 0 }
                when {
                    initial == 0L -> {
                        saveDailyTipPositionToDatabase(0)
                        getDailyTips(0)
                    }
                    DateUtils.isToday(initial) -> {
                        getDailyTipPositionFromDatabase(0)
                    }
                    else -> {
                        getDailyTipPositionFromDatabase(1)
                    }
                }
                saveCurrentStartTimeToDatabase()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", "${databaseError.message}")
            }
        })
    }

    private fun saveDailyTipPositionToDatabase(position: Int) {
        val ref = fireBaseDatabase.getReference("/users/$uid/daily tip/position")
        ref.setValue(position)
            .addOnSuccessListener {
                Log.d("TAG", "Successfully uploaded to the database")
            }
            .addOnFailureListener {
                Log.d("TAG", "${it.message}")
            }
    }

    private fun getDailyTipPositionFromDatabase(nextTip: Int) {
        val ref = fireBaseDatabase.getReference("/users/$uid/daily tip/position")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val position = dataSnapshot.value.toString().toInt()
                getDailyTips(position + nextTip)
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

}
