package com.yellowtwigs.knockin.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.ModelDB.GroupDB
import com.yellowtwigs.knockin.model.ModelDB.GroupWithContact
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation

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
    //get touts les groupes de la database trié par nom de A à Z
    @Query("SELECT * FROM groups_table ORDER BY name ASC")
    fun getAllGroupsByNameAZ(): List<GroupWithContact>

    @Query("SELECT * FROM groups_table WHERE name = :groupName")
    fun getGroupWithName(groupName: String): GroupDB

    //get touts les groupes de la database trié par nom de Z à A
    @Query("SELECT * FROM groups_table ORDER BY name DESC")
    fun getAllGroupsByNameZA(): List<GroupDB>

    //get un groupe grace à son id
    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroup(id: Int): GroupDB

    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroupWithContact(id: Int): GroupWithContact

    @Query("SELECT * FROM groups_table INNER JOIN link_contact_group_table ON groups_table.id = link_contact_group_table.id_group WHERE id_contact = :contactId")
    fun getGroupForContact(contactId: Int): List<GroupDB>

    //get nb of member in a group

    //insert le groupe dans la database
    @Insert
    fun insert(groups: GroupDB): Long?

    //update le nom du groupe grâce à son id
    @Query("UPDATE groups_table SET name = :name WHERE id = :id")
    fun updateGroupNameById(id: Int, name: String)

    //update la section d'un groupe grâce à son id
    @Query("UPDATE groups_table SET section_color = :section_color WHERE id = :id")
    fun updateGroupSectionColorById(id: Int, section_color: Int)

    //delete un groupe grace à son id
    @Query("DELETE FROM groups_table WHERE id = :id")
    fun deleteGroupById(id: Int)

    //delete tout les groupes de la database
    @Query("DELETE FROM groups_table")
    fun deleteAll()

    @Query("SELECT MAX(id)  FROM groups_table")
    fun getIdNeverUsed(): Int

}