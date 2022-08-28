package com.yellowtwigs.knockin.di

import android.app.Application
import android.content.Intent
import com.yellowtwigs.knockin.model.service.NotificationsListenerService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startService(Intent(this, NotificationsListenerService::class.java))
    }
}