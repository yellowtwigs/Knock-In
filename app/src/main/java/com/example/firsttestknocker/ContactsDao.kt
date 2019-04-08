package com.example.firsttestknocker

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface ContactsDao {

    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): List<Contacts>

    @Insert
    fun insert(contacts: Contacts)

    @Query("DELETE FROM contacts_table")
    fun deleteAll()
}