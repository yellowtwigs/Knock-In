package com.yellowtwigs.knockin.repositories.notifications

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
interface NotificationsRepository {

    fun getAllNotifications(): LiveData<List<NotificationDB>>
    fun getAllNotificationsList(): List<NotificationDB>

    fun getMessagingNotifications(): Flow<List<NotificationDB>>
    fun getSystemNotifications(): Flow<List<NotificationDB>>

    suspend fun insertNotification(notification: NotificationDB)
    suspend fun updateNotification(notification: NotificationDB)

    suspend fun deleteNotification(notification: NotificationDB)
    suspend fun deleteNotificationById(id: Int)
    suspend fun deleteAllSystemNotifications()
    suspend fun deleteNotificationsByPlatform(platform: String)
    suspend fun deleteAllNotifications()
}