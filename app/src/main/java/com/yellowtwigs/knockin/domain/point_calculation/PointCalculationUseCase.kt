package com.yellowtwigs.knockin.domain.point_calculation

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointCalculationUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    private val USER_POINT = "USER_POINT"
    private val USER_POINT_TIME = "USER_POINT_TIME"

    fun getStatisticsPoints(): Int {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(USER_POINT, 0)
    }

    fun setStatisticsPoints(pointsToAdd: Int) {
        val sharedPreferencesPoints: SharedPreferences = context.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        val points = sharedPreferencesPoints.getInt(USER_POINT, 0)
        val editorPoints = sharedPreferencesPoints.edit()
        editorPoints.putInt(USER_POINT, points + pointsToAdd)
        editorPoints.apply()

        val sharedPreferencesTime: SharedPreferences = context.getSharedPreferences(USER_POINT_TIME, Context.MODE_PRIVATE)
        val editor = sharedPreferencesTime.edit()
        editor.putLong(USER_POINT_TIME, localDateTimeToTimestamp(LocalDateTime.now()))
        editor.apply()
    }

    private fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}