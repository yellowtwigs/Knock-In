package com.yellowtwigs.knockin.ui.groups.manage_group.data

data class ContactManageGroupViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String,
    val priority: Int
) {
}