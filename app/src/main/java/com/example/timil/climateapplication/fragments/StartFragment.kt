package com.example.timil.climateapplication.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.example.timil.climateapplication.R
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

    /*override fun onAttach(context: Context?) {
        super.onAttach(context)
        activityCallBack = context as OnGameStart
    }*/

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
    }

}
