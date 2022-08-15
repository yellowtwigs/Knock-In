package com.yellowtwigs.knockin.repositories.contacts.list

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.data.ContactDB
import kotlinx.coroutines.flow.Flow

interface ContactsListRepository {

    fun getAllContacts(): Flow<List<ContactDB>>

    fun getContact(id: Int): Flow<ContactDB>

    suspend fun updateContact(contact: ContactDB)
    suspend fun deleteContact(contact: ContactDB)

    fun getSearchBarText(): LiveData<String>
    fun setSearchBarText(text: String)

    fun getSortedBy(): LiveData<Int>
    fun setSortedBy(sortBy: Int)

    fun getFilterBy(): LiveData<Int>
    fun setFilterBy(sortBy: Int)
}