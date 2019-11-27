package com.example.timil.climateapplication

import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.ArrayList

class DbManager() {

    private val db: FirebaseFirestore
    private val userId: String

    init {
        println("Database manager created")
        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser!!.uid
    }

    companion object {
        const val collection_user = "users"
        const val collection_discounts = "discounts"
        const val document_used_discounts = "used_discounts"
        const val field_points = "points"
    }

    fun updateUserDataToDb(discountId: String, userPoints: Int, discountPoints: Int, mCallback: () -> Unit){
        val docRef = db.collection(collection_user).document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val usedDiscountData = hashMapOf(
                        "id" to discountId,
                        "used_date" to Date()
                    )

                    // update user's points
                    db.collection(collection_user).document(userId)
                        .update(field_points, userPoints-discountPoints)
                        .addOnSuccessListener {
                            Log.d("tester", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener {
                                e -> Log.w("tester", "Error writing document", e)
                        }

                    // update user's used discounts
                    db.collection(collection_user).document(userId)
                        .update(document_used_discounts, FieldValue.arrayUnion(usedDiscountData))
                        .addOnSuccessListener {
                            Log.d("tester", "DocumentSnapshot successfully written!")
                            mCallback()
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

    fun getUsedDiscountsData(mCallback: (ArrayList<QueryDocumentSnapshot>) -> Unit) {
        db.collection(collection_user).document(userId).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    var usedDiscountsArrayList: ArrayList<Map<String, Date>>? = null
                    if (document.get(document_used_discounts) != null) {
                        usedDiscountsArrayList = document.get(document_used_discounts) as ArrayList<Map<String, Date>>
                    }

                    db.collection(collection_discounts).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val discounts = ArrayList<QueryDocumentSnapshot>()
                            val usedDiscountDocumentsToShowOnList = ArrayList<QueryDocumentSnapshot>()
                            for (discountDocument in task.result!!) {
                                discounts.add(discountDocument)
                            }

                            if (usedDiscountsArrayList != null) {
                                if (usedDiscountsArrayList.size > 0) {
                                    for (usedDiscount in usedDiscountsArrayList) {
                                        for (discount in discounts) {
                                            if (usedDiscount["id"].toString() == discount.id) {
                                                usedDiscountDocumentsToShowOnList.add(discount)
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                            mCallback(usedDiscountDocumentsToShowOnList)

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


}
