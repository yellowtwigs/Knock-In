package com.example.firsttestknocker

import android.arch.persistence.room.*

@Entity(tableName = "contacts_table")
data class Contacts(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,    //id du contact
        @ColumnInfo(name = "first_name") val firstName: String,                     //prénom du contact
        @ColumnInfo(name = "last_name") val lastName: String,                       //nom du contact
        @ColumnInfo(name = "phone_number") val phoneNumber: String,                 //numéro de téléphone du contact
        @ColumnInfo(name = "mail") val mail: String,                                //email du contact
        @ColumnInfo(name = "profile_picture") val profilePicture: Int,              //image de profile du contact
        @ColumnInfo(name = "background_image") val backgroundImage: Int,            //image d'arriere plan du contact
        @ColumnInfo(name = "contact_priority") val contactPriority: Int,            //priorité du contact
        @ColumnInfo(name = "profile_picture_str") val profilePicture64: String
)