package com.yellowtwigs.knockin.ui.notifications.history

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(repository: NotificationsRepository) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<NotificationsListViewState>>()

    init {
        val allNotifications = repository.getAllNotifications()

        viewStateLiveData.addSource(allNotifications) { notifications ->
            combine(notifications)
        }
    }

    private fun combine(allNotifications: List<NotificationDB>) {
        val notifications = arrayListOf<NotificationsListViewState>()

        if (allNotifications.isNotEmpty()) {
            for (notification in allNotifications) {
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
                    )
                )
            }
        }

        viewStateLiveData.value = notifications
    }

    fun getAllNotifications(): LiveData<List<NotificationsListViewState>> {
        return viewStateLiveData
    }
}