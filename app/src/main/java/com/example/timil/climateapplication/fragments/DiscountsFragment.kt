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
import android.icu.text.SimpleDateFormat
import com.example.timil.climateapplication.DbManager
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
    private val dbManager = DbManager()

    companion object {
        const val DISCOUNT_COMPANY_LOGO_KEY = "companyLogo"
        const val DISCOUNT_POINTS_KEY = "points_needed"
        const val USER_POINTS_KEY = "points"
        const val DISCOUNT_COMPANY_KEY = "company"
        const val DISCOUNT_INFORMATION_KEY = "information"
        const val EXPIRING_DATE_KEY = "expiring date"
        const val SHARED_ELEMENT_KEY = "shared element"
        const val DISCOUNT_WEB_LINK_KEY = "website"
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

        dbManager.getUserData(userPoints!!) {
            userPoints = it

            val tvUserPoints = root.findViewById<TextView>(R.id.tvMyPoints)
            tvUserPoints.text = resources.getString(R.string.user_points, userPoints.toString())
            tvUserPoints.visibility = View.VISIBLE
        }
        dbManager.getDiscountsData {

            progressBar.visibility = View.GONE
            adapter.setDiscounts(it, userPoints!!, true)
        }

        return root
    }
}
