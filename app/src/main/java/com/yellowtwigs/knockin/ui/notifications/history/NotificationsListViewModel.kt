package com.yellowtwigs.knockin.ui.notifications.history

import android.content.Context
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString
import com.yellowtwigs.knockin.utils.NotificationsGesture.isMessagingApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsListViewModel @Inject constructor(
    private val repository: NotificationsRepository, @ApplicationContext val context: Context
) : ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<NotificationsListViewState>>()

    private val searchBarTextLiveData = MutableLiveData<String>()
    private val sortedByLiveData = MutableLiveData<Int>()
    private val filterByLiveData = MutableLiveData<Int>()

    init {
        val allNotifications = repository.getAllNotifications()

        viewStateLiveData.addSource(allNotifications) { notifications ->
            combine(
                notifications, searchBarTextLiveData.value, sortedByLiveData.value, filterByLiveData.value
            )
        }

        viewStateLiveData.addSource(searchBarTextLiveData) { input ->
            combine(allNotifications.value, input, sortedByLiveData.value, filterByLiveData.value)
        }

        viewStateLiveData.addSource(sortedByLiveData) {
            combine(allNotifications.value, searchBarTextLiveData.value, it, filterByLiveData.value)
        }

        viewStateLiveData.addSource(filterByLiveData) {
            combine(allNotifications.value, searchBarTextLiveData.value, sortedByLiveData.value, it)
        }
    }

    private fun combine(
        allNotifications: List<NotificationDB>?, input: String?, sortedBy: Int?, filterBy: Int?

    ) {
        val notifications = arrayListOf<NotificationsListViewState>()

        if (allNotifications?.isNotEmpty() == true) {
            for (notification in allNotifications) {
                addNotificationInList(notifications, notification)
            }
        }

        viewStateLiveData.value = sortedContactsList(sortedBy, filterBy, input, notifications)
    }

    private fun sortedContactsList(
        sortedBy: Int?, filterBy: Int?, input: String?, notifications: ArrayList<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        if (sortedBy != null) {
            when (sortedBy) {
                R.id.sort_by_date -> {
                    return filterWithInput(filterBy, input, notifications).sortedByDescending {
                        it.id
                    }
                }
                R.id.sort_by_contact -> {
                    return filterWithInput(filterBy, input, notifications).sortedBy {
                        it.contactName.uppercase().unAccent()
                    }
                }
                R.id.notifications_sort_by_priority -> {
                    return filterWithInput(filterBy, input, notifications).sortedBy {
                        it.id
                    }.sortedByDescending { it.priority }
                }
                else -> {
                    return filterWithInput(filterBy, input, notifications).sortedByDescending {
                        it.id
                    }
                }
            }
        } else {
            return filterWithInput(filterBy, input, notifications).sortedByDescending {
                it.id
            }
        }
    }

    private fun filterWithInput(
        filterBy: Int?, input: String?, notifications: ArrayList<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        return if (input != null) {
            filterNotificationsList(filterBy, notifications.filter { notification ->
                val notificationText = notification.contactName + " " + notification.title + " " + notification.description
                notificationText.contains(input) || notificationText.uppercase().contains(input.uppercase()) || notificationText.lowercase()
                    .contains(input.lowercase())
            })
        } else {
            filterNotificationsList(filterBy, notifications)
        }
    }

    private fun filterNotificationsList(
        filterBy: Int?, notifications: List<NotificationsListViewState>
    ): List<NotificationsListViewState> {
        if (filterBy != null) {
            when (filterBy) {
                R.id.filter_by_all -> {
                    return notifications
                }
                R.id.filter_by_msg_apps -> {
                    return notifications.filter { isMessagingApp(it.platform, context) }
                }
                R.id.sms_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Message"
                    }
                }
                R.id.mail_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Gmail" || convertPackageToString(it.platform, context) == "Outlook"
                    }
                }
                R.id.whatsapp_filter -> {
                    return notifications.filter {
                        convertPackageToString(it.platform, context) == "WhatsApp"
                    }
                }
                R.id.facebook_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Messenger" || convertPackageToString(
                            it.platform, context
                        ) == "Facebook"
                    }
                }
                R.id.signal_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Signal"
                    }
                }
                R.id.discord_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Discord"
                    }
                }
                R.id.viber_filter -> {
                    return notifications.filter {
                        convertPackageToString(
                            it.platform, context
                        ) == "Viber"
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

    private fun addNotificationInList(
        notifications: ArrayList<NotificationsListViewState>, notification: NotificationDB
    ) {
        notifications.add(
            NotificationsListViewState(
                notification.id,
                notification.title,
                notification.contactName,
                notification.description,
                notification.platform,
                notification.timestamp,
                notification.idContact,
                notification.priority,
                notification.phoneNumber,
                notification.mail,
                notification.isSystem
            )
        )
    }

    fun getAllNotifications(): LiveData<List<NotificationsListViewState>> {
        return viewStateLiveData
    }

    fun deleteNotification(notification: NotificationDB) = viewModelScope.launch {
        repository.deleteNotification(notification)
    }

    fun setSearchTextChanged(text: String) {
        searchBarTextLiveData.value = text
    }

    fun setSortedBy(sortedBy: Int) {
        sortedByLiveData.value = sortedBy
    }

    fun setFilterBy(filterBy: Int) {
        filterByLiveData.value = filterBy
    }

    fun deleteAllNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteAllNotifications()
        }
    }

    fun deleteAllSystemNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteAllSystemNotifications()
        }
    }
}