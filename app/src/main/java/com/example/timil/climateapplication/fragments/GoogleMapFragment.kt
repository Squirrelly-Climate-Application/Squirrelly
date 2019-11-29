package com.example.timil.climateapplication.fragments

import android.app.Activity
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.text.format.DateUtils
import android.util.Log
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
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*

/**
 * For showing the discount-giving companies on a map.
 * @author Ville Lohkovuori
 * */

private const val DEFAULT_ZOOM_LEVEL = 13.0f
private val HELSINKI_CITY_CENTER = LatLng(60.1742309, 24.9342355)
private val MAP_BOUNDS = LatLngBounds(LatLng(60.1609117,24.8002801), LatLng(60.2618835,25.1415221))
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
    private var activityCallBack: OnLongClick? = null

    private lateinit var discountsList: MutableList<Discount>
    private var userPoints = 0

    interface OnLongClick {
        fun showMapDiscount(discount: Discount, userPoints: Int, view: View)
    }

    companion object {
        const val DISCOUNT_COMPANY_KEY = "company"
        const val DISCOUNT_COMPANY_ADDRESS_KEY = "companyAddress"
        const val DISCOUNT_INFORMATION_KEY = "information"
        const val DISCOUNT_POINTS_KEY = "points_needed"
        const val DISCOUNT_PERCENT_KEY = "discount_percent"
        const val DISCOUNT_EXPIRING_DATE_KEY = "expiring date"
        const val DISCOUNT_LATITUDE_KEY = "latitude"
        const val DISCOUNT_LONGITUDE_KEY = "longitude"
        const val DISCOUNT_COMPANY_LOGO_KEY = "companyLogo"
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            activityCallBack = activity as OnLongClick
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnLongClick interface.")
        }
    }

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

    override fun onResume() {
        super.onResume()
        
        discountsList = ArrayList()
        getDiscountsData()
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

            googleMap.setLatLngBoundsForCameraTarget(MAP_BOUNDS) // restrict the camera to the capital area
            moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL)) // zoom in a little bit
            moveCamera(CameraUpdateFactory.newLatLng(HELSINKI_CITY_CENTER))
        }
    } // onMapReady

    //TODO: Should make a class to store all the DB methods in one place
    private fun getDiscountsData(){
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    var usedDiscountsArrayList: ArrayList<Map<String, Date>>? = null
                    if (document.get("used_discounts") != null) {
                        usedDiscountsArrayList = document.get("used_discounts") as ArrayList<Map<String, Date>>
                    }

                    if(document.get("points") != null) {
                        userPoints = (document.get("points") as Long).toInt()
                    }

                    db.collection("discounts").get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val discounts = ArrayList<QueryDocumentSnapshot>()
                            for (discountDocument in task.result!!) {
                                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
                                val expiringDate = simpleDateFormat.parse(discountDocument.data.getValue(
                                    DiscountsFragment.EXPIRING_DATE_KEY
                                ).toString()).time
                                if (Date().time < expiringDate || DateUtils.isToday(expiringDate)) {
                                    discounts.add(discountDocument)
                                }
                            }

                            // if user has already used some of the discounts -> do not show it on the list
                            if (usedDiscountsArrayList != null) {
                                if (usedDiscountsArrayList.size > 0) {
                                    for (usedDiscount in usedDiscountsArrayList) {
                                        for (discount in discounts) {
                                            if (usedDiscount["id"].toString() == discount.id) {
                                                discounts.remove(discount)
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            for (discount in discounts) {
                                discountsList.add(
                                    Discount(
                                        discount.id,
                                        discount.get(DISCOUNT_COMPANY_KEY).toString(),
                                        discount.get(DISCOUNT_COMPANY_ADDRESS_KEY).toString(),
                                        discount.get(DISCOUNT_INFORMATION_KEY).toString(),
                                        (discount.get(DISCOUNT_POINTS_KEY) as Long).toInt(),
                                        (discount.get(DISCOUNT_PERCENT_KEY) as Long).toInt(),
                                        discount.get(DISCOUNT_EXPIRING_DATE_KEY).toString(),
                                        LatLng(discount.get(DISCOUNT_LATITUDE_KEY) as Double, discount.get(DISCOUNT_LONGITUDE_KEY) as Double),
                                        discount.get(DISCOUNT_COMPANY_LOGO_KEY).toString()
                                    )
                                )
                            }

                            placeDiscountsOnMap(discountsList)

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

    private fun placeDiscountsOnMap(list: List<Discount>) {

        list.forEach {

            val marker = googleMap.addMarker(markerOptionsFrom(it))
            marker.setInfoWindowAnchor(0.5f, 0.5f)
            // marker.setAnchor(0.5f,-2f) // disabling for now, as it makes the markers drift when zooming the map
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

        activityCallBack!!.showMapDiscount(discountsList[marker?.tag as Int], userPoints, mView)

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

            val discount = discountsList[markerIndex]
            infoView.apply {

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