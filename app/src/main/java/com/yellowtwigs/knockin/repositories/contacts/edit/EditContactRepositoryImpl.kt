package com.yellowtwigs.knockin.repositories.contacts.edit

import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class EditContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    EditContactRepository {

    override suspend fun updateContactPriority1To0() = dao.updateContactPriority1To0()
    override suspend fun updateContactPriority0To1() = dao.updateContactPriority0To1()

    override fun getContact(id: Int) = dao.getContact(id).asLiveData()

    override suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
}