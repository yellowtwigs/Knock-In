package com.yellowtwigs.knockin.model.service

import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner

data class PopupNotificationParams(
    val contactName: String,
    val description: String,
    val platform: String,
    val date: String,
    val listOfPhoneNumbersWithSpinner: List<PhoneNumberWithSpinner>,
    val mail: String,
)
