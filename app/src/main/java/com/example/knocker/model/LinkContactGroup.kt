package com.example.knocker.model

import androidx.room.*

@Entity(tableName = "link_contact_group_table", foreignKeys = arrayOf(ForeignKey(entity = Contacts::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id_contact"),
        onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Groups::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id_group"),
        onDelete = ForeignKey.CASCADE)))
data class LinkContactGroup(
        @ColumnInfo(name = "id_group") val idGroup: Long?,                          //id du contact
        @ColumnInfo(name = "id_contact") val idContact: Long?                      //id du contact
)