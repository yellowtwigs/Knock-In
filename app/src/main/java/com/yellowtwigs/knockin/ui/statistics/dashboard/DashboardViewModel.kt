package com.yellowtwigs.knockin.ui.statistics.dashboard

import android.app.Application
import android.graphics.Color
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import com.yellowtwigs.knockin.utils.NotificationsGesture
import com.yellowtwigs.knockin.utils.NotificationsGesture.MESSAGE_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.MESSENGER_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.SIGNAL_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.TELEGRAM_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.WHATSAPP_APP_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : ViewModel() {

    val spinnerSelectedItemFlow = MutableStateFlow(0)

    /* fun convertPackageToString(packageName: String, context: Context): String {
        when (packageName) {
            NotificationsGesture.FACEBOOK_PACKAGE -> return "Facebook"
            NotificationsGesture.MESSENGER_PACKAGE -> return "Messenger"
            NotificationsGesture.WHATSAPP_PACKAGE -> return "WhatsApp"
            NotificationsGesture.GMAIL_PACKAGE -> return "Gmail"
            NotificationsGesture.OUTLOOK_PACKAGE -> return "Outlook"

            NotificationsGesture.MESSAGE_PACKAGE -> return "Message"
            NotificationsGesture.XIAOMI_MESSAGE_PACKAGE -> return "Message"
            NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE -> return "Message"
            NotificationsGesture.MESSAGES_PACKAGE -> return "Message"
            Telephony.Sms.getDefaultSmsPackage(context) -> return "Message"

            NotificationsGesture.SIGNAL_PACKAGE -> return "Signal"
            NotificationsGesture.TELEGRAM_PACKAGE -> return "Telegram"
            NotificationsGesture.INSTAGRAM_PACKAGE -> return "Instagram"
            NotificationsGesture.DISCORD_PACKAGE -> return "Discord"
            NotificationsGesture.TIKTOK_PACKAGE -> return "Tiktok"
            NotificationsGesture.SNAPCHAT_PACKAGE -> return "Snapchat"
            NotificationsGesture.REDDIT_PACKAGE -> return "Reddit"
            NotificationsGesture.VIBER_PACKAGE -> return "Viber"
            NotificationsGesture.YOUTUBE_PACKAGE -> return "YouTube"
            NotificationsGesture.GOOGLE_PACKAGE -> return "Google"
            NotificationsGesture.SCREEN_RECORDER -> return "Screen Recorder"
            else -> return ""
        }
    } */

    // All
    // SMS
    // Whatsapp
    // Messenger
    // Telegram
    // Signal
    // Outlook
    // Gmail

    val dashboardViewStateLiveData: LiveData<DashboardViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(notificationsRepository.getAllNotifications().asFlow(), spinnerSelectedItemFlow) { list, selectItem ->
            val notifications = arrayListOf<PieChartDataViewState>()

            val platform = when (selectItem) {
                0 -> "ALL"
                1 -> "VIP"
                else -> "ALL"
            }

            val allVipNumbers = list.filter { notification ->
                if (platform != "") {
                    if (platform == "VIP") {
                        notification.isSystem == 0 && notification.priority == 2 && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
                            application
                        )
                    } else {
                        notification.isSystem == 0 && notification.platform == platform && notification.priority == 2
                    }
                } else {
                    notification.isSystem == 0 && notification.priority == 2
                }
            }.size

            val allMessagingNumbers = list.filter { notification ->
                if (platform != "") {
                    if (platform == "ALL") {
                        notification.isSystem == 0 && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
                            application
                        )
                    } else {
                        notification.isSystem == 0 && notification.platform == platform
                    }
                } else {
                    notification.isSystem == 0
                }
            }.size

            Log.i("GetSpinnerItem", "selectItem : $selectItem")

            val allOrVip = when (selectItem) {
                0 -> "ALL"
                1 -> "VIP"
                else -> "ALL"
            }

            val allMessagingAppsNumbers = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.isSystem == 0
                } else {
                    notification.isSystem == 0 && notification.priority == 2
                }
            }.size

            val messagingNumbersSms = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
                        application
                    )
                } else {
                    notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
                        application
                    ) && notification.priority == 2
                }
            }.size
            val messagingNumbersWhatsapp = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.WHATSAPP_PACKAGE
                } else {
                    notification.platform == NotificationsGesture.WHATSAPP_PACKAGE && notification.priority == 2
                }
            }.size
            val messagingNumbersMessenger = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.FACEBOOK_PACKAGE
                } else {
                    notification.platform == NotificationsGesture.FACEBOOK_PACKAGE && notification.priority == 2
                }
            }.size
            val messagingNumbersMail = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.GMAIL_PACKAGE || notification.platform == NotificationsGesture.OUTLOOK_PACKAGE
                } else {
                    notification.platform == NotificationsGesture.GMAIL_PACKAGE || notification.platform == NotificationsGesture.OUTLOOK_PACKAGE && notification.priority == 2
                }
            }.size
            val messagingNumbersTelegram = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.TELEGRAM_PACKAGE
                } else {
                    notification.platform == NotificationsGesture.TELEGRAM_PACKAGE && notification.priority == 2
                }
            }.size
            val messagingNumbersSignal = list.filter { notification ->
                if (allOrVip == "ALL") {
                    notification.platform == NotificationsGesture.SIGNAL_PACKAGE
                } else {
                    notification.platform == NotificationsGesture.SIGNAL_PACKAGE && notification.priority == 2
                }
            }.size

            val allNotificationsAvoided = allMessagingNumbers.minus(allVipNumbers)

            val timeSaved = allNotificationsAvoided * 23

            if (messagingNumbersWhatsapp > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersWhatsapp, WHATSAPP_APP_NAME, Color.rgb(37, 211, 102)))
            }

            if (messagingNumbersSms > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersSms, MESSAGE_APP_NAME, Color.rgb(240, 150, 0)))
            }

            if (messagingNumbersMessenger > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersMessenger, MESSENGER_APP_NAME, Color.rgb(159, 48, 255)))
            }

            if (messagingNumbersMail > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersMail, "Mail", Color.rgb(200, 44, 40)))
            }

            if (messagingNumbersTelegram > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersTelegram, TELEGRAM_APP_NAME, Color.rgb(49, 168, 223)))
            }

            if (messagingNumbersSignal > 0) {
                notifications.add(PieChartDataViewState(messagingNumbersSignal, SIGNAL_APP_NAME, Color.rgb(52, 105, 209)))
            }

            emit(DashboardViewState("$allMessagingAppsNumbers messages", notifications))
        }.collect()
    }

    private fun compareIfNotificationDateIsToday(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val notificationDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt()
            val todayMonth = dateToday?.get(1)?.toInt()
            val todayDay = dateToday?.get(2)?.split(" ")?.get(0)
//            val todayHour = dateToday?.get(2)?.split(" ")?.get(1)?.split(":")?.get(0)
//            val todayMinutes = dateToday?.get(2)?.split(" ")?.get(1)?.split(":")?.get(1)

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]
//            val notificationHour = dateToday?.get(2)?.split(" ")?.get(1)?.split(":")?.get(0)
//            val notificationMinutes = dateToday?.get(2)?.split(" ")?.get(1)?.split(":")?.get(1)

            return if (notificationYear != todayYear || notificationMonth != todayMonth) {
                false
            } else {
                if (notificationDay == todayDay) {
                    true
                } else {
                    (todayDay?.toInt()?.minus(notificationDay.toInt())) == 1
                }
            }
        } catch (e: Exception) {
            Log.i("GetLocalDateTime", "Exception : ${e}")
        }
        return false
    }

    private fun compareIfNotificationDateIsFromThisWeek(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val notificationDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt() // 2023
            val todayMonth = dateToday?.get(1)?.toInt() // 01
            val todayDay = dateToday?.get(2)?.split(" ")?.get(0) // 22

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]

