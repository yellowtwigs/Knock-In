package com.yellowtwigs.knockin.model.database.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupsDao {

    //region ============================================= GET ==============================================

    @Query("SELECT * FROM groups_table ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupDB>>

    @Query("SELECT * FROM groups_table WHERE name = :groupName")
    fun getGroupWithName(groupName: String): Flow<GroupDB>

    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroup(id: Int): Flow<GroupDB>

    //endregion

    //region ============================================ INSERT ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupDB)

    //endregion

    //region ============================================ UPDATE ============================================

    @Update
    suspend fun updateGroup(group: GroupDB)

    @Query("UPDATE groups_table SET priority = :priority WHERE id = :id")
    suspend fun updateGroupPriorityById(id: Int, priority: Int)

    @Query("UPDATE groups_table SET name = :name WHERE id = :id")
    suspend fun updateGroupNameById(id: Int, name: String)

    @Query("UPDATE groups_table SET section_color = :section_color WHERE id = :id")
    suspend fun updateGroupSectionColorById(id: Int, section_color: Int)

    //endregion

    //region ============================================ DELETE ============================================

    @Delete
    suspend fun deleteContact(group: GroupDB)

    @Query("DELETE FROM groups_table WHERE id = :id")
    suspend fun deleteGroupById(id: Int)

    @Query("DELETE FROM groups_table")
    suspend fun deleteAll()

    //endregion
}