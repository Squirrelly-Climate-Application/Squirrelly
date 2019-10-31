package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.adapters.DiscountsRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class UsedDiscountsFragment : Fragment() {

    private var root: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DiscountsRecyclerAdapter? = null
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_user_discounts, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()

        recyclerView = root!!.findViewById(R.id.recyclerViewUsedDiscounts)
        adapter = DiscountsRecyclerAdapter(ArrayList<String>(), activity!!)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        recyclerView!!.setHasFixedSize(false)

        progressBar = root!!.findViewById(R.id.progressBarUsedDiscounts)
        progressBar!!.visibility = View.VISIBLE

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        getUsedDiscountsData(userId)
    }

    private fun getUsedDiscountsData(userId: String){
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
                                discounts.add(discountDocument)
                            }

                            val discountDocumentsToShowOnList = ArrayList<QueryDocumentSnapshot>()
                            if (usedDiscountsArrayList != null) {
                                if (usedDiscountsArrayList.size > 0) {
                                    for (usedDiscount in usedDiscountsArrayList) {
                                        for (discount in discounts) {
                                            if (usedDiscount["id"].toString() == discount.id) {
                                                discountDocumentsToShowOnList.add(discount)
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            progressBar!!.visibility = View.GONE

                            adapter!!.setDiscounts(discountDocumentsToShowOnList, 0, false)

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
