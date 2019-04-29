package com.example.firsttestknocker

import android.arch.persistence.room.*

@Entity(tableName = "notifications_table")
data class Notifications(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,     //id de la notification
        @ColumnInfo(name = "title") val title: String,                              //titre de la notification
        @ColumnInfo(name = "contact_name") val contactName: String,                 //nom du contact sur la notification
        @ColumnInfo(name = "description") val description: String,                  //description de la notification
        @ColumnInfo(name = "platform") val platform: String,                        //plateforme de la notification
        @ColumnInfo(name = "contact_priority") val contactPriority: Int,            //priorité du contact
        @ColumnInfo(name = "is_blacklist") val isBlacklist: Boolean,                //si la notification est blacklisté
        @ColumnInfo(name = "date_time") val dateTime: String,                       //la date et l'heure de la notification
        @ColumnInfo(name = "timestamp") val timestamp: Int                         //timestamp de la notification
)