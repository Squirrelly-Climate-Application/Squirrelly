package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timil.climateapplication.R
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import com.example.timil.climateapplication.adapters.DiscountsRecyclerAdapter
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import android.icu.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class DiscountsFragment : Fragment() {

    private lateinit var root: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiscountsRecyclerAdapter
    private lateinit var progressBar: ProgressBar

    private var userPoints: Int? = 0

    companion object {
        const val DISCOUNT_POINTS_KEY = "points_needed"
        const val USER_POINTS_KEY = "points"
        const val DISCOUNT_COMPANY_KEY = "company"
        const val DISCOUNT_INFORMATION_KEY = "information"
        const val EXPIRING_DATE_KEY = "expiring date"
        const val SHARED_ELEMENT_KEY = "shared element"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_discounts, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        adapter = DiscountsRecyclerAdapter(ArrayList<String>(), activity!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)

        progressBar = root.findViewById(R.id.progressBarDiscounts)
        progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        getUserData(userId)
        getDiscountsData(userId)

        return root
    }

    override fun onResume() {
        super.onResume()


    }

    private fun getUserData(userId: String){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->

                if (document != null) {
                    // in case the user is new and doesn't have any data in Firebase yet -> give data to avoid null exceptions
                    if(document.data == null){
                        val userData = hashMapOf(
                            "points" to userPoints
                        )
                        db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("tester", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e -> Log.w("tester", "Error writing document", e) }
                    }
                    else {
                        userPoints = document.data!!.getValue(USER_POINTS_KEY).toString().toInt()
                    }

                    val tvUserPoints = root.findViewById<TextView>(R.id.tvMyPoints)
                    tvUserPoints.text = resources.getString(R.string.user_points, userPoints.toString())
                    tvUserPoints.visibility = View.VISIBLE

                } else {
                    Log.d("tester", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("tester", "get failed with ", exception)
            }

    }

    private fun getDiscountsData(userId: String){
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    var usedDiscountsArrayList: ArrayList<Map<String, Date>>? = null
                    if (document.get("used_discounts") != null) {
                        usedDiscountsArrayList = document.get("used_discounts") as ArrayList<Map<String, Date>>
                    }

                    db.collection("discounts").get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val discounts = ArrayList<QueryDocumentSnapshot>()
                            for (discountDocument in task.result!!) {
                                val expiringDate = DateFormat.getDateInstance().parse(discountDocument.data.getValue(EXPIRING_DATE_KEY).toString()).time
                                if (Date().time < expiringDate || DateUtils.isToday(expiringDate)) {
                                    discounts.add(discountDocument)
                                }
                                //discounts.add(discountDocument)
                            }

                            // if user has already used some of the discounts -> do not show it on the list
                            if (usedDiscountsArrayList != null) {
                                if (usedDiscountsArrayList.size > 0) {
                                    for (usedDiscount in usedDiscountsArrayList) {
                                        for (discount in discounts) {
                                            if (usedDiscount["id"].toString() == discount.id) {
                                                discounts.remove(discount)
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            progressBar.visibility = View.GONE

                            adapter.setDiscounts(discounts, userPoints!!, true)

                        } else {
                            Log.w("Error", "Error getting discounts.", task.exception)
                        }
                    }
                }

            } else {
                Log.w("Error", "Error getting discounts.", task.exception)
            }
        }
    }

}
