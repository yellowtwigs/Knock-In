package com.yellowtwigs.knockin.domain.notifications

import com.yellowtwigs.knockin.model.service.CheckDuplicateNotificationUseCase

data class NotificationsListenerUseCases(
    val getContactByName: GetContactByName,
    val getContactByMail: GetContactByMail,
    val getContactByPhoneNumber: GetContactByPhoneNumber,
    val saveNotification: SaveNotification,
    val checkDuplicateNotificationUseCase: CheckDuplicateNotificationUseCase,
)