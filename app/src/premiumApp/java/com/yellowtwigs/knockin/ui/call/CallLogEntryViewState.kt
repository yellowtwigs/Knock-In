package com.yellowtwigs.knockin.ui.call

data class CallLogEntryViewState(
    val id: String,
    val callerName: String?,
    val phoneNumber: String,
    val callType: Int,
    val callDate: Long,
    val callDuration: Long
)
