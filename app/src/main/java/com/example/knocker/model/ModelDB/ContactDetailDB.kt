package com.example.knocker.model.ModelDB

import androidx.room.*
import com.example.knocker.model.ModelDB.ContactDB

@Entity(tableName = "contact_details_table", foreignKeys = arrayOf(ForeignKey(entity = ContactDB::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("id_contact"),
        onDelete = ForeignKey.CASCADE)))
data class ContactDetailDB(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Int?,    //id du contact
        @ColumnInfo(name = "id_contact") var idContact: Int?,                      //id du contact
        @ColumnInfo(name = "contact_details") val contactDetails: String,           //
        @ColumnInfo(name = "tag") val tag: String,                                  //
        @ColumnInfo(name = "field_position") val fieldPosition: Int                 //
)