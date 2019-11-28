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
import com.example.timil.climateapplication.DbManager
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
    private val dbManager = DbManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_used_discounts, container, false)
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

        dbManager.getUsedDiscountsData {
            progressBar!!.visibility = View.GONE
            adapter!!.setDiscounts(it, 0, false)
        }
    }

}
