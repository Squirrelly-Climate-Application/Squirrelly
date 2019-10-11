package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.adapters.TabAdapter

/**
 * A simple [Fragment] subclass.
 */
class TabLayoutFragment : Fragment() {

    private var root: View? = null

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab_layout, container, false)

        Log.d("tester", "helloo")

        tabLayout = root!!.findViewById(R.id.tabLayout)
        viewPager = root!!.findViewById(R.id.viewPager)

        tabLayout!!.addTab(tabLayout!!.newTab().setText("Discounts"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("My Discounts"))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = TabAdapter(context!!, childFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        return root
    }

}