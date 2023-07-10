package com.yellowtwigs.knockin.di

import android.app.Application
import android.content.Intent
import android.util.Log
import com.yellowtwigs.knockin.model.service.NotificationsListenerService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            startService(Intent(this, NotificationsListenerService::class.java))
        } catch (e: Exception) {
            Log.e("ErrorCode", "Exception : $e")
        }
    }
}