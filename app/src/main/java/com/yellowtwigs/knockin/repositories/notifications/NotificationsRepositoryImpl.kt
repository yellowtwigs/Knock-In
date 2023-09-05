package com.yellowtwigs.knockin.repositories.notifications

import android.util.Log
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.NotificationsDao
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(private val dao: NotificationsDao) : NotificationsRepository {

    override fun getAllNotifications() = dao.getAllNotifications().asLiveData()
    override fun getAllNotificationsList() = dao.getAllNotificationsList()

    override fun getMessagingNotifications() = dao.getMessagingNotifications()
    override fun getSystemNotifications() = dao.getSystemNotifications()

    override suspend fun insertNotification(notification: NotificationDB) {
        dao.insertNotification(notification)
    }

    override suspend fun updateNotification(notification: NotificationDB) {
        dao.updateNotification(notification)
    }

    override suspend fun deleteNotification(notification: NotificationDB) {
        dao.deleteNotification(notification)
    }

    override suspend fun deleteNotifications(notifications: List<NotificationDB>) {
        dao.deleteNotifications(notifications)
    }

    override suspend fun deleteNotificationsByPlatform(platform: String) {
        dao.deleteNotificationsByPlatform(platform)
    }

    override suspend fun deleteNotificationsByIds(ids: List<Int>) {
        Log.i("DeleteNotification", "repository - ids : $ids")
        ids.map {
            dao.deleteNotificationById(it)
        }
    }

    override suspend fun deleteNotificationById(id: Int) {
        dao.deleteNotificationById(id)
    }

    override suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }

    override suspend fun deleteAllSystemNotifications() {
        dao.deleteAllSystemNotifications()
    }
}