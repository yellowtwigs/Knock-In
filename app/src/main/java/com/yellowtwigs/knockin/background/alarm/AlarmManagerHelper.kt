package com.yellowtwigs.knockin.background.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerHelper @Inject constructor(private val context: Context) {

    fun setRepeatingAlarm() {
        Log.i("GetNotification", "Passe par là : setRepeatingAlarm()")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, PointCalculatorReceiver::class.java)
        alarmIntent.action = "MY_CUSTOM_ACTION"
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

        // Calculate the trigger time for 23:59
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val triggerTimeMillis = calendar.timeInMillis

        // Set the repeating alarm at 23:59 every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
//            triggerTimeMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}