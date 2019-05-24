package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.ContactDetailDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact detail
 */
@Dao
interface ContactDetailsDao {
    /**
     * Récupère toutes les [notifications][ContactDetailDB] de la database
     * @return List&lt[ContactDetailDB]&gt
     */
    @Query("SELECT * FROM contact_details_table where tag='phone'AND id_contact=:id")
    fun getPhoneNumberById(id:Int?): ContactDetailDB

    @Query("SELECT * FROM contact_details_table where tag='mail'AND id_contact=:id")
    fun getMailById(id:Int?): ContactDetailDB

    @Query("SELECT * FROM contact_details_table")
    fun getAllpropertiesEditContact():List<ContactDetailDB>

    @Query("SELECT * FROM contact_details_table WHERE id_contact=:contactID")
    fun getDetailsForAContact(contactID:Int):List<ContactDetailDB>

    //update un contact_detail grace à son id
    @Query("UPDATE contact_details_table SET content = :contactDetail WHERE id = :id")
    fun updateContactDetailById(id: Int, contactDetail: String)

    @Insert
    fun insert(contactDetailDB: ContactDetailDB)

    @Query("SELECT * FROM contact_details_table")
    fun getAllDetails():List<ContactDetailDB>
}