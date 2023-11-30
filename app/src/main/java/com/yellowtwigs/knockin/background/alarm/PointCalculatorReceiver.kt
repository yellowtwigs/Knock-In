package com.yellowtwigs.knockin.background.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.get_number.GetNumberOfContactsUseCase
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.history.NotificationParams
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListViewState
import com.yellowtwigs.knockin.utils.NotificationsGesture
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class PointCalculatorReceiver @Inject constructor(
    private val applicationContext: Context,
    private val notificationsRepository: NotificationsRepository,
    private val getNumberOfContactsUseCase: GetNumberOfContactsUseCase
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("GetNotification", "Passe par là : onReceive()")

        if (intent?.action == "MY_CUSTOM_ACTION") {
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

                val allVipNumbers = notificationsAfterDistinct.filter { notification ->
                    notification.priority == 2
                }.size

                val allMessagingNumbers = notificationsAfterDistinct.size
                val numberOfContacts = getNumberOfContactsUseCase.invoke()
                val numberOfContactsVIP = numberOfContacts.numberOfVips
                val numberOfContactsStandard = numberOfContacts.numberOfStandard
                val numberOfContactsSilent = numberOfContacts.numberOfSilent

                val nonVipNotificationsNumbers = allMessagingNumbers.minus(allVipNumbers)
                val numberOfContactsTotal = numberOfContactsStandard.plus(numberOfContactsVIP).plus(numberOfContactsSilent)
                val isAllOtherContactsSilent = numberOfContactsTotal.minus(numberOfContactsVIP) == numberOfContactsSilent

                val points = if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                    0
                } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                    2
                } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                    4
                } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                    7
                } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                    12
                } else {
                    0
                }

                val adviceMessage = if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                    applicationContext.getString(R.string.strong_red_advice)
                } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                    applicationContext.getString(R.string.orange_advice)
                } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                    applicationContext.getString(R.string.yellow_advice)
                } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                    applicationContext.getString(R.string.light_green_advice)
                } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                    applicationContext.getString(R.string.strong_green_advice)
                } else {
                    applicationContext.getString(R.string.yellow_advice)
                }

            } catch (e: Exception) {
                Log.i("GetNotification", "Exception : $e")
            }
        }
    }

    private fun addNotificationInListDaily(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(applicationContext) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
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
                        AppCompatResources.getDrawable(applicationContext, R.drawable.rounded_form_layout),
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

            val configuration = applicationContext.resources.configuration
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