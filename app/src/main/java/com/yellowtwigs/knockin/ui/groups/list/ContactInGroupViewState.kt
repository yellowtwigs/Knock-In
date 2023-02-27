package com.yellowtwigs.knockin.ui.groups.list

import com.yellowtwigs.knockin.R

data class ContactInGroupViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    var profilePicture: Int,
    val profilePicture64: String,
    val listOfPhoneNumbers: List<String>,
    val listOfMails: List<String>,
    val priority: Int,
    val hasWhatsapp: Boolean,
    val hasTelegram: Boolean,
    val hasSignal: Boolean,
    val messengerId: String,
    val groupName: String,
    val pictureMultiSelect: Int = 0,
    var profilePictureSelected: Int = R.drawable.ic_item_selected,
)