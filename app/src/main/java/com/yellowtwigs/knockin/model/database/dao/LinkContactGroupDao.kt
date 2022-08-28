package com.yellowtwigs.knockin.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.database.data.LinkContactGroup

@Dao
interface LinkContactGroupDao {
    @Query("SELECT * FROM link_contact_group_table")
    fun getAllLinkContactGroup(): List<LinkContactGroup>

    @Insert
    fun insert(linkContactGroup : LinkContactGroup)

    @Query("DELETE FROM link_contact_group_table WHERE id_contact = :idContact AND id_group=:idGroup")
    fun deleteContactIngroup(idContact:Int,idGroup:Int)
}