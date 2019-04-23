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

    //get des contacts grace à leurs name/lastname
    @Query("SELECT * FROM contacts_table WHERE instr(lower(first_name), :name) > 0 OR instr(lower(last_name), :name) > 0")
    fun getContactByName(name: String): List<Contacts>

    //get les contacts qui possèdent un mail
    @Query("SELECT * FROM contacts_table WHERE mail <> ''")
    fun getContactWithMail(): List<Contacts>

    //get les contacts qui possède un numéro
    @Query("SELECT * FROM contacts_table WHERE phone_number <> ''")
    fun getContactWithPhoneNumber(): List<Contacts>

    //add un contact
    @Insert
    fun insert(contacts: Contacts)

    //update un contact grace à son id
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, phone_number = :phoneNumber, mail = :mail, profile_picture = :profilePicture, profile_picture_str = :profilePicture64, contact_priority = :priority WHERE id = :id")
    fun updateContactById(id: Int, firstName: String, lastName: String, phoneNumber: String, mail: String, profilePicture: Int, profilePicture64: String, priority: Int)

    //update un contact grace à son id
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, phone_number = :phoneNumber, mail = :mail, profile_picture = :profilePicture, contact_priority = :priority WHERE id = :id")
    fun updateContactByIdWithoutPic(id: Int, firstName: String, lastName: String, phoneNumber: String, mail: String, profilePicture: Int, priority: Int)

    //delete un contact grace à un id
    @Query("DELETE FROM contacts_table WHERE id = :id")
    fun deleteContactById(id: Int)

    //delete tout les contacts de la database
    @Query("DELETE FROM contacts_table")
    fun deleteAll()

}