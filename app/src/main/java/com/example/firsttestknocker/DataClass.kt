package com.example.firsttestknocker

import android.arch.persistence.room.*

@Entity(tableName = "contacts_table")
data class Contacts(
        @PrimaryKey(autoGenerate = true) val id: Long?,
        @ColumnInfo(name = "first_name") val firstName: String,
        @ColumnInfo(name = "last_name") val lastName: String,
        @ColumnInfo(name = "phone_number") val phoneNumber: String
)