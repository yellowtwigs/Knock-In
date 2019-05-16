package com.example.knocker.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GroupsDao {

    //get touts les groupes de la database trié par nom de A à Z
    @Query("SELECT * FROM groups_table ORDER BY name ASC")
    fun getAllGroupsByNameAZ(): List<Groups>

    //get touts les groupes de la database trié par nom de Z à A
    @Query("SELECT * FROM groups_table ORDER BY name DESC")
    fun getAllGroupsByNameZA(): List<Groups>

    //get un groupe grace à son id
    @Query("SELECT * FROM groups_table WHERE id = :id")
    fun getGroup(id: Int): Groups

    //insert le groupe dans la database
    @Insert
    fun insert(groups: Groups)

    //update un groupe grace à son id
    @Query("UPDATE groups_table SET name = :name, members = :members, nb_members = :nbMembers, profile_picture_str = :profilePicture")
    fun updateGroupeById(id: Int, name: String, members: String, nbMembers: Int, profilePicture: String)

    //delete un groupe grace à son id
    @Query("DELETE FROM groups_table WHERE id = :id")
    fun deleteGroupById(id: Int)

    //delete tout les groupes de la database
    @Query("DELETE FROM groups_table")
    fun deleteAll()
}