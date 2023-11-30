package com.yellowtwigs.knockin.ui.teleworking

data class TeleworkingViewState(
    val id: Int,
    val firstName: String,
    var lastName: String,
    val profilePicture: Int,
    val profilePicture64: String) {
}