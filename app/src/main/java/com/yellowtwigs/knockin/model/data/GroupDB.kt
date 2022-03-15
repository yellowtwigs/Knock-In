package com.yellowtwigs.knockin.model.data

import androidx.room.*

/**
 * Data class qui représente un groupe
 * @author Ryan Granet
 */
@Entity(tableName = "groups_table")
data class GroupDB(
    /**
     * Nom du groupe.
     */
    @ColumnInfo(name = "name") var name: String,
    /**
     * Image du groupe convertis en base 64.
     */
    @ColumnInfo(name = "profile_picture_str") val profilePicture: String,
    /**
     * Couleurs des sections.
     */
    @ColumnInfo(name = "section_color") var section_color: Int,

    /**
     * Id du groupe.
     */
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
    )