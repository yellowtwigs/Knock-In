package com.example.firsttestknocker

import android.arch.persistence.room.*

@Entity(tableName = "groups_table")
data class Groups(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,    //id du groupe
        @ColumnInfo(name = "name") val name: String,                                //nom du groupe
        @ColumnInfo(name = "members") val members: String,                          //membres du groupe
        @ColumnInfo(name = "nb_members") val nbMembers: Int,                        //nombre de membres
        @ColumnInfo(name = "profile_picture_str") val profilePicture: String        //image de profile du groupe
)