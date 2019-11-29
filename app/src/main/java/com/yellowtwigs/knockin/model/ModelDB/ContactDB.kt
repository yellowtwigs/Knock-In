package com.yellowtwigs.knockin.model.ModelDB

import androidx.room.*

/**
 * Data class qui représente un contact
 * @author Ryan Granet
 */
@Entity(tableName = "contacts_table")
data class ContactDB(
        /**
         * Id du contact.
         */
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Int?,
        /**
         * Prénom du contact.
         */
        @ColumnInfo(name = "first_name") val firstName: String,
        /**
         * Nom du contact.
         */
        @ColumnInfo(name = "last_name") var lastName: String,
        /**
         * Nom du compte mail du contact s'il est différent de son nom, prénom
         */
        @ColumnInfo(name = "mail_name") var mail_name: String,
        /**
         * Avatar utilisé lorsque le contact ne possède pas d'image de profile.
         */
        @ColumnInfo(name = "profile_picture") val profilePicture: Int,
        /**
         * Priorité du contact allant de 0 à 2.
         */
        @ColumnInfo(name = "contact_priority") val contactPriority: Int,
        /**
         * Image de profile du contact convertis en base 64.
         */
        @ColumnInfo(name = "profile_picture_str") val profilePicture64: String,
        /**
         * Le champ si Oui = 1 ou Non = 0, ce contact est un favori
         */
        @ColumnInfo(name = "is_favorite") val favorite: Int,
        /**
         * Id Messenger du contact
         */
        @ColumnInfo(name = "messenger_id") val messengerId: String

)
