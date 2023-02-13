package com.yellowtwigs.knockin.model.service

data class PopupNotificationViewState(
    val id: Int,
    val title: String,
    val description: String,
    val platform: String,
    val contactName: String,
    val phoneNumber: String,
    val messengerId: String,
    val email: String
)