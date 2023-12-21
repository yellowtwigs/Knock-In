package com.yellowtwigs.knockin.background.service

import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner

data class PopupNotificationParams(
    val contactName: String,
    val description: String,
    val platform: String,
    val date: Long,
    val listOfPhoneNumbersWithSpinner: List<PhoneNumberWithSpinner>,
    val mail: String,
)