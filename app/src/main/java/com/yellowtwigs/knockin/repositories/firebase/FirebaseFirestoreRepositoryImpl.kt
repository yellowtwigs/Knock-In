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

    private val ALL_PROMOTION_CODES_RELAX = "ALL_PROMOTION_CODES_RELAX"
    private val ALL_PROMOTION_CODES_JAZZY = "ALL_PROMOTION_CODES_JAZZY"
    private val ALL_PROMOTION_CODES_FUNKY = "ALL_PROMOTION_CODES_FUNKY"
    private val ALL_PROMOTION_CODES_MESSAGING = "ALL_PROMOTION_CODES_MESSAGING"
    private val ALL_PROMOTION_CODES_CONTACTS = "ALL_PROMOTION_CODES_CONTACTS"
    private val ALL_PROMOTION_CODES_FAKE = "ALL_PROMOTION_CODES_FAKE"

    override fun getPromotionCodesRelax(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_RELAX).addSnapshotListener { querySnapshot, error ->
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

    override fun getPromotionCodesJazzy(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_JAZZY).addSnapshotListener { querySnapshot, error ->
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

    override fun getPromotionCodesFunky(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_FUNKY).addSnapshotListener { querySnapshot, error ->
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

    override fun getPromotionCodesMessaging(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_MESSAGING).addSnapshotListener { querySnapshot, error ->
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

    override fun getPromotionCodesContacts(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_CONTACTS).addSnapshotListener { querySnapshot, error ->
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

    override fun getPromotionCodesFake(): LiveData<List<PromotionCode>> {
        val promotionCodes = MutableLiveData<List<PromotionCode>>(arrayListOf())

        firebaseFirestore.collection(ALL_PROMOTION_CODES_FAKE).addSnapshotListener { querySnapshot, error ->
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

    override fun addPromotionCodeRelaxToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES_RELAX).document(id).set(promotionCode)
    }

    override fun addPromotionCodeJazzyToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES_JAZZY).document(id).set(promotionCode)
    }

    override fun addPromotionCodeFakeToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES_FAKE).document(id).set(promotionCode)
    }

    override fun addPromotionCodeContactsToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES_CONTACTS).document(id).set(promotionCode)
    }

    override fun addPromotionCodeMessagingToFirestore(id: String, promotionCode: PromotionCode) {
        firebaseFirestore.collection(ALL_PROMOTION_CODES_MESSAGING).document(id).set(promotionCode)
    }

    override fun editPromotionCodeRelaxToIsUsed(promotionCode: PromotionCode) {
        try {
            if (promotionCode.content.isNotEmpty() && promotionCode.content.isNotBlank()) {
                firebaseFirestore.collection(ALL_PROMOTION_CODES_RELAX).document(promotionCode.content).set(promotionCode)
            }
        } catch (e: Error) {
            Log.i("PromoCodes", "$e")
        }
    }

    override fun editPromotionCodeJazzyToIsUsed(promotionCode: PromotionCode) {
        try {
            if (promotionCode.content.isNotEmpty() && promotionCode.content.isNotBlank()) {
                firebaseFirestore.collection(ALL_PROMOTION_CODES_JAZZY).document(promotionCode.content).set(promotionCode)
            }
        } catch (e: Error) {
            Log.i("PromoCodes", "$e")
        }
    }

    override fun editPromotionCodeMessagingToIsUsed(promotionCode: PromotionCode) {
        try {
            if (promotionCode.content.isNotEmpty() && promotionCode.content.isNotBlank()) {
                firebaseFirestore.collection(ALL_PROMOTION_CODES_MESSAGING).document(promotionCode.content).set(promotionCode)
            }
        } catch (e: Error) {
            Log.i("PromoCodes", "$e")
        }
    }
}