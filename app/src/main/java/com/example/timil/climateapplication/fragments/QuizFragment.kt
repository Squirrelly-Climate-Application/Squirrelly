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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlin.collections.ArrayList
import android.widget.ProgressBar




// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class QuizFragment : Fragment() {

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

        // show a progress bar while the questions are being fetched
        val progressBar: ProgressBar? = root!!.findViewById(R.id.progressBar);
        progressBar!!.visibility = View.VISIBLE;

        val questionsList = ArrayList<QueryDocumentSnapshot>()

        val db = FirebaseFirestore.getInstance()
        // get questions from Firebase
        db.collection("questions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    val question = document
                    questionsList.add(question)
                }

                val questionNumber = generateRandomNumber(questionsList)
                Log.d("TESTEr", questionNumber.toString())

                val txtQuestion = root!!.findViewById<TextView>(R.id.txtQuestion)
                txtQuestion.text = questionsList.get(questionNumber).data.getValue("question").toString()

                val options = questionsList[questionNumber].data.getValue("options") as ArrayList<*>

                // hide progress bar when the questions have been fetched
                progressBar.visibility = View.GONE;

                generateAnswerButtons(options)
            } else {
                Log.w("Error", "Error getting questions.", task.exception)
            }
        }
    }

    // generate random number to get a random question
    private fun generateRandomNumber(questions: ArrayList<*>): Int {
        val random = Random()
        val low = 0
        val high = questions.size

        return random.nextInt(high - low) + low
    }

    // generate buttons based on how many possible options are in the question
    private fun generateAnswerButtons(options: ArrayList<*>){
        val layout = root!!.findViewById(R.id.btnsLinearLayout) as LinearLayout
        layout.orientation = LinearLayout.VERTICAL

        for (i in 0..(options.size-1)) {
            val layoutColumn = LinearLayout(context)
            layoutColumn.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnAnswer = Button(context)
            btnAnswer.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            btnAnswer.text = options[i].toString()
            btnAnswer.id = options.indexOf(options[i])
            btnAnswer.setOnClickListener {
                Log.d("tester", "clicked "+btnAnswer.text.toString())
            }

            layoutColumn.addView(btnAnswer)
            layout.addView(layoutColumn)
        }
    }

}
