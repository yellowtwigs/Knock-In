package com.yellowtwigs.knockin.repositories.contacts.list

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.ContactDB
import kotlinx.coroutines.flow.Flow

interface ContactsListRepository {

    fun getAllContacts(): Flow<List<ContactDB>>

    fun getNumbersOfContactsVip(): Int

    suspend fun updateContactPriorityById(id: Int, priority: Int)

    suspend fun deleteContact(contact: ContactDB)
    suspend fun deleteContactById(id: Int)
}