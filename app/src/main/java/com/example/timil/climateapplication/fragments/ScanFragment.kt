package com.example.timil.climateapplication.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.timil.climateapplication.R
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.support.v7.app.AppCompatActivity



class ScanFragment: Fragment(), ZXingScannerView.ResultHandler  {

    private lateinit var mScannerView: ZXingScannerView
    private lateinit var quizFragment: QuizFragment


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        quizFragment = QuizFragment()
        mScannerView = ZXingScannerView(context)
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

        if (rawResult.text == "our qr text") {
            fragmentManager!!.beginTransaction().replace(R.id.fragment_container, quizFragment).addToBackStack(null).commit()
        }
        else {
            Toast.makeText(context, "Unidentified code, please try again", Toast.LENGTH_SHORT).show()
            onResume()
        }

    }
}