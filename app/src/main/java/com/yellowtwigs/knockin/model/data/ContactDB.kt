package com.yellowtwigs.knockin.model.data

import androidx.room.*
import com.yellowtwigs.knockin.R

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
        @ColumnInfo(name = "messenger_id") val messengerId: String,
        /**
         * Le champ si Oui = 1 ou Non = 0, l'utisateur possède ce contact dans ses contacts Whatsapp
         */
        @ColumnInfo(name = "has_whatsapp") val hasWhatsapp: Int,
        @ColumnInfo(name = "notification_tone") var notificationTone: String,
        @ColumnInfo(name = "notification_Sound") var notificationSound: Int = R.raw.sms_ring,
        /**
         * Le champ si Oui = 1 ou Non = 0, l'utisateur a configuré un son customisé
         */
        @ColumnInfo(name = "is_custom_sound") var isCustomSound: Int,
        /**
         * Si 1 = Permanent, 2 = Daytime, 3 = Workweek, 4 = ScheduleMix
         */
        @ColumnInfo(name = "vip_schedule") var vipSchedule: Int,
        /**
         * 10h30 to 19h30
         */
        @ColumnInfo(name = "hour_limit_for_notification") var hourLimitForNotification: String
)