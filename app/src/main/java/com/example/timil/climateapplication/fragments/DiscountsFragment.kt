package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timil.climateapplication.R
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import com.example.timil.climateapplication.adapters.DiscountsRecyclerAdapter




/**
 * A simple [Fragment] subclass.
 */
class DiscountsFragment : Fragment() {

    private var root: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DiscountsRecyclerAdapter? = null

    // just some fake data to test the adapter
    private val discountTitles =
        listOf<String>("Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9", "Title 10") as MutableList
    private val discountInformation =
        listOf<String>(
            "Information about discount 1",
            "Information about discount 2",
            "Information about discount 3",
            "Information about discount 4",
            "Information about discount 5",
            "Information about discount 6",
            "Information about discount 7",
            "Information about discount 8",
            "Information about discount 9",
            "Information about discount 10"
        ) as MutableList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_discounts, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()

        recyclerView = root!!.findViewById(R.id.recyclerView)
        adapter = DiscountsRecyclerAdapter(ArrayList<String>(), activity!!)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        recyclerView!!.setHasFixedSize(false)
        adapter!!.setDiscounts(discountTitles, discountInformation)
    }

}
