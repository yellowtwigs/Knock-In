package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.models.dao.ContactsDao
import com.yellowtwigs.knockin.models.data.Contact
import javax.inject.Inject

class ContactRepository @Inject constructor(private val dao: ContactsDao) :
    DefaultContactRepository {

    override fun getAllContacts() = dao.getAllContacts()
    override fun getContact(id: Int) = dao.getContact(id)
    override fun sortContactByFirstNameAZ() = dao.sortContactByFirstNameAZ()

    override fun sortContactByLastNameAZ() = dao.sortContactByFirstNameAZ()
    override fun sortContactByPriority20() = dao.sortContactByPriority20()
    override fun sortContactByFavorite() = dao.sortContactByFavorite()

    override suspend fun insert(contact: Contact) = dao.insert(contact)
    override suspend fun updateContact(contact: Contact) = dao.updateContact(contact)
    override suspend fun deleteContact(contact: Contact) = dao.deleteContact(contact)
    override suspend fun deleteAll(contacts: List<Contact>) = dao.deleteAll(contacts)
}