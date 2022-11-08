package com.yellowtwigs.knockin.model.database.data

import androidx.room.*
import com.yellowtwigs.knockin.R

@Entity(tableName = "contacts_table")
data class ContactDB(

    // Basic fields
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") var lastName: String,

    @ColumnInfo(name = "profile_picture") val profilePicture: Int,
    @ColumnInfo(name = "profile_picture_str") val profilePicture64: String,

    @ColumnInfo(name = "listOfPhoneNumbers") val listOfPhoneNumbers: List<String>,
    @ColumnInfo(name = "listOfMails") val listOfMails: List<String>,

    // Knock In Fields
    @ColumnInfo(name = "mail_name") var mail_name: String,
    @ColumnInfo(name = "priority") val priority: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int,
    @ColumnInfo(name = "messenger_id") val messengerId: String,
    @ColumnInfo(name = "listOfMessagingApps") val listOfMessagingApps: List<String>,

    @ColumnInfo(name = "notification_tone") var notificationTone: String,
    @ColumnInfo(name = "notification_Sound") var notificationSound: Int = R.raw.sms_ring,
    @ColumnInfo(name = "is_custom_sound") var isCustomSound: Int,
    @ColumnInfo(name = "vip_schedule") var vipSchedule: Int,
    @ColumnInfo(name = "hour_limit_for_notification") var hourLimitForNotification: String,
    @ColumnInfo(name = "audio_file_name") var audioFileName: String
)