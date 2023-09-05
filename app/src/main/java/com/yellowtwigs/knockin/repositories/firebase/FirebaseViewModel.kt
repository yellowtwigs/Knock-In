package com.yellowtwigs.knockin.repositories.firebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.database.data.PromotionCode
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(private val firebaseFirestoreRepository: FirebaseFirestoreRepository) : ViewModel() {

    fun getPromotionsCodes(): LiveData<List<PromotionCode>> {
        return firebaseFirestoreRepository.getPromotionCodes()
    }

    fun addPromotionCodeToFirestore(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.addPromotionToFirestore(promotionCode.content, promotionCode)
    }

    fun editPromotionCodeToIsUsed(promotionCode: PromotionCode) {
        firebaseFirestoreRepository.editPromotionCodeToIsUsed(promotionCode)
    }


}