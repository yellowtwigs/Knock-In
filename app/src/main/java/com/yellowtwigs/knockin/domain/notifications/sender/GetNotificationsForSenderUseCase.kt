package com.yellowtwigs.knockin.domain.notifications.sender

import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import javax.inject.Inject

class GetNotificationsForSenderUseCase @Inject constructor(private val notificationsRepository: NotificationsRepository) {

    operator fun invoke() = notificationsRepository.getAllNotificationsList()
}