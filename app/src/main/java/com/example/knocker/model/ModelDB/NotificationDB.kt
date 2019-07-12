package com.example.knocker.model.ModelDB

import androidx.room.*
import com.example.knocker.model.ContactsRoomDatabase

/**
 * Data class qui représente les notifications reçu
 * @author Ryan Granet
 */
@Entity(tableName = "notifications_table")
data class NotificationDB(
        /**
         * Id de la notification.
         */
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,
        /**
         * Titre de la notification.
         */
        @ColumnInfo(name = "title") val title: String,
        /**
         * Nom du contact sur la notification.
         */
        @ColumnInfo(name = "contact_name") val contactName: String,
        /**
         * Description de la notification.
         */
        @ColumnInfo(name = "description") val description: String,
        /**
         * Plateforme de la notification.
         */
        @ColumnInfo(name = "platform") val platform: String,
        /**
         * Booléen qui indique si la notification est blacklisté.
         */
        @ColumnInfo(name = "is_blacklist") val isBlacklist: Boolean,
        /**
         * Timestamp de la notification.
         */
        @ColumnInfo(name = "timestamp") val timestamp: Long,
        /**
         * Booléen qui indique si la notification peut etre annulée
         */
        @ColumnInfo(name = "is_cancellable") val isCancellable: Int,
        /**
         * Id du contact lié à la notification
         */
        @ColumnInfo(name = "id_contact") val idContact: Int
){
        fun insert(database: ContactsRoomDatabase){
                database.notificationsDao().insert(this)
        }

        override fun equals(other: Any?): Boolean {

                if(other is NotificationDB ){
                        if(other.contactName.equals(contactName) && other.description.equals(description) && other.timestamp.equals(timestamp)){
                                return true
                        }
                }else{
                        return false
                }

                return false
        }
}