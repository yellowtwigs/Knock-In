package com.yellowtwigs.knockin.ui.groups.manage_group.data

data class ManageGroupViewState(
    val id: Int,
    val groupName: String,
    val section_color: Int,
    val listOfContacts: List<ContactManageGroupViewState>,
    val listOfIds: List<String>
)