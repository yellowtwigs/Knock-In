package com.example.knocker.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactsDao {

    //trier les contacts par prénom A->Z
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE ASC")
    fun sortContactByFirstNameAZ(): List<Contacts>

    //trier les contacts par prénom Z->A
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE DESC")
    fun sortContactByFirstNameZA(): List<Contacts>

    //trier les contacts par nom A->Z
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE ASC")
    fun sortContactByLastNameAZ(): List<Contacts>

    //trier les contacts par nom Z->A
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE DESC")
    fun sortContactByLastNameZA(): List<Contacts>

    //trier les contacts par priorité 0->2
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority ASC")
    fun sortContactByPriority02(): List<Contacts>

    //trier les contacts par priorité 2->0
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority DESC")
    fun sortContactByPriority20(): List<Contacts>

    //get tout les contacts de la database
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): List<Contacts>

    //get un contact grace à son id
    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): Contacts

    //get des contacts grace à leurs name/lastname
    @Query("SELECT * FROM contacts_table WHERE instr(lower(first_name), :name) > 0 OR instr(lower(last_name), :name) > 0")
    fun getContactByName(name: String): List<Contacts>

    //add un contact
    @Insert
    fun insert(contacts: Contacts)

    @Query("INSERT INTO contact_details_table VALUES(LAST_INSERT_ID(),:contactDetails, :tag) ")
    fun insertWithContactDetail(contacts: Contacts, contactDetails: String,tag:String)

    //update un contact grace à son id
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, profile_picture = :profilePicture, profile_picture_str = :profilePicture64, contact_priority = :priority WHERE id = :id")
    fun updateContactById(id: Int, firstName: String, lastName: String, profilePicture: Int, profilePicture64: String, priority: Int)

    //update un contact grace à son id
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, profile_picture = :profilePicture, contact_priority = :priority WHERE id = :id")
    fun updateContactByIdWithoutPic(id: Int, firstName: String, lastName: String, profilePicture: Int, priority: Int)

    //delete un contact grace à un id
    @Query("DELETE FROM contacts_table WHERE id = :id")
    fun deleteContactById(id: Int)

    //delete tout les contacts de la database
    @Query("DELETE FROM contacts_table")
    fun deleteAll()

    //get les contacts qui possèdent un mail
    @Query("SELECT * FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id = contacts_table.id WHERE tag='mail'")
    fun getContactWithMail(): List<Contacts>

    //get les contacts qui possède un numéro
    @Query("SELECT * FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id = contacts_table.id WHERE tag='phone'")
    fun getContactWithPhoneNumber(): List<Contacts>


}