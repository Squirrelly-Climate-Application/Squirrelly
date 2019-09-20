package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.example.timil.climateapplication.R
import java.util.*
import android.widget.LinearLayout


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class QuizFragment : Fragment() {

    private val questionsList = ArrayList<String>(
        Arrays.asList(
            "How many kilograms of CO2 is released when driving 100 km with a petrol car?",
            "Which food causes the least CO2 emissions?"
        )
    )
    private val answersList = ArrayList<String>(
        Arrays.asList(
            "5,8 kg;18,6 kg;22,4 kg",
            "chicken;pork;beef"
        )
    )

    private var root: View? = null
    private var activityCallBack: OnButtonClick? = null

    interface OnButtonClick {
        fun startArFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_quiz, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()

        val txtQuestion = root!!.findViewById<TextView>(R.id.txtQuestion)
        txtQuestion.setText(questionsList[0])

        val answerChoices = answersList[0].split(";")

        val layout = root!!.findViewById(R.id.btnsLinearLayout) as LinearLayout
        layout.orientation = LinearLayout.VERTICAL

        for (i in 0..(answerChoices.size-1)) {
            val layoutColumn = LinearLayout(context)
            layoutColumn.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnAnswerChoice = Button(context)
            btnAnswerChoice.setLayoutParams(
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            btnAnswerChoice.setText(answerChoices[i])
            btnAnswerChoice.setId(answerChoices.indexOf(answerChoices[i]))
            btnAnswerChoice.setOnClickListener {
                Log.d("tester", "clicked "+btnAnswerChoice.text.toString())
            }

            layoutColumn.addView(btnAnswerChoice)
            layout.addView(layoutColumn)
        }

    }

}
