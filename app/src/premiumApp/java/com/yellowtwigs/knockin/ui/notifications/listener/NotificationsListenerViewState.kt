package com.yellowtwigs.knockin.ui.notifications.listener


data class NotificationsListenerViewState(
    val id: Int = 0,
    val title: String,
    val contactName: String,
    val description: String,
    val platform: String,
    val timestamp: Long,
    val isCancellable: Int,
    val idContact: Int,
    val priority: Int
) {
}