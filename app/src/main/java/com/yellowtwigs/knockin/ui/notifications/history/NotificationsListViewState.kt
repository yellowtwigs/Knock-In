package com.yellowtwigs.knockin.ui.notifications.history

data class NotificationsListViewState(
    val id: Int,
    val title: String,
    val contactName: String,
    val description: String,
    val platform: String,
    val timestamp: Long,
    val idContact: Int,
    val priority: Int,
    val phoneNumber: String,
    val mail: String,
)