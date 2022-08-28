package com.yellowtwigs.knockin.repositories.contacts.edit

import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import javax.inject.Inject

class EditContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    EditContactRepository {

    override suspend fun updateContactPriority1To0() = dao.updateContactPriority1To0()
    override suspend fun updateContactPriority0To1() = dao.updateContactPriority0To1()
}