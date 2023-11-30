package com.yellowtwigs.knockin.ui.notifications.sender

import android.app.*
import android.content.*

import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.notifications.sender.GetNotificationsForSenderUseCase
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.utils.NotificationsGesture.isMessagingApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NotificationSender : BroadcastReceiver() {

    @Inject
    lateinit var getNotificationsForSenderUseCase: GetNotificationsForSenderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val CHANNEL_ID = "my_channel"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        val sharedPreferences = context.getSharedPreferences("Notifications_Reminder", Context.MODE_PRIVATE)

        val resultIntent = Intent(context, NotificationsHistoryActivity::class.java)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        if (isNotificationServiceEnabled(context) && sharedPreferences.getBoolean("reminder", false)) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_letter_k)
                .setContentTitle(context.getString(R.string.notification_sender_content_title)).setContentText(
                    String.format(
                        context.getString(R.string.notification_sender_content_text), getNotificationsDailyNumber(context)
                    )
                ).setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        String.format(
                            context.getString(R.string.notification_sender_big_text), getNotificationsDailyNumber(context)
                        )
                    ).setBigContentTitle(context.getString(R.string.notification_sender_content_title)).setSummaryText("")
                ).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent).setAutoCancel(true)

            if (intent.action == "NOTIFICATION_TIME") manager.notify(0, notification.build())
        }
    }

    private fun getNotificationsDailyNumber(context: Context): Int {
        val list = getNotificationsForSenderUseCase.invoke()

        val calendar = GregorianCalendar()
        calendar.add(Calendar.DATE, -1)
        var numberOfNotifications = 0
        val sharedPreferences = context.getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
            while (numberOfNotifications <= list.size - 1 && calendar.time.before(Date(list.get(numberOfNotifications).timestamp))) {
                numberOfNotifications++
            }
        } else {
            var i = 0
            while (i <= list.size - 1 && calendar.time.before(Date(list[i].timestamp))) {
                if (isMessagingApp(list[i].platform, context)) {
                    numberOfNotifications++
                }
                i++
            }
        }

        return numberOfNotifications
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val str = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(str)) {
            val names = str.split(":")
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}