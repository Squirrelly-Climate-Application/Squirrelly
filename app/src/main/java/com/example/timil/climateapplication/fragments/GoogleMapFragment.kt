package com.example.timil.climateapplication.fragments

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.timil.climateapplication.Discount
import com.example.timil.climateapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

/**
 * For showing the discount-giving companies on a map.
 * @author Ville Lohkovuori
 * */

private const val DEFAULT_ZOOM_LEVEL = 13.0f
private val HELSINKI_CITY_CENTER = LatLng(60.1742309, 24.9342355)
//TODO: make latLongBounds to limit the map bounds to existing discount locations

private enum class MarkerColor(val value: Float) {

    // it's a bit ugly that it can't tell we're actually
    // using these, but ehh, what can you do :D
    AZURE(BitmapDescriptorFactory.HUE_AZURE),
    BLUE(BitmapDescriptorFactory.HUE_BLUE),
    CYAN(BitmapDescriptorFactory.HUE_CYAN),
    GREEN(BitmapDescriptorFactory.HUE_GREEN),
    MAGENTA(BitmapDescriptorFactory.HUE_MAGENTA),
    ORANGE(BitmapDescriptorFactory.HUE_ORANGE),
    RED(BitmapDescriptorFactory.HUE_RED),
    ROSE(BitmapDescriptorFactory.HUE_ROSE),
    VIOLET(BitmapDescriptorFactory.HUE_VIOLET),
    YELLOW(BitmapDescriptorFactory.HUE_YELLOW);

    companion object {

        private val values = values()
        private val rGen = Random()

        fun pickRandom(): MarkerColor {
            return values[rGen.nextInt(values.size)]
        }
    }
} // MarkerColor

class GoogleMapFragment :
    Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnInfoWindowLongClickListener
{
    private lateinit var googleMap: GoogleMap
    private lateinit var mView: View
    private lateinit var params: CoordinatorLayout.LayoutParams

    //TODO: fetch the real data from the database
    private val mockupDiscounts = listOf(
        Discount(
            "Kissantappajat Oy",
            "Murhakuja 13",
            "totally legal",
            400,
            30,
            "31.12.2019",
            LatLng(60.176961, 24.926007),
            "sdfsfda"
        ),
        Discount(
            "Oy Infect Ab",
            "Tippurikatu 69",
            "tautisen hyvä mesta",
            200,
            20,
            "31.12.2100",
            LatLng(60.1595731,24.9464303),
            "sdfsdfa"
        ),
        Discount(
            "Savupiippu Oy",
            "Savusumunkatu 01",
            "vain parasta saastetta",
            1000,
            5,
            "31.12.2100",
            LatLng(60.1837555,24.9699441),
            "sdgdsfg"
        )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mView = inflater.inflate(R.layout.fragment_googlemap, container, false)

        params = container!!.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = null
        mView.requestLayout()

        val mapFragment = childFragmentManager.findFragmentById(R.id.googlemap_support_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        return mView
    }

    override fun onDestroy() {
        super.onDestroy()
        params.behavior = AppBarLayout.ScrollingViewBehavior()
        mView.requestLayout()
    }

    // called when mapFragment.getMapAsync() returns in onCreateView()
    override fun onMapReady(map: GoogleMap) {

        googleMap = map
        googleMap.apply {

            setInfoWindowAdapter(CustomInfoWindowAdapter())
            setOnInfoWindowClickListener(this@GoogleMapFragment)
            setOnInfoWindowLongClickListener(this@GoogleMapFragment)

            moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL)) // zoom in a little bit
            moveCamera(CameraUpdateFactory.newLatLng(HELSINKI_CITY_CENTER))
        }
        placeDiscountsOnMap(mockupDiscounts)
    } // onMapReady

    private fun placeDiscountsOnMap(list: List<Discount>) {

        list.forEach {

            val marker = googleMap.addMarker(markerOptionsFrom(it))
            marker.setInfoWindowAnchor(0f, -0.4f)
            marker.tag = list.indexOf(it) // we need to 'remember' the marker to show the info window correctly
        }
    } // placeDiscountsOnMap

    private fun markerOptionsFrom(discount: Discount): MarkerOptions {

        return MarkerOptions()
            .color(randomMarkerColor())
            .position(discount.geoLocation)
    }

    //TODO: make it take into account already used colors
    private fun randomMarkerColor(): Float {

        return MarkerColor.pickRandom().value
    }

    // extension function to simplify things
    fun MarkerOptions.color(hue: Float): MarkerOptions {
        return this.icon(BitmapDescriptorFactory.defaultMarker(hue))
    }

    // we need to use this to close the info window, because it's unreliable to
    // do it by clicking on the marker
    override fun onInfoWindowClick(marker: Marker) {

        marker.hideInfoWindow()
    }

    override fun onInfoWindowLongClick(marker: Marker?) {

        //TODO: go to the discounts view and view the discount that was in the window
    }

    // we need this to show more info than the company name and address
    private inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker?): View {

            val infoView = layoutInflater.inflate(R.layout.custom_info_window, null)

            // unsafe, but I'm not sure how to deal with the Any? type; even with a cast to Int,
            // it complains about being used in if-checks
            render(marker?.tag as Int, infoView)
            return infoView
        }

        override fun getInfoContents(unUsed: Marker?): View? {

            // this method is used to customize only the *content* of the InfoWindow;
            // we return null to signal that we don't use this
            return null
        }

        private fun render(markerIndex: Int, infoView: View) {

            val discount = mockupDiscounts[markerIndex]
            infoView.apply {

                alpha = 0.5f // doesn't seem to have any effect (nor does setting it in the xml)
                findViewById<TextView>(R.id.tv_company).text = discount.companyName
                findViewById<TextView>(R.id.tv_address).text = discount.companyAddress
                findViewById<TextView>(R.id.tv_points).text = getString(R.string.txt_points, discount.pointsNeeded)
                findViewById<TextView>(R.id.tv_discount_percent).text = getString(R.string.txt_discount_percent, discount.discountPercent)
                findViewById<TextView>(R.id.tv_info).text = discount.information
                findViewById<TextView>(R.id.tv_expire_date).text = getString(R.string.txt_expires, discount.expiringDate)
            }
        } // render

    } // CustomInfoWindowAdapter

} // GoogleMapFragment