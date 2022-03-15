package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import javax.inject.Inject

class ContactsRepository @Inject constructor(private val dao: ContactsDao) {

    fun getAllContacts() = dao.getAllContacts()
    fun getContactAllInfo() = dao.getContactAllInfo()
    fun getContact(id: Int) = dao.getContact(id)
    fun getContactByName(name: String) = dao.getContactByName(name)
    fun getContactForGroup(groupId : Int) = dao.getContactForGroup(groupId)
    fun getContactWithPhoneNumber() = dao.getContactWithPhoneNumber()
    fun getContactWithMail() = dao.getContactWithMail()

    suspend fun insertContact(contact: ContactDB) = dao.insertContact(contact)
    suspend fun insertDetails(contactDetails: List<ContactDetailDB>) = dao.insertDetails(contactDetails)

    suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)

    suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
}