//            Log.i("GetLocalDateTime", "WEEKLY todayYear : ${todayYear}")
//            Log.i("GetLocalDateTime", "WEEKLY notificationYear : ${notificationYear}")
//
//            Log.i("GetLocalDateTime", "WEEKLY todayMonth : ${todayMonth}")
//            Log.i("GetLocalDateTime", "WEEKLY notificationMonth : ${notificationMonth}")
//
//            Log.i("GetLocalDateTime", "WEEKLY todayDay : ${todayDay}")
//            Log.i("GetLocalDateTime", "WEEKLY notificationDay : ${notificationDay}")

            return if (notificationYear != todayYear) {
                false
            } else {
                if (notificationDay == todayDay && notificationMonth == todayMonth) {
                    true
                } else {
                    if (todayMonth != null && todayDay != null && todayMonth.minus(notificationMonth) > 1) {
                        when (todayDay.toInt()) {
                            1 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 24
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 23
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 21
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            2 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 25
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 24
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 22
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            3 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 26
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 25
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 23
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            4 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 27
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 26
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 24
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            5 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 28
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 27
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 25
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            6 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 29
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 28
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 26
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }
                            7 -> {
                                when (notificationMonth) {
                                    // 31 days months
                                    3, 5, 7, 8, 10, 12 -> {
                                        notificationDay.toInt() > 30
                                    }

                                    // 30 days months
                                    4, 6, 9, 11 -> {
                                        notificationDay.toInt() > 29
                                    }

                                    // 28-29 days months
                                    2 -> {
                                        notificationDay.toInt() > 27
                                    }
                                    else -> {
                                        false
                                    }
                                }
                            }

                            else -> {
                                (todayDay.toInt().minus(notificationDay.toInt())) <= 7
                            }
                        }
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("GetLocalDateTime", "Exception : ${e}")
        }
        return false
    }

    private fun compareIfNotificationDateIsFromThisMonth(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val date = LocalDateTime.now().format(formatter)

            val notificationDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt()
            val todayMonth = dateToday?.get(1)?.toInt()

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()

            return if (notificationYear != todayYear) {
                false
            } else {
                if (notificationMonth == todayMonth) {
                    true
                } else {
                    todayMonth?.minus(notificationMonth) == 1
                }
            }
        } catch (e: Exception) {
            Log.i("GetLocalDateTime", "Exception : ${e}")
        }
        return false
    }

    fun updateSpinnerSelectedItem(value: Int) {
        viewModelScope.launch(coroutineDispatcherProvider.io) {
            spinnerSelectedItemFlow.emit(value)
        }
    }


    //region


