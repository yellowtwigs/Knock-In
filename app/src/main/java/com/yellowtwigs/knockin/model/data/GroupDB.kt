package com.yellowtwigs.knockin.model.data

import androidx.room.*

/**
 * Data class qui représente un groupe
 * @author Ryan Granet
 */
@Entity(tableName = "groups_table")
data class GroupDB(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "profile_picture_str") val profilePicture: String,
    @ColumnInfo(name = "section_color") var section_color: Int
//    @ColumnInfo(name = "priority") var priority: Int
)