package com.yellowtwigs.knockin.model.database.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.database.data.ContactDB
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    //region ============================================= GET ==============================================

    @Query("SELECT * FROM contacts_table ORDER BY priority ASC")
    fun getAllContacts(): Flow<List<ContactDB>>

    @Query("SELECT * FROM contacts_table ORDER BY full_name ASC")
    fun getAllContactsByFullName(): Flow<List<ContactDB>>

    @Query("SELECT * FROM contacts_table ORDER BY is_favorite ASC")
    fun getAllContactsByFavorite(): Flow<List<ContactDB>>

    @Query("SELECT * FROM contacts_table")
    fun getAllContactsForNotificationsListener(): List<ContactDB>

    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): Flow<ContactDB>

    @Query("SELECT * FROM contacts_table WHERE priority = 2")
    fun getAllContactsVIP(): Flow<List<ContactDB>>

    @Query("SELECT COUNT(priority) FROM contacts_table WHERE priority = 2")
    fun getNumbersOfContactsVip(): Int

    //endregion

    //region ============================================ INSERT ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactDB: ContactDB): Long

    //endregion

    //region ============================================ UPDATE ============================================

    @Update
    suspend fun updateContact(contact: ContactDB)

    @Query("UPDATE contacts_table SET priority = :priority WHERE id = :id")
    suspend fun updateContactPriorityById(id: Int, priority: Int)

    @Query("UPDATE contacts_table SET priority = 0 WHERE priority = 1")
    suspend fun updateContactPriority1To0()

    @Query("UPDATE contacts_table SET priority = 1 WHERE priority = 0")
    suspend fun updateContactPriority0To1()

    //endregion

    //region ============================================ DELETE ============================================

    @Delete
    suspend fun deleteContact(contact: ContactDB)

    @Query("DELETE from contacts_table WHERE id = :id")
    suspend fun deleteContactById(id: Int)

    //endregion
}