package com.yellowtwigs.knockin.repositories.contacts.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class ContactsListRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    ContactsListRepository {

    override fun getAllContacts() = dao.getAllContacts().asLiveData()

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
}