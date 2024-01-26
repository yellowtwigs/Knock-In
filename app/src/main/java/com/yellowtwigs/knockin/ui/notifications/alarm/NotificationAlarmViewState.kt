package com.yellowtwigs.knockin.ui.notifications.alarm


data class NotificationAlarmViewState(
    var title: String?,
    var content: String?,
    var platform: String?,
    var contactId: Int,
    val dateTime: String
)