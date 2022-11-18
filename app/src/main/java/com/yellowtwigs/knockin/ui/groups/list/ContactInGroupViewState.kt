package com.yellowtwigs.knockin.ui.groups.list

data class ContactInGroupViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val listOfPhoneNumbers: List<String>,
    val listOfMails: List<String>,
    val priority: Int,
    val hasWhatsapp: Boolean,
    val hasTelegram: Boolean,
    val hasSignal: Boolean,
    val messengerId: String
) {
}