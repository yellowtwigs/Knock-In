package com.yellowtwigs.knockin.repositories.firebase

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.PromotionCode

interface FirebaseFirestoreRepository {

    fun getPromotionCodesRelax(): LiveData<List<PromotionCode>>
    fun getPromotionCodesJazzy(): LiveData<List<PromotionCode>>
    fun getPromotionCodesFunky(): LiveData<List<PromotionCode>>
    fun getPromotionCodesMessaging(): LiveData<List<PromotionCode>>
    fun getPromotionCodesContacts(): LiveData<List<PromotionCode>>
    fun getPromotionCodesFake(): LiveData<List<PromotionCode>>

    fun addPromotionCodeRelaxToFirestore(id: String, promotionCode: PromotionCode)
    fun addPromotionCodeJazzyToFirestore(id: String, promotionCode: PromotionCode)
    fun addPromotionCodeMessagingToFirestore(id: String, promotionCode: PromotionCode)

    fun editPromotionCodeRelaxToIsUsed(promotionCode: PromotionCode)
    fun editPromotionCodeJazzyToIsUsed(promotionCode: PromotionCode)
    fun editPromotionCodeMessagingToIsUsed(promotionCode: PromotionCode)
    fun addPromotionCodeFakeToFirestore(id: String, promotionCode: PromotionCode)
    fun addPromotionCodeContactsToFirestore(id: String, promotionCode: PromotionCode)
}