package com.example.firsttestknocker

import android.arch.persistence.room.*

@Entity(tableName = "notifications_table")
data class Notifications(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,     //id de la notification
        @ColumnInfo(name = "title") val title: String,                              //titre de la notification
        @ColumnInfo(name = "contact_name") val contactName: String,                 //nom du contact sur la notification
        @ColumnInfo(name = "description") val description: String,                  //description de la notification
        @ColumnInfo(name = "platform") val platform: String                         //plateforme de la notification
)