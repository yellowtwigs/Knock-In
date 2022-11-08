package com.yellowtwigs.knockin.repositories.contacts.list

import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class ContactsListRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    ContactsListRepository {

    override fun getAllContacts() = dao.getAllContacts()

    override fun getNumbersOfContactsVip() = dao.getNumbersOfContactsVip()

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
    override suspend fun deleteContactById(id: Int) = dao.deleteContactById(id)
}