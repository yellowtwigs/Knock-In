package com.yellowtwigs.knockin.di

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yellowtwigs.knockin.background.StatisticsPointWorker
import com.yellowtwigs.knockin.background.alarm.AlarmManagerHelper
import com.yellowtwigs.knockin.background.service.NotificationsListenerService
import dagger.hilt.android.HiltAndroidApp
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var alarmManagerHelper: AlarmManagerHelper

    override fun onCreate() {
        super.onCreate()

        val targetTime = Calendar.getInstance()
        targetTime.set(Calendar.HOUR_OF_DAY, 23)
        targetTime.set(Calendar.MINUTE, 59)
        targetTime.set(Calendar.SECOND, 0)

        val initialDelay: Long = targetTime.timeInMillis - System.currentTimeMillis()

        val repeatingRequest = PeriodicWorkRequestBuilder<StatisticsPointWorker>(repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(repeatingRequest)

        try {
            startService(Intent(this, NotificationsListenerService::class.java))
        } catch (e: Exception) {
            Log.e("ErrorCode", "Exception : $e")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}