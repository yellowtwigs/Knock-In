package com.yellowtwigs.knockin.model.service

data class PopupNotificationViewState(
    val id: Int = 0,
    val title: String,
    val contactName: String,
    val description: String,
    val platform: String,
    val phoneNumber: String,
    val messengerId: String,
    val email: String
)