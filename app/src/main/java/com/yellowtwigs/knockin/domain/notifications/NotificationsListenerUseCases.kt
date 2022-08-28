package com.yellowtwigs.knockin.domain.notifications

data class NotificationsListenerUseCases(
    val getContactByName: GetContactByName,
    val getContactByMail: GetContactByMail,
    val getContactByPhoneNumber: GetContactByPhoneNumber,
    val saveNotification: SaveNotification
)