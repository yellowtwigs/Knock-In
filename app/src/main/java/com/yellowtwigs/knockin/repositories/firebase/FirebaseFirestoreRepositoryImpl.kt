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

        val year = dateToday.year
        val month = dateToday.monthValue
        val day = dateToday.dayOfMonth
        val hour = LocalTime.now().hour
        val minutes = LocalTime.now().minute
        val second = LocalTime.now().second

        val nano = LocalTime.now().nano

        val today = "$year-$month-$day-$hour-$minutes-$second-$nano"

        val updateMap: MutableMap<String, String> = HashMap()
        updateMap["$activityName $nano"] = "$activityName $nano"

        firebaseFirestore.collection(id).document(today).set(updateMap)
    }

    override fun setActivityNameToUserClickBis(id: String, activityName: String) {
        val dateToday = LocalDate.now()

        val year = dateToday.year
        val month = dateToday.monthValue
        val day = dateToday.dayOfMonth
        val hour = LocalTime.now().hour
        val minutes = LocalTime.now().minute
        val second = LocalTime.now().second

        val nano = LocalTime.now().nano

        val today = "$year-$month-$day-$hour-$minutes-$second-$nano"

        val updateMap: MutableMap<String, String> = HashMap()
        updateMap["$activityName $nano"] = "$activityName $nano"

        firebaseFirestore.collection("BugException-$id").document(today).set(updateMap)
    }
}