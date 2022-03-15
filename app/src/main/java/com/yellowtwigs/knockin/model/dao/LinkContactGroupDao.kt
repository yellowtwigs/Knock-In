package com.yellowtwigs.knockin.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkContactGroupDao {
    @Query("SELECT * FROM link_contact_group_table")
    fun getAllLinkContactGroup(): Flow<List<LinkContactGroup>>

    @Insert
    suspend fun insert(linkContactGroup : LinkContactGroup)

    @Query("DELETE FROM link_contact_group_table WHERE id_contact = :idContact AND id_group=:idGroup")
    suspend fun deleteContactInGroup(idContact:Int, idGroup:Int)
}