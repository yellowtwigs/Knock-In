package com.yellowtwigs.knockin.ui.notifications.history

import android.app.Application
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import com.yellowtwigs.knockin.utils.NotificationsGesture
import com.yellowtwigs.knockin.utils.NotificationsGesture.KNOCKIN_PACKAGE
import com.yellowtwigs.knockin.utils.NotificationsGesture.MESSAGE_APP_NAME
import com.yellowtwigs.knockin.utils.NotificationsGesture.WHATSAPP_PACKAGE
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString
import com.yellowtwigs.knockin.utils.NotificationsGesture.isMessagingApp
import com.yellowtwigs.knockin.utils.NotificationsGesture.isSocialMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class NotificationsListViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : ViewModel() {

    private val searchBarTextFlow = MutableStateFlow("")
    private val sortedByFlow = MutableStateFlow(R.id.sort_by_date)
    private val filterByFlow = MutableStateFlow(R.id.filter_by_msg_apps)

    val notificationsListViewStateLiveData: LiveData<NotificationsHistoryViewState> = liveData(coroutineDispatcherProvider.io) {
        combine(
            notificationsRepository.getMessagingNotifications(),
            notificationsRepository.getSystemNotifications(),
            searchBarTextFlow,
            sortedByFlow,
            filterByFlow
        ) { messagingNotifications, systemNotifications, searchBarText, sortedBy, filterBy ->
            val notifications = arrayListOf<NotificationsListViewState>()
            val notificationsForSystem = arrayListOf<NotificationsListViewState>()

            var pinNotification: NotificationDB? = null
            var cpt = 0

            if (messagingNotifications.isNotEmpty()) {
                messagingNotifications.map { notification ->
                    if (notification.platform == KNOCKIN_PACKAGE) {
                        if (compareIfNotificationDateIsToday(notification.timestamp)) {
                            if (cpt > 1) {
                                if (notification.timestamp > pinNotification!!.timestamp) {
                                    pinNotification = notification
                                    cpt += 1
                                }
                            } else {
                                pinNotification = notification
                                cpt += 1
                            }
                        }
                    } else {
                        addNotificationInList(notifications, notification)
                    }
                }
            }

            if (systemNotifications.isNotEmpty()) {
                systemNotifications.map { notification ->
                    if (notification.platform == KNOCKIN_PACKAGE) {
                        if (compareIfNotificationDateIsToday(notification.timestamp)) {
                            if (cpt > 1) {
                                if (notification.timestamp > pinNotification!!.timestamp) {
                                    pinNotification = notification
                                    cpt += 1
                                }
                            } else {
                                pinNotification = notification
                                cpt += 1
                            }
                        }
                    } else {
                        addNotificationInList(notificationsForSystem, notification)
                    }
                }
            }

            pinNotification?.let {
                addNotificationInList(notifications, it)
            }

            emit(
                NotificationsHistoryViewState(list = sortedContactsList(
                    sortedBy, filterBy, searchBarText, notifications, notificationsForSystem
                ).distinctBy {
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
                })
            )
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

            val notificationYear = notificationDateToday[0].toInt()
            val notificationMonth = notificationDateToday[1].toInt()
            val notificationDay = notificationDateToday[2].split(" ")[0]

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

    private fun sortedContactsList(
        sortedBy: Int?,
        filterBy: Int?,
        input: String?,
        notifications: ArrayList<NotificationsListViewState>,
        notificationsForSystem: ArrayList<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        if (sortedBy != null) {
            when (sortedBy) {
                R.id.sort_by_date -> {
                    return filterWithInput(filterBy, input, notifications, notificationsForSystem).sortedByDescending {
                        it.id
                    }.sortedByDescending {
                        it.systemPriority
                    }
                }
                R.id.sort_by_contact -> {
                    return filterWithInput(filterBy, input, notifications, notificationsForSystem).sortedBy {
                        it.contactName.uppercase().unAccent()
                    }.sortedByDescending {
                        it.systemPriority
                    }
                }
                R.id.notifications_sort_by_priority -> {
                    return filterWithInput(filterBy, input, notifications, notificationsForSystem).sortedBy {
                        it.id
                    }.sortedByDescending { it.priority }.sortedByDescending {
                        it.systemPriority
                    }
                }
                else -> {
                    return filterWithInput(filterBy, input, notifications, notificationsForSystem).sortedByDescending {
                        it.id
                    }.sortedByDescending {
                        it.systemPriority
                    }
                }
            }
        } else {
            return filterWithInput(filterBy, input, notifications, notificationsForSystem).sortedByDescending {
                it.id
            }.sortedByDescending {
                it.systemPriority
            }
        }
    }

    private fun filterWithInput(
        filterBy: Int?,
        input: String?,
        notifications: ArrayList<NotificationsListViewState>,
        notificationsForSystem: ArrayList<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        return if (input != null) {
            filterNotificationsList(filterBy, notifications.filter { notification ->
                val notificationText = notification.contactName + " " + notification.title + " " + notification.description
                notificationText.contains(input) || notificationText.uppercase().contains(input.uppercase()) || notificationText.lowercase()
                    .contains(input.lowercase())
            }, notificationsForSystem.filter { notification ->
                val notificationText = notification.contactName + " " + notification.title + " " + notification.description
                notificationText.contains(input) || notificationText.uppercase().contains(input.uppercase()) || notificationText.lowercase()
                    .contains(input.lowercase())
            })
        } else {
            filterNotificationsList(filterBy, notifications, notificationsForSystem)
        }
    }

    private fun filterNotificationsList(
        filterBy: Int?, notifications: List<NotificationsListViewState>, notificationsForSystem: List<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        if (filterBy != null) {
            when (filterBy) {
                R.id.filter_by_all -> {
                    return notificationsForSystem.filter {
                        convertPackageToString(it.platform, application) != NotificationsGesture.LINKEDIN_NAME
                    }
                }
                R.id.filter_by_social_media -> {
                    val superList = notifications + notificationsForSystem
                    return superList.filter { isSocialMedia(it.platform) }
                }
                R.id.filter_by_msg_apps -> {
                    return notifications.filter { isMessagingApp(it.platform, application) }
                }
                R.id.sms_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, application
                        ) == MESSAGE_APP_NAME
                    }
                }
                R.id.mail_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, application
                        ) == "Gmail" || convertPackageToString(it.platform, application) == "Outlook"
                    }
                }
                R.id.whatsapp_filter -> {
                    return notifications.filter {
                        convertPackageToString(it.platform, application) == NotificationsGesture.WHATSAPP_APP_NAME
                    }
                }
                R.id.telegram_filter -> {
                    return notifications.filter {
                        convertPackageToString(it.platform, application) == "Telegram"
                    }
                }
                R.id.facebook_filter -> {
                    return notifications.filter {
                        convertPackageToString(it.platform, application) == "Messenger"
                    }
                }
                R.id.signal_filter -> {
                    return notifications.filter {
                        convertPackageToString(it.platform, application) == "Signal"
                    }
                }
                else -> {
                    return notifications
                }
            }
        } else {
            return notifications
        }
    }

    private fun addNotificationInList(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        val background =
            if (compareIfNotificationDateIsToday(notification.timestamp) && notification.isCancellable != 1 && isMessagingApp(
                    notification.platform,
                    application
                )
            ) {
                if (notification.priority == 2) {
                    AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout_yellow)
                } else {
                    AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout_blue_turquoise)
                }
            } else {
                AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout)
            }

        val icon =
            if (compareIfNotificationDateIsToday(notification.timestamp) && notification.isCancellable != 1 && isMessagingApp(
                    notification.platform,
                    application
                )
            ) {
                if (notification.priority == 2) {
                    AppCompatResources.getDrawable(application, R.drawable.ic_circular_vip_icon)
                } else {
                    AppCompatResources.getDrawable(application, R.drawable.ic_new_icon)
                }
            } else {
                null
            }

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
                background,
                icon
            )
        )
    }

    fun deleteNotification(notification: NotificationDB) = viewModelScope.launch {
        notificationsRepository.deleteNotification(notification)
    }

    fun setSearchTextChanged(text: String) {
        searchBarTextFlow.tryEmit(text)
    }

    fun setSortedBy(sortedBy: Int) {
        sortedByFlow.tryEmit(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        filterByFlow.tryEmit(filterBy)
    }

    fun deleteAllNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.deleteAllNotifications()
        }
    }

    fun deleteNotificationById(id : Int) {
        Log.i("GetPosition", "id - 1 : $id")
        CoroutineScope(Dispatchers.Default).launch {
            Log.i("GetPosition", "id - 2 : $id")
            notificationsRepository.deleteNotificationById(id)
        }
    }

    fun updateNotification(notification: NotificationDB) {
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.updateNotification(notification)
        }
    }

    fun deleteAllSystemNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.deleteAllSystemNotifications()
        }
    }
}