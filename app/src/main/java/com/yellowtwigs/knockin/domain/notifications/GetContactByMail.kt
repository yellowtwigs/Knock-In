package com.yellowtwigs.knockin.domain.notifications

import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class GetContactByMail @Inject constructor(private val contactsDao: ContactsDao) {

    operator fun invoke(mail: String): ContactDB? {
        val contacts = contactsDao.getAllContactsForNotificationsListener()
        var contactDB: ContactDB? = null

        for (contact in contacts) {
            if (contact.listOfMails[0] == mail) {
                contactDB = contact
                break
            }
        }

        return contactDB
    }
}