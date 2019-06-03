package com.example.knocker.model.ModelDB

import androidx.room.*
import com.example.knocker.model.ModelDB.ContactDB

/**
 * Data class qui représente un détails d'un contact par exemple un mail, numero de téléphone, etc...
 * @author Ryan Granet
 */
@Entity(tableName = "contact_details_table",
        foreignKeys = arrayOf(
                ForeignKey(entity = ContactDB::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("id_contact"),
                onDelete = ForeignKey.CASCADE)))
data class ContactDetailDB(
        /**
         * Id du contact Detail.
         */
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,
        /**
         * Id du [contact][ContactDB].
         */
        @ColumnInfo(name = "id_contact") var idContact: Int?,
        /**
         * Contenu du contact detail exemple: "06 24 25 45 .." ou "test.test@gmail.com"
         */
        @ColumnInfo(name = "content") val content: String,
        /**
         * Type du "contenu" exemple: phoneNumber ou mail
         */
        @ColumnInfo(name = "type") val type: String,
        /**
         * Tag pour spécifier le contenu, exemple: Principal, Mobile, Bureau...
         */
        @ColumnInfo(name = "tag") val tag: String,
        /**
         * La position du champ lors de l'edition d'un contact
         */
        @ColumnInfo(name = "field_position") val fieldPosition: Int
)