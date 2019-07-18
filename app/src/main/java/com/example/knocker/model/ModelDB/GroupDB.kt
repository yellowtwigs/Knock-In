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
                println("id group" + this.id!!.toInt() + "modulo 3 égale" + this.id!!.toInt() % 3)
                when (this.id!!.toInt() % 3) {
                        0 -> {
                                println("return 0")
                                return context.getResources().getColor(R.color.redColorDark)
                        }
                        1 -> {
                                println("return 1")
                                return context.getResources().getColor(R.color.com_facebook_blue)
                        }
                        2 -> {
                                println("return 2")
                                return context.getResources().getColor(R.color.rounded_button_pressed_blue)
                        }
                }
                return R.color.lightGreyColor
        }
}