package com.example.timil.climateapplication.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.timil.climateapplication.R
import com.google.firebase.firestore.QueryDocumentSnapshot



class DiscountsRecyclerAdapter(private val discounts: MutableList<String>, activity: Activity)
    : RecyclerView.Adapter<DiscountsRecyclerAdapter.DiscountViewHolder>() {

    private var mCallback: OnDiscountClick? = null

    private var discountsList: MutableList<QueryDocumentSnapshot>? = null
    private var userPoints: Int? = null
    private var proceedToViewFragment: Boolean? = null

    interface OnDiscountClick {
        fun showDiscount(document: QueryDocumentSnapshot, userPoints: Int)
    }

    init {
        try {
            this.mCallback = activity as OnDiscountClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnDiscountClick interface.")
        }
    }

    fun setDiscounts(discounts: ArrayList<QueryDocumentSnapshot>, points: Int, proceedToViewFragment: Boolean) {
        discountsList = discounts
        userPoints = points
        this.proceedToViewFragment = proceedToViewFragment

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DiscountViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.discount_recycler_view_item, viewGroup, false)
        return DiscountViewHolder(view)
    }

    override fun onBindViewHolder(discountViewHolder: DiscountViewHolder, i: Int) {
        discountViewHolder.discountTitle.text = discountsList!![i].data.getValue("company").toString()
        discountViewHolder.discountInfo.text = discountsList!![i].data.getValue("information").toString()
        discountViewHolder.discountPoints.text = discountViewHolder.view.context.resources.getString(R.string.discount_cost, discountsList!![i].data.getValue("points_needed").toString())
        if (proceedToViewFragment!!) {
            discountViewHolder.itemRoot.setOnClickListener {
                mCallback!!.showDiscount(discountsList!![i], userPoints!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (discountsList == null) 0 else discountsList!!.size
    }

    inner class DiscountViewHolder(var itemRoot: View) : RecyclerView.ViewHolder(itemRoot) {
        var discountTitle: TextView = itemRoot.findViewById(R.id.tvDiscountTitle)
        var discountInfo: TextView = itemRoot.findViewById(R.id.tvDiscountInfo)
        var discountPoints: TextView = itemRoot.findViewById(R.id.tvDiscountPoints)
        var view: View = itemRoot
    }

}