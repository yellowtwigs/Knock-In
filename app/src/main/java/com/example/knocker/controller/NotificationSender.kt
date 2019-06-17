package com.example.knocker.controller

import android.app.*
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

import androidx.core.app.NotificationCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import java.util.*


class NotificationSender : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("received")
        val CHANNEL_ID="my_channel"

        println("extras test"+intent.extras.toString())
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        if(!isNotificationServiceEnabled(context)) {

            var main_ContactsDatabase: ContactsRoomDatabase? = null
            lateinit var main_mDbWorkerThread: DbWorkerThread
            main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
            main_mDbWorkerThread.start()
            main_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)
            val runnableSendNotif = Runnable {
                val nbOfnotif = main_ContactsDatabase!!.notificationsDao().getNotificationSinceYesterday()
                println("size of notif" + main_ContactsDatabase!!.notificationsDao().getIntTime().toString())
                val intentSender = Intent(context, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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

            if (intent.action.equals("NOTIFICAION_TIME"))
                manager.notify(0, notification.build())
            }
            main_mDbWorkerThread.postTask(runnableSendNotif)

        }else{
            println("in else")
        }

    }
    private fun isNotificationServiceEnabled(context:Context): Boolean {
            val pkgName = MainActivity::class.java.`package`.name
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

}
