package com.example.knocker.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDetailsDao {

    @Query("SELECT * FROM contact_details_table where tag='phone'AND id_contact=:id")
    fun getPhoneNumberById(id:Long?):ContactDetails

    @Query("SELECT * FROM contact_details_table where tag='mail'AND id_contact=:id")
    fun getMailById(id:Long?):ContactDetails

    @Query("SELECT * FROM contact_details_table")
    fun getAllpropertiesEditContact():List<ContactDetails>

    //update un contact_detail grace à son id
    @Query("UPDATE contact_details_table SET contact_details = :contactDetail WHERE id = :id")
    fun updateContactDetailById(id: Int, contactDetail: String)

    @Insert
    fun insert(contactDetails:ContactDetails)
}