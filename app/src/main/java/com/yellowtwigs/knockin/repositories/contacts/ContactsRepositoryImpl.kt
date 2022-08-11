package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.data.ContactDB
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    ContactsRepository {

    override fun getAllContacts() = dao.getAllContacts()
    override fun getContact(id: Int) = dao.getContact(id)

    override suspend fun insertContact(contact: ContactDB) = dao.insertContact(contact)
    override suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)
    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
}