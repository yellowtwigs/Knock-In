package com.example.knocker.model.ModelDB

import androidx.room.*

@Entity(tableName = "groups_table")
data class GroupDB(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,    //id du groupe
        @ColumnInfo(name = "name") val name: String,                                //nom du groupe
        @ColumnInfo(name = "nb_members") val nbMembers: Int,                        //nombre de membres //TODELETE
        @ColumnInfo(name = "profile_picture_str") val profilePicture: String        //image de profile du groupe
)