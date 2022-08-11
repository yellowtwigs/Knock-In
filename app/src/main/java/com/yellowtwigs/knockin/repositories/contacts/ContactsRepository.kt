package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.model.data.ContactDB
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {

    fun getAllContacts(): Flow<List<ContactDB>>
    fun getContact(id: Int): Flow<ContactDB>

    suspend fun insertContact(contact: ContactDB): Long
    suspend fun updateContact(contact: ContactDB)
    suspend fun deleteContact(contact: ContactDB)
}