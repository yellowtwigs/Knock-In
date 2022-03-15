package com.yellowtwigs.knockin.repositories.contacts

import com.yellowtwigs.knockin.model.dao.ContactDetailsDao
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import javax.inject.Inject

class ContactDetailsRepository @Inject constructor(private val dao: ContactDetailsDao) {

    fun getPhoneNumberById(id: Int) = dao.getPhoneNumberById(id)
    fun getMailById(id: Int) = dao.getMailById(id)

    fun getAllProperties() = dao.getAllProperties()

    fun getDetailsForAContact(contactID: Int) = dao.getDetailsForAContact(contactID)

    suspend fun updateContactDetail(contactDetailDB: ContactDetailDB) =
        dao.updateContactDetail(contactDetailDB)

    suspend fun insert(contactDetailDB: ContactDetailDB) = dao.insert(contactDetailDB)

    suspend fun deleteDetail(contactDetailDB: ContactDetailDB) = dao.deleteDetail(contactDetailDB)
}