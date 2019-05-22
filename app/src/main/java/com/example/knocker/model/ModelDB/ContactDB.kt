package com.example.knocker.model.ModelDB

import androidx.room.*

@Entity(tableName = "contacts_table")
data class ContactDB(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Int?,    //id du contact
        @ColumnInfo(name = "first_name") val firstName: String,                     //prénom du contact
        @ColumnInfo(name = "last_name") var lastName: String,                       //nom du contact
        @ColumnInfo(name = "profile_picture") val profilePicture: Int,              //image de profile du contact
        @ColumnInfo(name = "background_image") val backgroundImage: Int,            //image d'arriere plan du contact
        @ColumnInfo(name = "contact_priority") val contactPriority: Int,            //priorité du contact
        @ColumnInfo(name = "profile_picture_str") val profilePicture64: String
)