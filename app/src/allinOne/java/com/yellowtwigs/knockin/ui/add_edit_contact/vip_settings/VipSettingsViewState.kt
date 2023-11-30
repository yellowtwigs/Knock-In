package com.yellowtwigs.knockin.ui.add_edit_contact.vip_settings

data class VipSettingsViewState(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val listOfPhoneNumbers: List<String>,
    val listOfMails: List<String>,
    var mail_name: String,
    val priority: Int,
    val isFavorite: Int,
    val messengerId: String,
    val listOfMessagingApps: List<String>,

    val notificationSound: Int,
    val notificationTone: String,
    val isCustomSound: Boolean,
    val vipSchedule: Int,
    var hourLimitForNotification: String,
    var audioFileName: String
)
