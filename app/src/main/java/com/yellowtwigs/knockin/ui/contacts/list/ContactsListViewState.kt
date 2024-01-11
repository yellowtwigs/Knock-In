package com.yellowtwigs.knockin.ui.contacts.list

import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.EquatableCallback

data class ContactsListViewState(
    val id: Int,
    val fullName: String,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val firstPhoneNumber: PhoneNumberWithSpinner,
    val secondPhoneNumber: PhoneNumberWithSpinner,
    val listOfMails: List<String>,
    val priority: Int,
    val isFavorite: Boolean,
    val messengerId: String,
    val hasWhatsapp: Boolean,
    val hasTelegram: Boolean,
    val hasSignal: Boolean,
    val onClickedCallback: EquatableCallback,
)