package com.yellowtwigs.knockin.domain.notifications

import android.util.Log
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import javax.inject.Inject

class SaveNotification @Inject constructor(private val notificationsRepository: NotificationsRepository) {

    suspend operator fun invoke(notificationDB: NotificationDB) {
        var isNotDuplicate = true

        for (notification in notificationsRepository.getAllNotificationsList()) {

            notification.apply {
                if (title == notificationDB.title &&
                    contactName == notificationDB.contactName &&
                    description == notificationDB.description &&
                    platform == notificationDB.platform &&
                    timestamp == notificationDB.timestamp &&
                    isCancellable == notificationDB.isCancellable &&
                    idContact == notificationDB.idContact &&
                    priority == notificationDB.priority
                ) {
                    Log.i("notification_platform", "notificationDB : $notificationDB")
                    Log.i("notification_platform", "notification : $notification")

                    isNotDuplicate = false
                }
            }
        }

        if (isNotDuplicate) {
            notificationsRepository.insertNotification(notificationDB)
        }
    }
}