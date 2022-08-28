package com.yellowtwigs.knockin.repositories.notifications

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.NotificationDB

interface NotificationsRepository {

    fun getAllNotifications(): LiveData<List<NotificationDB>>

    suspend fun insertNotification(notification: NotificationDB)

    suspend fun deleteNotification(notification: NotificationDB)
    suspend fun deleteNotificationsByPlatform(platform: String)
    suspend fun deleteAllNotifications()
}