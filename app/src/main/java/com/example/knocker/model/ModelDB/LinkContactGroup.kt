package com.example.knocker.model.ModelDB

import androidx.annotation.NonNull
import androidx.room.*
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.GroupDB

/**
 * Data class qui représente un lien entre un contact et un groupe
 * @author Ryan Granet
 */
@Entity(tableName = "link_contact_group_table",
        primaryKeys = arrayOf("id_group","id_contact"),
        foreignKeys = arrayOf(
                ForeignKey(entity = GroupDB::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("id_group"),
                        onDelete = ForeignKey.CASCADE),
                ForeignKey(entity = ContactDB::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("id_contact"),
                        onDelete = ForeignKey.CASCADE))
        ,indices = arrayOf(Index(value = arrayOf("id_group", "id_contact"), unique = true)))
data class LinkContactGroup(
        /**
         * Id du [groupe][GroupDB] auquel appartient le contact.
         */
        @ColumnInfo(name = "id_group")  val idGroup: Int,
        /**
         * Id du [contact][ContactDB] lié au groupe.
         */
        @ColumnInfo(name = "id_contact") val idContact: Int
)