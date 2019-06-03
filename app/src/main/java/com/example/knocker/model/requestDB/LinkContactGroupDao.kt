package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.LinkContactGroup

@Dao
interface LinkContactGroupDao {
    @Query("SELECT * FROM link_contact_group_table")
    fun getAllLinkContactGroup(): List<LinkContactGroup>
    //insert le lien dans la database
    @Insert
    fun insert(linkContactGroup : LinkContactGroup)
}