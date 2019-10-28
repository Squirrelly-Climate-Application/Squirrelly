package com.example.timil.climateapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.timil.climateapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ViewDiscountFragment : Fragment() {

    private var root: View? = null
    private var discountCompanyTv: TextView? = null
    private var discountInformationTv: TextView? = null
    private var discountPointsTv: TextView? = null
    private var btnUseDiscount: Button? = null

    private var userPoints: Int? = 0
    private var discountPoints: Int? = 0

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

        discountCompanyTv = root!!.findViewById(R.id.tvViewDiscountTitle)
        discountInformationTv = root!!.findViewById(R.id.tvViewDiscountInfo)
        discountPointsTv = root!!.findViewById(R.id.tvViewDiscountPoints)
        btnUseDiscount = root!!.findViewById(R.id.btnUseDiscount)

        val bundle = arguments
        try {
            discountPoints = bundle!!.getInt("discountPoints")
            userPoints = bundle.getInt("userPoints")

            discountCompanyTv!!.text = bundle.getString("discountCompany")
            discountInformationTv!!.text = bundle.getString("discountInformation")
            discountPointsTv!!.text = discountPoints.toString()

            btnUseDiscount!!.setOnClickListener {
                if( userPoints!! >=  discountPoints!!){
                    updateUserDataToDb(bundle)
                } else {
                    Toast.makeText(context, "Not enough points to activate the discount!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (err: Exception) {
            Log.d("TEST", "No bundle data")
        }

    }

    private fun updateUserDataToDb(bundle: Bundle){
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val docRef = db.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->

                if (document != null) {
                    val usedDiscountData = hashMapOf(
                        "id" to bundle.getString("discountId"),
                        "used_date" to Date()
                    )

                    // update user's points
                    db.collection("users").document(userId)
                        .update("points", userPoints!!-discountPoints!!)
                        .addOnSuccessListener {
                            Log.d("tester", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener {
                                e -> Log.w("tester", "Error writing document", e)
                        }

                    // update user's used discounts
                    db.collection("users").document(userId)
                        .update("used_discounts", FieldValue.arrayUnion(usedDiscountData))
                        .addOnSuccessListener {
                            Log.d("tester", "DocumentSnapshot successfully written!")
                            fragmentManager!!.popBackStack()
                        }
                        .addOnFailureListener {
                                e -> Log.w("tester", "Error writing document", e)
                        }
                } else {
                    Log.d("tester", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("tester", "get failed with ", exception)
            }
    }

}
