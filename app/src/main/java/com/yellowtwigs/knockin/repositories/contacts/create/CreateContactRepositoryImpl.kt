package com.yellowtwigs.knockin.repositories.contacts.create

import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.data.ContactDB
import javax.inject.Inject

class CreateContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    CreateContactRepository {

    override suspend fun insertContact(contact: ContactDB) = dao.insertContact(contact)
}