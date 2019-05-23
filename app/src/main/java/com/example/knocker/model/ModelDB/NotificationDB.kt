package com.example.knocker.model.ModelDB

import androidx.room.*

@Entity(tableName = "notifications_table")
data class NotificationDB(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,     //id de la notification
        @ColumnInfo(name = "title") val title: String,                              //titre de la notification
        @ColumnInfo(name = "contact_name") val contactName: String,                 //nom du contact sur la notification
        @ColumnInfo(name = "description") val description: String,                  //description de la notification
        @ColumnInfo(name = "platform") val platform: String,                        //plateforme de la notification
        @ColumnInfo(name = "contact_priority") val contactPriority: Int,            //priorité du contact //TODELETE
        @ColumnInfo(name = "is_blacklist") val isBlacklist: Boolean,                //si la notification est blacklisté
        @ColumnInfo(name = "date_time") val dateTime: String,                       //la date et l'heure de la notification //TODELETE
        @ColumnInfo(name = "timestamp") val timestamp: Int,                         //timestamp de la notification
        @ColumnInfo(name = "is_cancellable") val isCancellable: Int,                //si la notification peut etre cancel
        @ColumnInfo(name = "app_image") val appImage: String                        //image de l'application qui à envoyé la notification //TODELETE
)