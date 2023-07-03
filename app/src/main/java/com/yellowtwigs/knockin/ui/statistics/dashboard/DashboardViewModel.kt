package com.yellowtwigs.knockin.ui.statistics.dashboard

import android.app.Application
import android.graphics.Color
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.get_number.GetNumberOfContactsUseCase
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.history.NotificationParams
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsListViewState
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import com.yellowtwigs.knockin.utils.NotificationsGesture
import com.yellowtwigs.knockin.utils.NotificationsGesture.FACEBOOK_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.MESSAGE_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.MESSENGER_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.SIGNAL_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.TELEGRAM_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.WHATSAPP_APP_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
    private val getNumberOfContactsUseCase: GetNumberOfContactsUseCase,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : ViewModel() {

    val vipOrNewFlow = MutableStateFlow(false)
    val dailyWeeklyMonthlyFlow = MutableStateFlow(R.id.item_daily)

    val dashboardViewStateLiveData: LiveData<DashboardViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            notificationsRepository.getAllNotifications().asFlow(),
            getNumberOfContactsUseCase.invoke(),
            vipOrNewFlow,
            dailyWeeklyMonthlyFlow
        ) { list, numberOfContacts, isVIP, dailyWeeklyMonthly ->

            val notifications = arrayListOf<NotificationsListViewState>()
            list.filter {
                NotificationsGesture.isMessagingApp(it.platform, application)
            }.map { notification ->
                when (dailyWeeklyMonthly) {
                    R.id.item_daily -> addNotificationInListDaily(notifications, notification)
                    R.id.item_weekly -> addNotificationInListWeekly(notifications, notification)
                    R.id.item_monthly -> addNotificationInListMonthly(notifications, notification)
                    else -> addNotificationInListDaily(notifications, notification)
                }
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

            val numberOfContactsVIP = numberOfContacts.numberOfVips
            val numberOfContactsStandard = numberOfContacts.numberOfStandard
            val numberOfContactsSilent = numberOfContacts.numberOfSilent

            val nonVipNotificationsNumbers = allMessagingNumbers.minus(allVipNumbers)
            val numberOfContactsTotal = numberOfContactsStandard.plus(numberOfContactsVIP).plus(numberOfContactsSilent)
            val isAllOtherContactsSilent = numberOfContactsTotal.minus(numberOfContactsVIP) == numberOfContactsSilent

            val icon = if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                R.drawable.ic_speedometer_strong_red
            } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                R.drawable.ic_speedometer_orange
            } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                R.drawable.ic_speedometer_yellow
            } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                R.drawable.ic_speedometer_light_green
            } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                R.drawable.ic_speedometer_strong_green
            } else {
                R.drawable.ic_speedometer_yellow
            }

            val adviceMessage = if (numberOfContactsVIP == 0 && numberOfContactsSilent == 0) {
                application.getString(R.string.strong_red_advice)
            } else if (numberOfContactsVIP < 5 && numberOfContactsSilent == 0) {
                application.getString(R.string.orange_advice)
            } else if (numberOfContactsVIP < 5 && nonVipNotificationsNumbers >= 5) {
                application.getString(R.string.yellow_advice)
            } else if (numberOfContactsVIP == 5 && nonVipNotificationsNumbers >= 5) {
                application.getString(R.string.light_green_advice)
            } else if (numberOfContactsVIP > 1 && isAllOtherContactsSilent) {
                application.getString(R.string.strong_green_advice)
            } else {
                application.getString(R.string.yellow_advice)
            }

            Log.i("GetAdviceMessage", "DashboardViewModel - adviceMessage : $adviceMessage")

            val listOfPieChartData = arrayListOf<PieChartDataViewState>()

            val messagingNumbersSms = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == Telephony.Sms.getDefaultSmsPackage(application)
                } else {
                    notification.priority == 2 && notification.platform == Telephony.Sms.getDefaultSmsPackage(application)
                }
            }.size

            val messagingNumbersWhatsapp = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == NotificationsGesture.WHATSAPP_PACKAGE
                } else {
                    notification.priority == 2 && notification.platform == NotificationsGesture.WHATSAPP_PACKAGE
                }
            }.size
            val messagingNumbersMessenger = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == NotificationsGesture.FACEBOOK_PACKAGE || notification.platform == NotificationsGesture.MESSENGER_PACKAGE
                } else {
                    notification.priority == 2 && notification.platform == NotificationsGesture.FACEBOOK_PACKAGE || notification.platform == NotificationsGesture.MESSENGER_PACKAGE
                }
            }.size

            val messagingNumbersMail = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == NotificationsGesture.GMAIL_PACKAGE || notification.platform == NotificationsGesture.OUTLOOK_PACKAGE
                } else {
                    notification.priority == 2 && notification.platform == NotificationsGesture.GMAIL_PACKAGE || notification.platform == NotificationsGesture.OUTLOOK_PACKAGE
                }
            }.size
            val messagingNumbersTelegram = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == NotificationsGesture.TELEGRAM_PACKAGE
                } else {
                    notification.priority == 2 && notification.platform == NotificationsGesture.TELEGRAM_PACKAGE
                }
            }.size
            val messagingNumbersSignal = notificationsAfterDistinct.filter { notification ->
                if (!isVIP) {
                    notification.priority != 2 && notification.platform == NotificationsGesture.SIGNAL_PACKAGE
                } else {
                    notification.priority == 2 && notification.platform == NotificationsGesture.SIGNAL_PACKAGE
                }
            }.size

            if (messagingNumbersWhatsapp > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersWhatsapp, WHATSAPP_APP_NAME, Color.rgb(37, 211, 102)))
            }

            if (messagingNumbersSms > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersSms, MESSAGE_APP_NAME, Color.rgb(240, 150, 0)))
            }

            if (messagingNumbersMessenger > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersMessenger, FACEBOOK_APP_NAME, Color.rgb(159, 48, 255)))
            }

            if (messagingNumbersMail > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersMail, "Mail", Color.rgb(200, 44, 40)))
            }

            if (messagingNumbersTelegram > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersTelegram, TELEGRAM_APP_NAME, Color.rgb(49, 168, 223)))
            }

            if (messagingNumbersSignal > 0) {
                listOfPieChartData.add(PieChartDataViewState(messagingNumbersSignal, SIGNAL_APP_NAME, Color.rgb(52, 105, 209)))
            }

            val allMessagingAppsNumbers =
                messagingNumbersWhatsapp + messagingNumbersSms + messagingNumbersMessenger + messagingNumbersMail + messagingNumbersTelegram + messagingNumbersSignal

            emit(
                DashboardViewState(
                    icon = icon,
                    adviceMessage = adviceMessage,
                    notificationsTitle = application.getString(R.string.messages_with_number, allMessagingAppsNumbers),
                    numberOfNotificationsUnprocessed = "$nonVipNotificationsNumbers",
                    numberOfNotificationsVip = "$allVipNumbers",
                    listOfPieChartData
                )
            )
        }.collect()
    }

    private fun addNotificationInListDaily(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(application) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
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
                        AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout),
                        null
                    )
                )
            }
        }
    }

    private fun addNotificationInListWeekly(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(application) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
        } else {
            if (compareIfNotificationDateIsFromThisWeek(notification.timestamp)) {
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
                        AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout),
                        null
                    )
                )
            }
        }
    }

    private fun addNotificationInListMonthly(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == NotificationsGesture.KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        if (notification.platform != Telephony.Sms.getDefaultSmsPackage(application) && notification.platform != NotificationsGesture.WHATSAPP_PACKAGE && notification.platform != NotificationsGesture.FACEBOOK_PACKAGE && notification.platform != NotificationsGesture.MESSENGER_PACKAGE && notification.platform != NotificationsGesture.GMAIL_PACKAGE && notification.platform != NotificationsGesture.OUTLOOK_PACKAGE && notification.platform != NotificationsGesture.SIGNAL_PACKAGE && notification.platform != NotificationsGesture.TELEGRAM_PACKAGE) {
        } else {
            if (compareIfNotificationDateIsFromThisMonth(notification.timestamp)) {
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
                        AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout),
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

            val notificationDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))

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

    private fun compareIfNotificationDateIsFromThisWeek(timestamp: Long): Boolean {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDateTime.now().format(formatter)

            val notificationDate = SimpleDateFormat("yyyy-MM-dd").format(Date(timestamp))

            val dateToday = date?.split("-")
            val notificationDateToday = notificationDate.split("-")

            val todayYear = dateToday?.get(0)?.toInt() // 2023
            val todayMonth = dateToday?.get(1)?.toInt() // 01
            val todayDay = dateToday?.get(2)?.split(" ")?.get(0) // 22

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]

            return if (notificationYear != todayYear) {
                false
            } else if (notificationMonth != todayMonth) {
                false
            } else {
                if (todayDay != null) {
                    if (notificationDay == todayDay) {
                        true
                    } else if (todayDay > notificationDay) {
                        todayDay.toInt().minus(notificationDay.toInt()) <= 7
                    } else {
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
                    }
                } else {
                    false
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

    fun changeNewOrVipNotifications(value: Boolean) {
        viewModelScope.launch {
            vipOrNewFlow.tryEmit(value)
        }
    }

    fun changeDailyWeeklyMonthly(value: Int) {
        CoroutineScope(coroutineDispatcherProvider.io).launch {
            dailyWeeklyMonthlyFlow.tryEmit(value)
        }
    }
}