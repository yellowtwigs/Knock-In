package com.yellowtwigs.knockin.repositories.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.yellowtwigs.knockin.model.database.data.PromotionCode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : FirebaseFirestoreRepository {

    private val ALL_PROMOTION_CODES = "ALL_PROMOTION_CODES"

    override fun getPromotionCodes(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES).addSnapshotListener { querySnapshot, error ->
            if (querySnapshot != null) {
                try {
                    val list = querySnapshot.toObjects(PromotionCode::class.java)
                    promotionCodes.value = list
                } catch (e: Exception) {
                    Log.e("PromoCodes", "error : $error")
                }
            }
        }

        return promotionCodes
    }

    override fun addPromotionToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES).document(id).set(promotionCode)
    }

    override fun editPromotionCodeToIsUsed(promotionCode: PromotionCode) {
        try {
            Log.i("PromoCodes", "FirebaseFirestoreRepositoryImpl - promotionCode : ${promotionCode}")
            Log.i("PromoCodes", "FirebaseFirestoreRepositoryImpl - promotionCode.content : ${promotionCode.content}")
            if (promotionCode.content.isNotEmpty() && promotionCode.content.isNotBlank()) {
                firebaseFirestore.collection(ALL_PROMOTION_CODES).document(promotionCode.content).set(promotionCode)
            }
        } catch (e: Error) {
            Log.i("PromoCodes", "$e")
        }
    }
}