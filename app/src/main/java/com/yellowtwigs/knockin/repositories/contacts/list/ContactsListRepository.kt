package com.yellowtwigs.knockin.repositories.contacts.list

import com.yellowtwigs.knockin.model.database.data.ContactDB
import kotlinx.coroutines.flow.Flow

interface ContactsListRepository {

    fun getAllContacts(): Flow<List<ContactDB>>
    fun getAllContactsByFavorite(): Flow<List<ContactDB>>
    fun getAllContactsByFullName(): Flow<List<ContactDB>>
    fun getAllContactsDB(): List<ContactDB>

    fun getAllContactsVIP(): Flow<List<ContactDB>>
    fun getAllAndroidIds(): List<Int>

    fun getNumbersOfContactsVip(): Int

    suspend fun updateContactPriorityById(id: Int, priority: Int)

    suspend fun deleteContact(contact: ContactDB)
    suspend fun deleteContactById(id: Int)
}