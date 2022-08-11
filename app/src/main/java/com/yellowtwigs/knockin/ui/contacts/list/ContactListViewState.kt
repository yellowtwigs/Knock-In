package com.yellowtwigs.knockin.ui.contacts.list


data class ContactListViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val listOfPhoneNumbers: List<String>,
    val listOfMails: List<String>,
    val contactPriority: Int,
    val isFavorite: Boolean,
    val messengerId: String,
    val hasWhatsapp: Boolean,
    val hasTelegram: Boolean,
    val hasSignal: Boolean
)