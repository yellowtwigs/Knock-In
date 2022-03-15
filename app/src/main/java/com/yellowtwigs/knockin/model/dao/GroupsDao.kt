package com.yellowtwigs.knockin.model.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.data.GroupDB
import com.yellowtwigs.knockin.model.data.GroupWithContact
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import kotlinx.coroutines.flow.Flow

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table groups
 * @author Ryan Granet
 */
@Dao
interface GroupsDao {
    /**
     * Récupère touts les [groupes][GroupDB] trier par prénom A->Z
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM groups_table ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupWithContact>>

    @Query("SELECT * FROM groups_table WHERE name = :groupName")
    fun getGroupWithName(groupName: String): Flow<GroupDB>

    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroupById(id: Int): Flow<GroupDB>

    @Query("SELECT * FROM groups_table INNER JOIN link_contact_group_table ON groups_table.id = link_contact_group_table.id_group WHERE id_contact = :contactId")
    fun getGroupsForContact(contactId: Int): Flow<List<GroupDB>>

    @Insert
    suspend fun insertGroup(group: GroupDB): Long

    @Update
    suspend fun updateGroup(group: GroupDB)

    @Query("UPDATE groups_table SET section_color = :section_color WHERE id = :id")
    suspend fun updateGroupSectionColorById(id: Int, section_color: Int)

    @Delete
    suspend fun deleteGroup(group: GroupDB)

    @Query("SELECT MAX(id)  FROM groups_table")
    fun getIdNeverUsed(): Int
}