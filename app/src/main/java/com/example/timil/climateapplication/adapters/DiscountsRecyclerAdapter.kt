package com.example.timil.climateapplication.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_COMPANY_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_COMPANY_LOGO_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_INFORMATION_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_POINTS_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_WEB_LINK_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.EXPIRING_DATE_KEY
import com.google.firebase.firestore.QueryDocumentSnapshot
import android.view.animation.Animation
import android.view.animation.ScaleAnimation





class DiscountsRecyclerAdapter(private val discounts: MutableList<String>, activity: Activity)
    : RecyclerView.Adapter<DiscountsRecyclerAdapter.DiscountViewHolder>() {

    private var mCallback: OnDiscountClick? = null

    private var discountsList: MutableList<QueryDocumentSnapshot>? = null
    private var userPoints: Int? = null
    private var proceedToViewFragment: Boolean? = null

    private lateinit var view: View

    var duration: Long = 800

    interface OnDiscountClick {
        fun showDiscount(view: View, document: QueryDocumentSnapshot, userPoints: Int)
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
        view = LayoutInflater.from(viewGroup.context).inflate(R.layout.discount_recycler_view_item, viewGroup, false)
        return DiscountViewHolder(view)
    }

    override fun onBindViewHolder(discountViewHolder: DiscountViewHolder, i: Int) {
        Glide.with((view.context as Activity))
            .load(discountsList!![i].data.getValue(DISCOUNT_COMPANY_LOGO_KEY).toString())
            .into(discountViewHolder.discountLogo)
        /*
        discountViewHolder.discountWebLink.text = discountsList!![i].data.getValue(DISCOUNT_WEB_LINK_KEY).toString()
            .replace("https://".toRegex(), "")
            .replace("http://".toRegex(), "")

        discountViewHolder.discountWebLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(discountsList!![i].data.getValue(DISCOUNT_WEB_LINK_KEY).toString()))
            (view.context as Activity).startActivity(browserIntent)
        }
        */
        discountViewHolder.discountTitle.text = discountsList!![i].data.getValue(DISCOUNT_COMPANY_KEY).toString()
        //discountViewHolder.discountInfo.text = discountsList!![i].data.getValue(DISCOUNT_INFORMATION_KEY).toString()
        discountViewHolder.discountPoints.text = discountViewHolder.view.context.resources.getString(R.string.discount_cost, discountsList!![i].data.getValue(DISCOUNT_POINTS_KEY).toString())
        discountViewHolder.expiringDate.text = discountsList!![i].data.getValue(EXPIRING_DATE_KEY).toString()
        discountViewHolder.view.transitionName = discountsList!![i].id
        if (proceedToViewFragment!!) {
            discountViewHolder.itemRoot.setOnClickListener {
                mCallback!!.showDiscount(discountViewHolder.view, discountsList!![i], userPoints!!)
            }
        }
        discountViewHolder.animationScaleIn( discountViewHolder.itemRoot )
    }

    override fun getItemCount(): Int {
        return if (discountsList == null) 0 else discountsList!!.size
    }

    inner class DiscountViewHolder(var itemRoot: View) : RecyclerView.ViewHolder(itemRoot) {
        var discountLogo: ImageView = itemRoot.findViewById(R.id.image_view_company_logo)
        var discountTitle: TextView = itemRoot.findViewById(R.id.tvDiscountTitle)
        //var discountWebLink: TextView = itemRoot.findViewById(R.id.tvDiscountWebLink)
        //var discountInfo: TextView = itemRoot.findViewById(R.id.tvDiscountInfo)
        var discountPoints: TextView = itemRoot.findViewById(R.id.tvDiscountPoints)
        var expiringDate: TextView = itemRoot.findViewById(R.id.tvExpiringDate)
        var view: View = itemRoot

        fun animationScaleIn(view: View) {
            val animationScaleIn = ScaleAnimation(
                0.0f,
                1.0f,
                0.0f,
                1.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )

            animationScaleIn.duration = duration
            duration += 400

            view.startAnimation(animationScaleIn)
        }
    }

}