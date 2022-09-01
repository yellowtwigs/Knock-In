package com.yellowtwigs.knockin.model.database.data

import androidx.room.*

@Entity(tableName = "groups_table")
data class GroupDB(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "profile_picture_str") val profilePicture: String,
    @ColumnInfo(name = "section_color") var section_color: Int,
    @ColumnInfo(name = "listOfContactsId") val listOfContactsId: List<String>,
    @ColumnInfo(name = "priority") var priority: Int
)