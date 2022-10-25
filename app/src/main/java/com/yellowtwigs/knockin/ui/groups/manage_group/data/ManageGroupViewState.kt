package com.yellowtwigs.knockin.ui.groups.manage_group

data class ManageGroupViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val section_color: Int,
    val listOfContactsData: List<String>
)