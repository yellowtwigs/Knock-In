package com.yellowtwigs.knockin.models.data

import androidx.room.*

/**
 * Data class qui représente un lien entre un contact et un groupe
 * @author Ryan Granet
 */
@Entity(tableName = "link_contact_group_table",
        primaryKeys = ["id_group","id_contact"],
        foreignKeys = [
                ForeignKey(entity = GroupDB::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("id_group"),
                        onDelete = ForeignKey.CASCADE
                ),
                ForeignKey(entity = Contact::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("id_contact"),
                        onDelete = ForeignKey.CASCADE
                )]
        ,indices = [Index(value = arrayOf("id_group", "id_contact"), unique = true)])
data class LinkContactGroup(
        /**
         * Id du [groupe][GroupDB] auquel appartient le contact.
         */
        @ColumnInfo(name = "id_group")  val idGroup: Int,
        /**
         * Id du [contact][Contact] lié au groupe.
         */
        @ColumnInfo(name = "id_contact") val idContact: Int
)