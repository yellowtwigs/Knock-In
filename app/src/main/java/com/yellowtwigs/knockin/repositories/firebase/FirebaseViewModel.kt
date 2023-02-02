package com.yellowtwigs.knockin.repositories.firebase

import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(private val firebaseFirestoreRepository: FirebaseFirestoreRepository) : ViewModel() {

    fun setActivityNameToUserClick(id: String, activityName: String) {
        firebaseFirestoreRepository.setActivityNameToUserClick(id, activityName)
    }
}