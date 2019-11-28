package com.example.timil.climateapplication

import android.icu.text.SimpleDateFormat
import android.text.format.DateUtils
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
        const val users = "users"
        const val discounts = "discounts"
        const val used_discounts = "used_discounts"
        const val points = "points"
        const val expiring_date = "expiring date"
        const val id = "id"
        const val used_date = "used_date"
    }

    fun updateUserDataToDb(discountId: String, userPoints: Int, discountPoints: Int, mCallback: () -> Unit){
        val docRef = db.collection(users).document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val usedDiscountData = hashMapOf(
                        id to discountId,
                        used_date to Date()
                    )

                    // update user's points
                    db.collection(users).document(userId)
                        .update(points, userPoints-discountPoints)
                        .addOnSuccessListener {
                            Log.d("tester", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener {
                                e -> Log.w("tester", "Error writing document", e)
                        }

                    // update user's used discounts
                    db.collection(users).document(userId)
                        .update(used_discounts, FieldValue.arrayUnion(usedDiscountData))
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
        db.collection(users).document(userId).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    var usedDiscountsArrayList: ArrayList<Map<String, Date>>? = null
                    if (document.get(used_discounts) != null) {
                        usedDiscountsArrayList = document.get(used_discounts) as ArrayList<Map<String, Date>>
                    }

                    db.collection(discounts).get().addOnCompleteListener { task ->
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
                                            if (usedDiscount[id].toString() == discount.id) {
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

    fun getUserData(userPoints: Int, mCallback: (Int) -> Unit){
        val docRef = db.collection(users).document(userId)
        docRef.get()
            .addOnSuccessListener { document ->

                if (document != null) {
                    var pointsToReturn = userPoints
                    // in case the user is new and doesn't have any data in Firebase yet -> give data to avoid null exceptions
                    if(document.data == null){
                        val userData = hashMapOf(
                            points to userPoints
                        )
                        db.collection(users).document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("tester", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e -> Log.w("tester", "Error writing document", e) }
                    }
                    else {
                        pointsToReturn = document.data!!.getValue(points).toString().toInt()
                    }
                    mCallback(pointsToReturn);

                } else {
                    Log.d("tester", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("tester", "get failed with ", exception)
            }

    }

    fun getDiscountsData(mCallback: (ArrayList<QueryDocumentSnapshot>) -> Unit){
        db.collection(users).document(userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    var usedDiscountsArrayList: ArrayList<Map<String, Date>>? = null
                    if (document.get(used_discounts) != null) {
                        usedDiscountsArrayList = document.get(used_discounts) as ArrayList<Map<String, Date>>
                    }

                    db.collection(discounts).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val discounts = ArrayList<QueryDocumentSnapshot>()
                            for (discountDocument in task.result!!) {
                                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
                                val expiringDate = simpleDateFormat.parse(discountDocument.data.getValue(expiring_date).toString()).time
                                if (Date().time < expiringDate || DateUtils.isToday(expiringDate)) {
                                    discounts.add(discountDocument)
                                }
                            }

                            // if user has already used some of the discounts -> do not show it on the list
                            if (usedDiscountsArrayList != null) {
                                if (usedDiscountsArrayList.size > 0) {
                                    for (usedDiscount in usedDiscountsArrayList) {
                                        for (discount in discounts) {
                                            if (usedDiscount[id].toString() == discount.id) {
                                                discounts.remove(discount)
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            mCallback(discounts)

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
