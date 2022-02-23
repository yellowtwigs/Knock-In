package com.yellowtwigs.knockin.model.ModelDB

import androidx.room.*
import com.yellowtwigs.knockin.model.ContactsRoomDatabase

@Entity(tableName = "vip_notifications_table")
data class VipNotificationsDB (
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,
        @ColumnInfo(name = "notification_id") val notificationId: Int,
        @ColumnInfo(name = "app_notifier") val appNotifier: String,
        @ColumnInfo(name = "list_size") val listSize: Int,
        @ColumnInfo(name = "notification_text") val notificationText: String
)