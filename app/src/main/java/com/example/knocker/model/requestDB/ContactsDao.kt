package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation

@Dao
interface ContactsDao {

    //trier les contacts par prénom A->Z
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE ASC")
    fun sortContactByFirstNameAZ(): List<ContactWithAllInformation>

    //trier les contacts par prénom Z->A
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE DESC")
    fun sortContactByFirstNameZA(): List<ContactDB>

    //trier les contacts par nom A->Z
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE ASC")
    fun sortContactByLastNameAZ(): List<ContactDB>

    //trier les contacts par nom Z->A
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE DESC")
    fun sortContactByLastNameZA(): List<ContactDB>

    //trier les contacts par priorité 0->2
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority ASC")
    fun sortContactByPriority02(): List<ContactDB>

    //trier les contacts par priorité 2->0
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority DESC")
    fun sortContactByPriority20(): List<ContactDB>

    //get tout les contacts de la database
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): List<ContactDB>

    //get un contact grace à son id
    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): ContactWithAllInformation

    //get des contacts grace à leurs name/lastname
    @Query("SELECT * FROM contacts_table WHERE instr(lower(first_name), :name) > 0 OR instr(lower(last_name), :name) > 0")
    fun getContactByName(name: String): List<ContactWithAllInformation>

    //add un contact
    @Insert
    fun insert(contactDB: ContactDB):Long?

    @Insert
    fun insertDetails(contactDetails: List<ContactDetailDB>)
    //update un contact grace à son id //DELETE PP and BPP
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, profile_picture = :profilePicture, profile_picture_str = :profilePicture64, contact_priority = :priority WHERE id = :id")
    fun updateContactById(id: Int, firstName: String, lastName: String, profilePicture: Int, profilePicture64: String, priority: Int)

    //update un contact grace à son id //DELETE PP and BPP
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, profile_picture = :profilePicture, contact_priority = :priority WHERE id = :id")
    fun updateContactByIdWithoutPic(id: Int, firstName: String, lastName: String, profilePicture: Int, priority: Int)

    //delete un contact grace à un id
    @Query("DELETE FROM contacts_table WHERE id = :id")
    fun deleteContactById(id: Int)

    //delete tout les contacts de la database
    @Query("DELETE FROM contacts_table")
    fun deleteAll()

    //get les contacts qui possèdent un mail
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE tag='mail'")
    fun getContactWithMail(): List<ContactWithAllInformation>

    //get les contacts qui possède un numéro
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE tag='phone'")
    fun getContactWithPhoneNumber(): List<ContactWithAllInformation>

    @Query("SELECT * FROM contacts_table")
    fun getContactAllInfo():List<ContactWithAllInformation>

}