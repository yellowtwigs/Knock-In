package com.yellowtwigs.knockin.models.data

import androidx.room.*

@Entity(tableName = "vip_notifications_table")
data class VipNotificationsDB (
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,
        @ColumnInfo(name = "notification_id") val notificationId: Int,
        @ColumnInfo(name = "app_notifier") val appNotifier: String,
        @ColumnInfo(name = "list_size") val listSize: Int,
        @ColumnInfo(name = "notification_text") val notificationText: String
)