package com.yellowtwigs.knockin.repositories.contacts.create

import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class CreateContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    CreateContactRepository {

    override suspend fun insertContact(contact: ContactDB) = dao.insertContact(contact)
}