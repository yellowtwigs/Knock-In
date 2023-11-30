package com.yellowtwigs.knockin.ui.notifications

import android.os.Parcel
import android.os.Parcelable

data class NotificationAlarmViewState(
    var id: Int = 0,
    var content: String? = "",
    var platform: String? = "",
    var sender: String? = "",
    val contactId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(content)
        parcel.writeString(platform)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationAlarmViewState> {
        override fun createFromParcel(parcel: Parcel): NotificationAlarmViewState {
            return NotificationAlarmViewState(parcel)
        }

        override fun newArray(size: Int): Array<NotificationAlarmViewState?> {
            return arrayOfNulls(size)
        }
    }
}