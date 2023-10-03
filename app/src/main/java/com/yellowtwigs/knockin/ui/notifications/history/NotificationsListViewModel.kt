package com.yellowtwigs.knockin.ui.notifications.history

import android.app.Application
import android.graphics.drawable.Drawable
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

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
                        if (!notification.description.contains("Today with the way you set your contacts") || !notification.description.contains(
                                "grâce à la façon dont vous paramétrez vos contacts, vous recevez"
                            )
                        )
                            if (compareIfDateIsToday(notification.timestamp)) {
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
                        if (!notification.description.contains("Today with the way you set your contacts, you receive") || !notification.description.contains(
                                "grâce à la façon dont vous paramétrez vos contacts, vous recevez"
                            )
                        ) {
                            if (compareIfDateIsToday(notification.timestamp)) {
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
                        }
                    } else {
                        addNotificationInList(notificationsForSystem, notification)
                    }
                }
            }

            pinNotification?.let {
                addNotificationInList(notifications, it)
            }

            val fullList = sortedContactsList(sortedBy, filterBy, searchBarText, notifications, notificationsForSystem)
            val listDistinct = fullList.distinctBy {
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
            val duplicates = fullList.filterNot { it in listDistinct }
            emit(NotificationsHistoryViewState(list = listDistinct, duplicates = duplicates))
        }.collect()
    }

    private fun compareIfDateIsToday(timestamp: Long): Boolean {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        val currentDate = LocalDateTime.now()

        return date.toLocalDate() == currentDate.toLocalDate() || date.toLocalDate().plusDays(1) == currentDate.toLocalDate()
    }

    private fun sortedContactsList(
        sortedBy: Int?,
        filterBy: Int?,
        input: String?,
        notifications: List<NotificationsListViewState>,
        systemNotifications: List<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        val filteredNotifications = filterWithInput(filterBy, input, notifications, systemNotifications)
        return when (sortedBy) {
            R.id.sort_by_date -> filteredNotifications.sortedWith(compareByDescending<NotificationsListViewState> { it.id }.thenByDescending { it.systemPriority })
            R.id.sort_by_contact -> filteredNotifications.sortedWith(compareBy<NotificationsListViewState> {
                it.contactName.uppercase().unAccent()
            }.thenByDescending { it.systemPriority })

            R.id.notifications_sort_by_priority -> filteredNotifications.sortedWith(compareByDescending<NotificationsListViewState> { it.priority }
                .thenByDescending { it.systemPriority })

            else -> filteredNotifications.sortedWith(compareByDescending<NotificationsListViewState> { it.id }.thenByDescending { it.systemPriority })
        }
    }

    private fun filterWithInput(
        filterBy: Int?,
        input: String?,
        notifications: List<NotificationsListViewState>,
        systemNotifications: List<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        return if (input != null) {
            filterNotificationsList(filterBy, notifications.filter { notification ->
                val notificationText = notification.contactName + " " + notification.title + " " + notification.description
                notificationText.contains(input) || notificationText.uppercase().contains(input.uppercase()) || notificationText.lowercase()
                    .contains(input.lowercase())
            }, systemNotifications)
        } else {
            filterNotificationsList(filterBy, notifications, systemNotifications)
        }
    }

    private fun filterNotificationsList(
        filterBy: Int?, notifications: List<NotificationsListViewState>, systemNotifications: List<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        return when (filterBy) {
            R.id.filter_by_all -> systemNotifications
            R.id.filter_by_social_media -> notifications.filter { isSocialMedia(it.platform) }
            R.id.filter_by_msg_apps -> notifications.filter { isMessagingApp(it.platform, application) }
            R.id.sms_filter -> notifications.filter { convertPackageToString(it.platform, application) == MESSAGE_APP_NAME }
            R.id.mail_filter -> notifications.filter {
                convertPackageToString(
                    it.platform, application
                ) == "Gmail" || convertPackageToString(it.platform, application) == "Outlook"
            }

            R.id.whatsapp_filter -> notifications.filter {
                convertPackageToString(
                    it.platform, application
                ) == NotificationsGesture.WHATSAPP_APP_NAME
            }

            R.id.telegram_filter -> notifications.filter { convertPackageToString(it.platform, application) == "Telegram" }
            R.id.facebook_filter -> notifications.filter { convertPackageToString(it.platform, application) == "Messenger" }
            R.id.signal_filter -> notifications.filter { convertPackageToString(it.platform, application) == "Signal" }
            else -> notifications
        }
    }

    private fun addNotificationInList(notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB) {
        val systemPriority = if (notification.platform == KNOCKIN_PACKAGE) {
            2
        } else {
            1
        }

        val background: Drawable?
        val icon: Drawable?

        if (compareIfDateIsToday(notification.timestamp) && notification.isCancellable != 1 && isMessagingApp(
                notification.platform, application
            )
        ) {
            if (notification.priority == 2) {
                background = AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout_yellow)
                icon = AppCompatResources.getDrawable(application, R.drawable.ic_circular_vip_icon)
            } else {
                background = AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout_blue_turquoise)
                icon = AppCompatResources.getDrawable(application, R.drawable.ic_new_icon)
            }
        } else {
            background = AppCompatResources.getDrawable(application, R.drawable.rounded_form_layout)
            icon = null
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
                dateFormat.format(Date(notification.timestamp)),
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


    fun setSearchTextChanged(text: String) {
        searchBarTextFlow.tryEmit(text)
    }

    fun setSortedBy(sortedBy: Int) {
        sortedByFlow.tryEmit(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        filterByFlow.tryEmit(filterBy)
    }

    fun deleteNotification(notification: NotificationDB) = viewModelScope.launch {
        notificationsRepository.deleteNotification(notification)
    }

    fun deleteNotifications(notifications: List<NotificationDB>) = viewModelScope.launch {
        Log.i("DeleteNotification", "viewModel - notifications : $notifications")
        notificationsRepository.deleteNotifications(notifications)
    }

    fun deleteAllNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.deleteAllNotifications()
        }
    }

    fun deleteNotificationById(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.deleteNotificationById(id)
        }
    }

    fun deleteNotificationsByIds(ids: List<Int>) = viewModelScope.launch {
        notificationsRepository.deleteNotificationsByIds(ids)
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