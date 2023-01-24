package com.yellowtwigs.knockin.repositories.firebase

import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreRepositoryImpl @Inject constructor(
    val firebaseFirestore: FirebaseFirestore
) : FirebaseFirestoreRepository {

    override fun setActivityNameToUserClick(id: String, activityName: String) {
        val dateToday = LocalDate.now()
        val nano = LocalTime.now().nano

        val updateMap: MutableMap<String, String> = HashMap()
        updateMap["$activityName $nano"] = "$activityName $nano"

        val year = dateToday.year
        val month = dateToday.monthValue
        val day = dateToday.dayOfMonth

        val today = "$year-$month-$day"

        firebaseFirestore.collection("$today-$id")
            .document("$id-$nano")
            .set(updateMap)
    }
}