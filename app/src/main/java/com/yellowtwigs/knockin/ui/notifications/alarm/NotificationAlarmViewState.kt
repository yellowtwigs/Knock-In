package com.yellowtwigs.knockin.ui.notifications.alarm

import android.os.Parcel
import android.os.Parcelable

data class NotificationAlarmViewState(
    var id: Int = 0,
    var content: String? = "",
    var platform: String? = "",
    var sender: String? = "",
    val contactId: Int
)