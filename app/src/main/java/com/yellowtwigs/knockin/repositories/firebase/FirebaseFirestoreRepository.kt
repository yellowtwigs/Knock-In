package com.yellowtwigs.knockin.repositories.firebase

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.PromotionCode

interface FirebaseFirestoreRepository {

    fun getPromotionCodes(): LiveData<List<PromotionCode>>
    fun addPromotionToFirestore(id: String, promotionCode: PromotionCode)
    fun editPromotionCodeToIsUsed(promotionCode: PromotionCode)
}