//    val vipNumbersDaily = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                ) && notification.priority == 2
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp) && notification.platform == platform && notification.priority == 2
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp) && notification.priority == 2
//        }
//    }.size
//    val vipNumbersWeekly = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                ) && notification.priority == 2
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp) && notification.platform == platform && notification.priority == 2
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp) && notification.priority == 2
//        }
//    }.size
//    val vipNumbersMonthly = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                ) && notification.priority == 2
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp) && notification.platform == platform && notification.priority == 2
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp) && notification.priority == 2
//        }
//    }.size
//    val messagingNumbersDaily = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                )
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp) && notification.platform == platform
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsToday(notification.timestamp)
//        }
//    }.size
//    val messagingNumbersWeekly = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                )
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp) && notification.platform == platform
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsFromThisWeek(notification.timestamp)
//        }
//    }.size
//    val messagingNumbersMonthly = list.filter { notification ->
//        if (platform != "") {
//            if (platform == "Message") {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp) && notification.platform == NotificationsGesture.MESSAGE_PACKAGE || notification.platform == NotificationsGesture.XIAOMI_MESSAGE_PACKAGE || notification.platform == NotificationsGesture.MESSAGE_SAMSUNG_PACKAGE || notification.platform == NotificationsGesture.MESSAGES_PACKAGE || notification.platform == Telephony.Sms.getDefaultSmsPackage(
//                    application
//                )
//            } else {
//                notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp) && notification.platform == platform
//            }
//        } else {
//            notification.isSystem == 0 && compareIfNotificationDateIsFromThisMonth(notification.timestamp)
//        }
//    }.size

    //endregion
}