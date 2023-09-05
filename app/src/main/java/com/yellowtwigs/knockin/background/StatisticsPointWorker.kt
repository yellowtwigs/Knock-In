package com.yellowtwigs.knockin.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.get_number.GetNumberOfContactsUseCase
import com.yellowtwigs.knockin.domain.point_calculation.PointCalculationUseCase
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.history.NotificationParams
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListViewState
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.ui.statistics.dashboard.DashboardActivity
import com.yellowtwigs.knockin.utils.NotificationsGesture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@HiltWorker
class StatisticsPointWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pointCalculationUseCase: PointCalculationUseCase,
    private val notificationsRepository: NotificationsRepository,
    private val getNumberOfContactsUseCase: GetNumberOfContactsUseCase
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        var result = Result.failure()

        try {
            val notifications = arrayListOf<NotificationsListViewState>()
            notificationsRepository.getAllNotificationsList().map { notification ->
                addNotificationInListDaily(notifications, notification)
            }

            val notificationsAfterDistinct = notifications.distinctBy {
                NotificationParams(
                    contactName = it.contactName,
                    description = it.description,
                    platform = it.platform,
                    date = it.date,
                    idContact = it.idContact,
                    priority = it.priority,
                    phoneNumber = it.phoneNumber,
                    mail = it.mail,
                    isSystem = it.isSystem
                )
            }

            val allVipNotifications = notificationsAfterDistinct.filter { notification ->
                notification.priority == 2
            }.size

            val allMessagingNumbers = notificationsAfterDistinct.size
            val numberOfContacts = getNumberOfContactsUseCase.invoke()
            val numberOfContactsVIP = numberOfContacts.numberOfVips
            val numberOfContactsStandard = numberOfContacts.numberOfStandard
            val numberOfContactsSilent = numberOfContacts.numberOfSilent

            val nonVipNotificationsNumbers = allMessagingNumbers.minus(allVipNotifications)
            val numberOfContactsTotal = numberOfContactsStandard.plus(numberOfContactsVIP).plus(numberOfContactsSilent)
            val isAllOtherContactsSilent = numberOfContactsTotal.minus(numberOfContactsVIP) == numberOfContactsSilent

            val points: Int
            val adviceMessage: String

            if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                points = 0
                adviceMessage = context.getString(R.string.strong_red_advice)
            } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                points = 12
                adviceMessage = context.getString(R.string.strong_green_advice)
            } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                points = 7
                adviceMessage = context.getString(R.string.light_green_advice)
            } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                points = 2
                adviceMessage = context.getString(R.string.orange_advice)
            } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                points = 4
                adviceMessage = context.getString(R.string.yellow_advice)
            } else {
                points = 0
                adviceMessage = context.getString(R.string.yellow_advice)
            }

            val sharedPreferences: SharedPreferences = context.getSharedPreferences("USER_POINT_TIME", Context.MODE_PRIVATE)
            val time = sharedPreferences.getLong("USER_POINT_TIME", 0L)

            Log.i("GetNotification", "time : $time")
            Log.i("GetNotification", "!compareIfDateIsToday(time) : ${!compareIfDateIsToday(time)}")
            if (!compareIfDateIsToday(time)) {
                pointCalculationUseCase.setStatisticsPoints(points)
                buildNotificationWorker(adviceMessage, points, pointCalculationUseCase.getStatisticsPoints())
            }
            result = Result.success()
        } catch (e: Exception) {
            Log.i("GetNotification", "Exception : $e")
        }

        return result
    }

    private fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    private fun compareIfDateIsToday(timestamp: Long): Boolean {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        val currentDate = LocalDateTime.now()

        Log.i("GetNotification", "date.dayOfMonth : ${date.dayOfMonth}")
        Log.i("GetNotification", "date.month : ${date.month}")
        Log.i("GetNotification", "date.year : ${date.year}")

        Log.i("GetNotification", "currentDate.dayOfMonth : ${currentDate.dayOfMonth}")
        Log.i("GetNotification", "currentDate.month : ${currentDate.month}")
        Log.i("GetNotification", "currentDate.year : ${currentDate.year}")

        return date.dayOfMonth == currentDate.dayOfMonth && date.year == currentDate.year && date.month == currentDate.month
    }

    private fun buildNotificationWorker(adviceMessage: String, points: Int, statisticsPoints: Int) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val CHANNEL_ID = "CHANNEL_ID"
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val intent: Intent = DailyStatisticsActivity.navigate(context)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(applicationContext).addNextIntentWithParentStack(intent)
            .getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        notificationManager.notify(
            1,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID).setSmallIcon(R.drawable.ic_knockin_logo)
                .setContentTitle(adviceMessage)
                .setContentText("Today with the way you set your contacts, you receive : $points points. Your total is $statisticsPoints")
                .setStyle(NotificationCompat.BigTextStyle()).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true).build()
        )
//        }

    }

    private fun addNotificationInListDaily(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(context) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
        } else {
            if (compareIfNotificationDateIsToday(notification.timestamp)) {
                val phoneNumber = if (notification.phoneNumber.contains(":")) {
                    notification.phoneNumber.split(":")[1]
                } else {
                    notification.phoneNumber
                }

                notifications.add(
                    NotificationsListViewState(
                        notification.id,
                        notification.title,
                        notification.contactName,
                        notification.description,
                        notification.platform,
                        notification.timestamp,
                        SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notification.timestamp)),
                        notification.idContact,
                        notification.priority,
                        phoneNumber,
                        notification.mail,
                        notification.isSystem,
                        systemPriority,
                        AppCompatResources.getDrawable(context, R.drawable.rounded_form_layout),
                        null
                    )
                )
            }
        }
    }

    private fun compareIfNotificationDateIsToday(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val configuration = context.resources.configuration
            val notificationDate = if (configuration.locales.get(0).language == "ar") {
                translateToEnglish(SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))) ?: date
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))
            }

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt()
            val todayMonth = dateToday?.get(1)?.toInt()
            val todayDay = dateToday?.get(2)?.split(" ")?.get(0)

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]

            return if (notificationYear != todayYear || notificationMonth != todayMonth) {
                false
            } else {
                notificationDay == todayDay
            }
        } catch (e: Exception) {
            Log.i("GetLocalDateTime", "Exception : $e")
        }
        return false
    }

    private fun translateToEnglish(arabicDateTime: String): String? {
        try {
            val arabicFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar"))
            val arabicDate = arabicFormat.parse(arabicDateTime)
            val englishFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            return arabicDate?.let { englishFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }
}