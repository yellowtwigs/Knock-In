package com.yellowtwigs.knockin.model.database.data

import androidx.room.*

@Entity(tableName = "notifications_table")
data class NotificationDB(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "contact_name") val contactName: String,
        @ColumnInfo(name = "description") val description: String,
        @ColumnInfo(name = "platform") val platform: String,
        @ColumnInfo(name = "timestamp") val timestamp: Long,
        @ColumnInfo(name = "is_cancellable") val isCancellable: Int,
        @ColumnInfo(name = "id_contact") val idContact: Int,
        @ColumnInfo(name = "priority") val priority: Int,
        @ColumnInfo(name = "phone_number") val phoneNumber: String,
        @ColumnInfo(name = "mail") val mail: String,
        @ColumnInfo(name = "messenger_id") val messengerId: String,

        )