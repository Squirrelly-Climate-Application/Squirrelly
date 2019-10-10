package com.example.timil.climateapplication.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.timil.climateapplication.fragments.DiscountsFragment
import com.example.timil.climateapplication.fragments.UserDiscountsFragment


class TabAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) :
    FragmentPagerAdapter(fm) {

    // fragment tabs
    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                return DiscountsFragment()
            }
            1 -> {
                return UserDiscountsFragment()
            }
            else -> return null
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}