package com.yellowtwigs.knockin.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vip_sbn_table")
data class VipSbnDB (
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,
        @ColumnInfo(name = "vip_notification_id") val notificationId: Int,
        @ColumnInfo(name = "sbn_key") val sbnKey: String,
        @ColumnInfo(name = "sbn_value") val sbnValue: String
)