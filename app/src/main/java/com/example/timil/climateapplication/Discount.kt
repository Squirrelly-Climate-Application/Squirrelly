package com.example.timil.climateapplication

import com.google.android.gms.maps.model.LatLng

/**
 * Class that is the object equivalent of the discount records that are being stored in the database.
 * @author Ville Lohkovuori
 * */

data class Discount(

    val id: String,
    val companyName: String,
    val companyAddress: String,
    val information: String,
    val pointsNeeded: Int,
    val discountPercent: Int,
    val expiringDate: String,
    val geoLocation: LatLng,
    val companyLogo: String,
    val webLink: String
)