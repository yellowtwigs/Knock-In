package com.yellowtwigs.knockin.domain.point_calculation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointCalculationUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    private val USER_POINT = "USER_POINT"

    fun getStatisticsPoints(): Int {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(USER_POINT, 0)
    }

    fun setStatisticsPoints(pointsToAdd: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        val points = sharedPreferences.getInt(USER_POINT, 0)
        val editor = sharedPreferences.edit()
        editor.putInt(USER_POINT, points + pointsToAdd)
        editor.apply()
    }
}