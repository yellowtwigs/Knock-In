package com.yellowtwigs.knockin.ui.notifications

import android.app.*
import android.content.*

import android.os.Build
import com.yellowtwigs.knockin.model.service.NotificationsListenerService

class NotificationSender : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val CHANNEL_ID = "my_channel"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
//        if (!isNotificationServiceEnabled(context) && sharedPreferences.getBoolean("reminder",false)) {
//            val main_ContactsDatabase: ContactsDatabase?
////            lateinit var main_mDbWorkerThread: DbWorkerThread
////            main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
////            main_mDbWorkerThread.start()
//
//            main_ContactsDatabase = ContactsDatabase.getDatabase(context)
//            val runnableSendNotif = Runnable {
//                val list = main_ContactsDatabase!!.notificationsDao().getAllNotifications()
//                val calendar = GregorianCalendar()
//                calendar.add(Calendar.DATE, -1)
//                var nbOfnotif = 0
//                val sharedPreferences = context.getSharedPreferences("Notification_tri", Context.MODE_PRIVATE)
//                if (!sharedPreferences.getBoolean("filter_by_msg_apps", true)) {
//
//                    while (nbOfnotif <= list.size - 1 && calendar.time.before(Date(list.get(nbOfnotif).timestamp))) {
//                        nbOfnotif++
//
//                    }
//                } else {
//                    var i = 0
//                    while (i <= list.size - 1 && calendar.time.before(Date(list[i].timestamp))) {
//                        if (isMessagingApp(list[i].platform)) {
//                            nbOfnotif++
//                        }
//                        i++
//                    }
//                }
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
////                val resultIntent = Intent(context, NotificationHistoryActivity::class.java)
//                val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
////                    addNextIntentWithParentStack(resultIntent)
//                    getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
//                }
//
//                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.ic_letter_k)
//                        .setContentTitle(context.getString(R.string.notification_sender_content_title))
//                        .setContentText(String.format(context.getString(R.string.notification_sender_content_text), nbOfnotif))
//                        .setStyle(
//                                NotificationCompat.BigTextStyle()
//                                        .bigText(String.format(context.getString(R.string.notification_sender_big_text), nbOfnotif))
//                                        .setBigContentTitle(context.getString(R.string.notification_sender_content_title))
//                                        .setSummaryText(""))
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setContentIntent(pendingIntent)
//                        .setAutoCancel(true)
//
//                if (intent.action == "NOTIFICATION_TIME")
//                    manager.notify(0, notification.build())
//            }
////            main_mDbWorkerThread.postTask(runnableSendNotif)
//        }

    }

//    private fun isNotificationServiceEnabled(context: Context): Boolean {
//        val pkgName = Main2Activity::class.java.`package`!!.name
//        val str = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
//        if (!TextUtils.isEmpty(str)) {
//            val names = str.split(":")
//            for (i in names.indices) {
//                val cn = ComponentName.unflattenFromString(names[i])
//                if (cn != null) {
//                    if (TextUtils.equals(pkgName, cn.packageName)) {
//                        return true
//                    }
//                }
//            }
//        }
//        return false
//    }
}