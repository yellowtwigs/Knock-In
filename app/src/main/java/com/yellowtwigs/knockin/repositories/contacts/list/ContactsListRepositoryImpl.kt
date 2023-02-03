package com.yellowtwigs.knockin.repositories.contacts.list

import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class ContactsListRepositoryImpl @Inject constructor(private val dao: ContactsDao) : ContactsListRepository {

    override fun getAllContacts() = dao.getAllContacts()
    override fun getAllContactsByFavorite() = dao.getAllContactsByFavorite()
    override fun getAllContactsByFullName() = dao.getAllContactsByFullName()
    override fun getAllContactsDB() = dao.getAllContactsForNotificationsListener()

    override fun getAllAndroidIds() = dao.getAllAndroidIds()

    override fun getAllContactsVIP() = dao.getAllContactsVIP()
    override fun getNumbersOfContactsVip() = dao.getNumbersOfContactsVip()
    override fun getContactsVIPIds(): ArrayList<Int> {
        val arrayList = arrayListOf<Int>()
        arrayList.addAll(dao.getContactsVIPIds())
        return arrayList
    }

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)
    override suspend fun deleteContactById(id: Int) = dao.deleteContactById(id)
}