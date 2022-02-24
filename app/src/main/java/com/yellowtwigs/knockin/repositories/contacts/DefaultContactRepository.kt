package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.models.data.Contact
import kotlinx.coroutines.flow.Flow

interface DefaultContactRepository {

    //region ============================================== GET =============================================

    fun getAllContacts(): Flow<List<Contact>>
    fun getContact(id: Int): Flow<Contact>

    //endregion

    //region ============================================= SORT =============================================

    fun sortContactByFirstNameAZ(): Flow<List<Contact>>
    fun sortContactByLastNameAZ(): Flow<List<Contact>>
    fun sortContactByPriority20(): Flow<List<Contact>>
    fun sortContactByFavorite(): Flow<List<Contact>>

    //endregion

    //region ============================================ INSERT ============================================

    suspend fun insert(contact: Contact)

    //endregion

    //region ============================================ UPDATE ============================================

    suspend fun updateContact(contact: Contact)

    //endregion

    //region ============================================ DELETE ============================================

    suspend fun deleteContact(contact: Contact)
    suspend fun deleteAll(contacts: List<Contact>)

    //endregion
}