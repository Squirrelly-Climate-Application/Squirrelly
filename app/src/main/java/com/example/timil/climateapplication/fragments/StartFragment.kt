package com.example.timil.climateapplication.fragments


import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.example.timil.climateapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList

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

        val btnStart = root!!.findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            activityCallBack!!.startQRscan()
        }
        getDailyTips()
    }

    private fun getDailyTips(){
        val dailyTips = ArrayList<QueryDocumentSnapshot>()

        val db = FirebaseFirestore.getInstance()
        db.collection("tips").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    dailyTips.add(document)
                }

                val randomNumber = generateRandomNumber(dailyTips)

                val tvDaily = root!!.findViewById<TextView>(R.id.tvDailyTipInfo)
                tvDaily.text = dailyTips[randomNumber].data.getValue("information").toString()


            } else {
                Log.w("Error", "Error getting questions.", task.exception)
            }
        }
    }

    private fun generateRandomNumber(dailyTips: ArrayList<*>): Int {
        val random = Random()
        val low = 0
        val high = dailyTips.size

        return random.nextInt(high - low) + low
    }

}
