package com.example.timil.climateapplication.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.timil.climateapplication.R

class DiscountsRecyclerAdapter(private val discounts: MutableList<String>, activity: Activity)
    : RecyclerView.Adapter<DiscountsRecyclerAdapter.DiscountViewHolder>() {

    private var mCallback: OnDiscountClick? = null

    // for testing
    private var discountTitles: MutableList<String>? = null
    private var discountInformations: MutableList<String>? = null

    interface OnDiscountClick {
        fun showDiscount()
    }

    init {
        try {
            this.mCallback = activity as OnDiscountClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnDiscountClick interface.")
        }
    }

    fun setDiscounts(discounts: MutableList<String>, discounts2: MutableList<String>) {
        discountTitles = discounts
        discountInformations = discounts2

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DiscountViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.discount_recycler_view_item, viewGroup, false)
        return DiscountViewHolder(view)
    }

    override fun onBindViewHolder(discountViewHolder: DiscountViewHolder, i: Int) {
        discountViewHolder.discountTitle.setText(discountTitles!![i])
        discountViewHolder.discountInfo.setText(discountInformations!![i])
        discountViewHolder.itemRoot.setOnClickListener {
            mCallback!!.showDiscount()
        }
    }

    override fun getItemCount(): Int {
        return discountTitles!!.size
    }

    inner class DiscountViewHolder(var itemRoot: View) : RecyclerView.ViewHolder(itemRoot) {
        var discountTitle: TextView = itemRoot.findViewById(R.id.tvDiscountTitle)
        var discountInfo: TextView = itemRoot.findViewById(R.id.tvDiscountInfo)
    }

}