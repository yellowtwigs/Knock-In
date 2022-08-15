package com.yellowtwigs.knockin.repositories.contacts.insert

import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.data.ContactDB
import javax.inject.Inject

class InsertContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    InsertContactRepository {

    override suspend fun insertContact(contact: ContactDB) = dao.insertContact(contact)
}