package com.example.knocker.model

import androidx.room.*

@Entity(tableName = "contact_details_table", foreignKeys = arrayOf(ForeignKey(entity = Contacts::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id_contact"),
        onDelete = ForeignKey.CASCADE)))
data class ContactDetails(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,    //id du contact
        @ColumnInfo(name = "id_contact") val idContact: Long?,                      //id du contact
        @ColumnInfo(name = "contact_details") val contactDetails: String,           //
        @ColumnInfo(name = "tag") val tag: String,                                  //
        @ColumnInfo(name = "field_position") val fieldPosition: Int                 //
)