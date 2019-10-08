package com.example.timil.climateapplication.fragments

import android.app.Activity
import android.graphics.PorterDuff
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
import android.support.v7.app.AlertDialog
import android.view.Gravity

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
    private var savedQuestionData = false

    interface OnButtonClick {
        fun startArActivity()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            activityCallBack = activity as OnButtonClick
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnButtonClick interface.")
        }
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

        // If the fragment has been used/created before, no need to fetch questions data again
        if (!savedQuestionData) {
            // show a progress bar while the questions are being fetched
            val progressBar: ProgressBar? = root!!.findViewById(R.id.progressBar)
            progressBar!!.visibility = View.VISIBLE

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

                    val txtQuestion = root!!.findViewById<TextView>(R.id.txtQuestion)
                    txtQuestion.text = questionsList[questionNumber].data.getValue("question").toString()

                    // hide progress bar when the questions have been fetched
                    progressBar.visibility = View.GONE

                    generateAnswerButtons(questionsList[questionNumber])
                } else {
                    Log.w("Error", "Error getting questions.", task.exception)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        savedQuestionData = true
    }

    override fun onDestroy() {
        super.onDestroy()
        savedQuestionData = false
    }

    // generate random number to get a random question
    private fun generateRandomNumber(questions: ArrayList<*>): Int {
        val random = Random()
        val low = 0
        val high = questions.size

        return random.nextInt(high - low) + low
    }

    // generate buttons based on how many possible options are in the question
    private fun generateAnswerButtons(question: QueryDocumentSnapshot){

        val options = question.data.getValue("options") as ArrayList<*>
        val rightAnswer = question.data.getValue("answer") as String
        val information = question.data.getValue("information") as String

        val layout = root!!.findViewById(R.id.btnsLinearLayout) as LinearLayout
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER

        for (i in 0..(options.size-1)) {

            val btnAnswer = Button(context)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 10, 10, 10)
            btnAnswer.layoutParams = params
            btnAnswer.background.setColorFilter(btnAnswer.context.resources.getColor(R.color.greenButtonColor), PorterDuff.Mode.MULTIPLY)
            btnAnswer.textSize = 16F

            btnAnswer.text = options[i].toString()
            btnAnswer.id = options.indexOf(options[i])
            btnAnswer.setOnClickListener {

                val alertDialog = AlertDialog.Builder(context!!).create()

                if(btnAnswer.text.equals(rightAnswer)){
                    alertDialog.setTitle("Right answer!")
                    alertDialog.setMessage("Your answer is correct! \n$information")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OKAY") {
                            dialog, _ ->
                        dialog.dismiss()
                        activityCallBack!!.startArActivity()
                    }
                } else {
                    // TODO: app idea changed. Not needed anymore?
                    alertDialog.setTitle("Wrong answer.")
                    alertDialog.setMessage("Wrong answer. Better luck next time!")
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OKAY") {
                            dialog, _ ->
                        dialog.dismiss()

                        activityCallBack!!.startArActivity()
                        // fragmentManager!!.popBackStack()
                    }
                }
                alertDialog.show()
            }

            layout.addView(btnAnswer)
        }
    }

}
