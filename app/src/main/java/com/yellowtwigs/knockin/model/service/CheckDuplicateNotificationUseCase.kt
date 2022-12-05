package com.yellowtwigs.knockin.model.service

import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import javax.inject.Inject

class CheckDuplicateNotificationUseCase @Inject constructor(private val notificationsRepository: NotificationsRepository) {

    suspend operator fun invoke(notificationDB: NotificationDB): Boolean {
        notificationsRepository.getAllNotificationsList().map {
            return it.title == notificationDB.title &&
                    it.contactName == notificationDB.contactName &&
                    it.description == notificationDB.description &&
                    it.platform == notificationDB.platform &&
                    it.timestamp == notificationDB.timestamp
        }
        return false
    }
}