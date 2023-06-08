package com.yellowtwigs.knockin.ui.notifications.history

data class NotificationsHistoryViewState(
    val title: String,
    val description: String,
    val platform: String,
    val timestamp: Long,
    val isVisible: Boolean,
    val list: List<NotificationsListViewState>
)

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
    val isSystem: Int
)