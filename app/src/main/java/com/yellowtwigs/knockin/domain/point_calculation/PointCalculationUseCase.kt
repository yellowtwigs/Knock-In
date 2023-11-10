package com.yellowtwigs.knockin.domain.point_calculation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointCalculationUseCase @Inject constructor(@ApplicationContext private val context: Context) {



    private fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}