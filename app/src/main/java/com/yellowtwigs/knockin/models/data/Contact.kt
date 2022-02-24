package com.yellowtwigs.knockin.models.data

import androidx.room.*

/**
 * Data class qui représente un contact
 * @author Ryan Granet
 */
@Entity(tableName = "contact")
data class Contact(
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") var lastName: String,
    @ColumnInfo(name = "mail") var mail: String,
    @ColumnInfo(name = "mail_id") var mailId: String,
    @ColumnInfo(name = "phone_number") var phoneNumber: String,
    @ColumnInfo(name = "fix_number") var fixNumber: String,
    @ColumnInfo(name = "profile_picture") val profilePicture: Int,
    @ColumnInfo(name = "profile_picture_str") val profilePicture64: String,
    @ColumnInfo(name = "priority") val priority: Int,
    @ColumnInfo(name = "is_favorite") val favorite: Int,
    @ColumnInfo(name = "messenger_id") val messengerId: String,
    @ColumnInfo(name = "has_whatsapp") val hasWhatsapp: Int,
    @ColumnInfo(name = "notification_Sound") var notificationSound: Int,
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)