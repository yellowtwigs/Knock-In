package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import com.example.knocker.model.ModelDB.LinkContactGroup

@Dao
interface LinkContactGroupDao {
    //insert le lien dans la database
    @Insert
    fun insert(linkContactGroup : LinkContactGroup)
}