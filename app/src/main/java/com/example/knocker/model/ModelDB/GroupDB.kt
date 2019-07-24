package com.example.knocker.model.ModelDB

import android.content.Context
import androidx.room.*
import com.example.knocker.R

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
){
    fun randomColorGroup(context: Context): Int {
        when (this.id!!.toInt() % 5) {
            0 -> {
                return context.resources.getColor(R.color.red_tag_group)
            }
            1 -> {
                return context.resources.getColor(R.color.blue_tag_group)
            }
            2 -> {
                return context.resources.getColor(R.color.green_tag_group)
            }
            3 -> {
                return context.resources.getColor(R.color.yellow_tag_group)
            }
            4 -> {
                return context.resources.getColor(R.color.orange_tag_group)
            }
        }
        return R.color.lightGreyColor
    }
}