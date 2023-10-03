package com.yellowtwigs.knockin.repositories.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.database.data.PromotionCode
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(private val firebaseFirestoreRepository: FirebaseFirestoreRepository) : ViewModel() {

    fun getPromotionCodesRelax(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesRelax()
    }

    fun getPromotionCodesJazzy(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesJazzy()
    }

    fun getPromotionCodesFunky(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesFunky()
    }

    fun getPromotionCodesMessaging(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesMessaging()
    }

    fun getPromotionCodesContacts(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesContacts()
    }

    fun getPromotionCodesFake(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodesFake()
    }

    fun addPromotionCodeRelaxToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionCodeRelaxToFirestore(promotionCode.content, promotionCode)
    }

    fun addPromotionCodeJazzyToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionCodeJazzyToFirestore(promotionCode.content, promotionCode)
    }

    fun addPromotionCodeMessagingToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionCodeMessagingToFirestore(promotionCode.content, promotionCode)
    }

    fun addPromotionCodeFakeToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionCodeFakeToFirestore(promotionCode.content, promotionCode)
    }


    fun addPromotionCodeContactsToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionCodeContactsToFirestore(promotionCode.content, promotionCode)
    }

    fun editPromotionCodeRelaxToIsUsed(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.editPromotionCodeRelaxToIsUsed(promotionCode)
    }

    fun editPromotionCodeJazzyToIsUsed(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.editPromotionCodeJazzyToIsUsed(promotionCode)
    }

    fun editPromotionCodeMessagingToIsUsed(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.editPromotionCodeMessagingToIsUsed(promotionCode)
    }
}