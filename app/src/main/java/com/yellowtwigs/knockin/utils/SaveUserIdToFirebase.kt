package com.yellowtwigs.knockin.utils

import android.content.SharedPreferences
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import java.util.*

object SaveUserIdToFirebase {

    fun saveUserIdToFirebase(userIdPreferences: SharedPreferences, viewModel: FirebaseViewModel, message:  String) {
        var userId = userIdPreferences.getString("User_Id", "Default")

        if (userId == "Default") {
            userId = UUID.randomUUID().toString()
            val userIdEdit = userIdPreferences.edit()
            userIdEdit.putString("User_Id", userId)
            userIdEdit.apply()

            viewModel.setActivityNameToUserClick(userId, message)
        } else {
            userId?.let {
                viewModel.setActivityNameToUserClick(it, message)
            }
        }
    }
}