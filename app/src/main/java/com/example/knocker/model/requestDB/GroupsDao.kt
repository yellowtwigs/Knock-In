package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.GroupDB

@Dao
interface GroupsDao {

    //get touts les groupes de la database trié par nom de A à Z
    @Query("SELECT * FROM groups_table ORDER BY name ASC")
    fun getAllGroupsByNameAZ(): List<GroupDB>

    //get touts les groupes de la database trié par nom de Z à A
    @Query("SELECT * FROM groups_table ORDER BY name DESC")
    fun getAllGroupsByNameZA(): List<GroupDB>

    //get un groupe grace à son id
    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroup(id: Int): GroupDB

    //get nb of member in a group

    //insert le groupe dans la database
    @Insert
    fun insert(groups: GroupDB)

    //update un groupe grace à son id
    @Query("UPDATE groups_table SET name = :name, nb_members = :nbMembers, profile_picture_str = :profilePicture")
    fun updateGroupeById(id: Int, name: String, nbMembers: Int, profilePicture: String)

    //delete un groupe grace à son id
    @Query("DELETE FROM groups_table WHERE id = :id")
    fun deleteGroupById(id: Int)

    //delete tout les groupes de la database
    @Query("DELETE FROM groups_table")
    fun deleteAll()
}