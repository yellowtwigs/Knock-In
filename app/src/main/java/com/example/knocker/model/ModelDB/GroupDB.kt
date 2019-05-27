package com.example.knocker.model.ModelDB

import androidx.room.*

/**
 * Data class qui représente un groupe
 * @author Ryan Granet
 */
@Entity(tableName = "groups_table")
data class GroupDB(
        /**
         * Id du groupe.
         */
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,    //id du groupe
        /**
         * Nom du groupe.
         */
        @ColumnInfo(name = "name") val name: String,                                //nom du groupe
        /**
         * Image du groupe convertis en base 64.
         */
        @ColumnInfo(name = "profile_picture_str") val profilePicture: String        //image de profile du groupe
)