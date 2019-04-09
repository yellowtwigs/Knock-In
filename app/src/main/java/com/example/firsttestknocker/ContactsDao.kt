package com.example.firsttestknocker

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface ContactsDao {

    //get tout les contacts de la database
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): List<Contacts>

    //get un contact grace à son id
    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): Contacts

    //add un contact
    @Insert
    fun insert(contacts: Contacts)

    //update un contact grace à son id
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, phone_number = :phoneNumber, profile_picture = :profilePicture, background_image = :backgroundImage WHERE id = :id")
    fun updateContactById(id: Int, firstName: String, lastName: String, phoneNumber: String, profilePicture: Int, backgroundImage: Int)

    //delete un contact grace à un id
    @Query("DELETE FROM contacts_table WHERE id = :id")
    fun deleteContactById(id: Int)

    //delete tout les contacts de la database
    @Query("DELETE FROM contacts_table")
    fun deleteAll()

}