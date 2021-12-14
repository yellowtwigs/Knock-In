package com.yellowtwigs.knockin.controller

import android.annotation.SuppressLint
import android.app.*
import android.content.*

import androidx.core.app.NotificationCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.HomeActivity
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import java.util.*

class NotificationSender : BroadcastReceiver() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context, intent: Intent) {
        println("received")
        val CHANNEL_ID = "my_channel"

        println("extras test" + intent.extras!!.toString())
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        if (!isNotificationServiceEnabled(context) && sharedPreferences.getBoolean("reminder",false)) {

            val main_ContactsDatabase: ContactsRoomDatabase?
            lateinit var main_mDbWorkerThread: DbWorkerThread
            main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
            main_mDbWorkerThread.start()
            main_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)
            val runnableSendNotif = Runnable {
                val list = main_ContactsDatabase!!.notificationsDao().getAllNotifications()
                val calendar = GregorianCalendar()
                calendar.add(Calendar.DATE, -1)
                var nbOfnotif = 0
                val sharedPreferences = context.getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
                if (!sharedPreferences.getBoolean("filtre_message", true)) {

                    while (nbOfnotif <= list.size - 1 && calendar.time.before(Date(list.get(nbOfnotif).timestamp))) {
                        nbOfnotif++

                    }
                } else {
                    var i = 0
                    while (i <= list.size - 1 && calendar.time.before(Date(list[i].timestamp))) {
                        if (isMessagingApp(list[i].platform)) {
                            nbOfnotif++
                        }
                        i++
                        println("before test nb$nbOfnotif")
                    }
                }
                println("after")
                // calendar.time.after(Date(notificationDB.timestamp))
                println("size of notif" + main_ContactsDatabase.notificationsDao().getIntTime().toString())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_letter_k)
                        .setContentTitle(context.getString(R.string.notification_sender_content_title))
                        .setContentText(String.format(context.getString(R.string.notification_sender_content_text), nbOfnotif))
                        .setStyle(
                                NotificationCompat.BigTextStyle()
                                        .bigText(String.format(context.getString(R.string.notification_sender_big_text), nbOfnotif))
                                        .setBigContentTitle(context.getString(R.string.notification_sender_content_title))
                                        .setSummaryText(""))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

                if (intent.action == "NOTIFICATION_TIME")
                    manager.notify(0, notification.build())
            }
            main_mDbWorkerThread.postTask(runnableSendNotif)

        } else {
            println("in else")
        }

    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = HomeActivity::class.java.`package`!!.name
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
    }//TODO: enlever code duplicate

    private fun isMessagingApp(packageName: String): Boolean {
        if (packageName == NotificationListener.FACEBOOK_PACKAGE) {
            return true
        } else if (packageName == NotificationListener.MESSENGER_PACKAGE) {
            return true
        } else if (packageName == NotificationListener.WHATSAPP_SERVICE) {
            return true
        } else if (packageName == NotificationListener.GMAIL_PACKAGE) {
            return true
        } else if (packageName == NotificationListener.MESSAGE_PACKAGE || packageName == NotificationListener.MESSAGE_SAMSUNG_PACKAGE) {
            return true
        } else if (packageName == NotificationListener.TELEGRAM_PACKAGE)
            return true
        return false
    }

}