package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.timil.climateapplication.DbManager
import com.example.timil.climateapplication.OnDiscountUseListener
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.SwipeButton
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_COMPANY_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_INFORMATION_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.DISCOUNT_POINTS_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.EXPIRING_DATE_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.SHARED_ELEMENT_KEY
import com.example.timil.climateapplication.fragments.DiscountsFragment.Companion.USER_POINTS_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ViewDiscountFragment : Fragment() {

    private lateinit var root: View
    private lateinit var discountCompanyTv: TextView
    private lateinit var discountInformationTv: TextView
    private lateinit var discountPointsTv: TextView
    private lateinit var expiringDateTv: TextView
    //private var btnUseDiscount: Button? = null
    private lateinit var btnUseDiscountSwipe: SwipeButton
    private lateinit var linearLayoutCardView: LinearLayout

    private val dbManager = DbManager()
    private var userPoints: Int? = 0
    private var discountPoints: Int? = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = TransitionInflater
            .from(context).inflateTransition(
                android.R.transition.move
            )
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_view_discount, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()

        discountCompanyTv = root.findViewById(R.id.tvViewDiscountTitle)
        discountInformationTv = root.findViewById(R.id.tvViewDiscountInfo)
        discountPointsTv = root.findViewById(R.id.tvViewDiscountPoints)
        expiringDateTv = root.findViewById(R.id.tvViewExpiringDate)
        //btnUseDiscount = root!!.findViewById(R.id.btnUseDiscount)
        btnUseDiscountSwipe = root.findViewById(R.id.btnUseDiscountSwipe)
        linearLayoutCardView = root.findViewById(R.id.linearLayoutCardView)

        val bundle = arguments
        try {
            discountPoints = bundle!!.getInt(DISCOUNT_POINTS_KEY)
            userPoints = bundle.getInt(USER_POINTS_KEY)

            discountCompanyTv.text = bundle.getString(DISCOUNT_COMPANY_KEY)
            discountInformationTv.text = bundle.getString(DISCOUNT_INFORMATION_KEY)
            discountPointsTv.text = discountPoints.toString()
            expiringDateTv.text = bundle.getString(EXPIRING_DATE_KEY)

            linearLayoutCardView.transitionName = bundle.getString(SHARED_ELEMENT_KEY)!!
            /*
            btnUseDiscount!!.setOnClickListener {
                if( userPoints!! >=  discountPoints!!){
                    updateUserDataToDb(bundle)
                } else {
                    Toast.makeText(context, "Not enough points to activate the discount!", Toast.LENGTH_LONG).show()
                }
            }
            */
            btnUseDiscountSwipe.setOnDiscountUseListener(object : OnDiscountUseListener {

                override fun onDiscountUse(): Boolean {
                    return if( userPoints!! >=  discountPoints!!){
                        dbManager.updateUserDataToDb(bundle.getString("discountId")!!, userPoints!!, discountPoints!!) {
                            fragmentManager!!.popBackStack()
                        }
                        true
                    } else {
                        Toast.makeText(context, "Not enough points to activate the discount!", Toast.LENGTH_LONG).show()
                        false
                    }
                }
            })

        } catch (err: Exception) {
            Log.d("TEST", "No bundle data")
        }

    }

}
