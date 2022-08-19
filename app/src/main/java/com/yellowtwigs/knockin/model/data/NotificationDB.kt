package com.yellowtwigs.knockin.model.data

import androidx.room.*
import com.yellowtwigs.knockin.model.ContactsDatabase

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
        @ColumnInfo(name = "priority") val priority: Int
)