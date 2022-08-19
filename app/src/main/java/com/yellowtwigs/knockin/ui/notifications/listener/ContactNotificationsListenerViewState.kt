package com.yellowtwigs.knockin.ui.notifications.listener

import com.yellowtwigs.knockin.R

data class ContactNotificationsListenerViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val listOfPhoneNumbers: List<String>,
    val listOfMails: List<String>,
    var mail_name: String,
    val priority: Int,
    val isFavorite: Boolean,
    val messengerId: String,
    val hasWhatsapp: Boolean,
    val hasTelegram: Boolean,
    val hasSignal: Boolean,
    var notificationTone: String,
    var notificationSound: Int = R.raw.sms_ring,
    var isCustomSound: Int,
    var vipSchedule: Int,
    var hourLimitForNotification: String,
    var audioFileName: String
)