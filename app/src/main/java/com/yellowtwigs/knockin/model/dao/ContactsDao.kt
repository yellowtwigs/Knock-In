package com.yellowtwigs.knockin.model.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.data.ContactDB
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    //region ============================================= GET ==============================================

    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): Flow<List<ContactDB>>

    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): Flow<ContactDB>

    //endregion

    //region ============================================ INSERT ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactDB: ContactDB): Long

    //endregion

    //region ============================================ UPDATE ============================================

    @Update
    suspend fun updateContact(contact: ContactDB)

    //endregion

    //region ============================================ DELETE ============================================

    @Delete
    suspend fun deleteContact(contact: ContactDB)

    //endregion
}