package com.yellowtwigs.knockin.domain.notifications

import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import javax.inject.Inject

class SaveNotification @Inject constructor(private val notificationsRepository: NotificationsRepository) {

    suspend operator fun invoke(notificationDB: NotificationDB): Boolean {
        notificationsRepository.insertNotification(notificationDB)
        return true
    }
}