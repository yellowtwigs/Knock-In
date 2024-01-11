package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import com.yellowtwigs.knockin.R

data class EditContactViewState(
    val id: Int,
    val androidId: Int?,
    val fullName: String,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val firstPhoneNumber: PhoneNumberWithFlag,
    val listOfMails: List<String>,
    var mail_name: String,
    val priority: Int,
    val isFavorite: Int,
    val messengerId: String,
    val listOfMessagingApps: List<String>,

    var notificationTone: String,
    var notificationSound: Int = R.raw.sms_ring,
    var isCustomSound: Int,
    var vipSchedule: Int,
    var hourLimitForNotification: String,
    var audioFileName: String
)