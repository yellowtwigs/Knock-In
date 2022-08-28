package com.yellowtwigs.knockin.domain.notifications

import android.telephony.PhoneNumberUtils
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import javax.inject.Inject

class GetContactByPhoneNumber @Inject constructor(private val contactsDao: ContactsDao) {

    operator fun invoke(phoneNumber: String): ContactDB? {
        val contacts = contactsDao.getAllContactsForNotificationsListener()
        var contactDB: ContactDB? = null

        for (contact in contacts) {
            if (PhoneNumberUtils.compare(contact.listOfPhoneNumbers[0], phoneNumber)) {
                contactDB = contact
                break
            }
        }

        return contactDB
    }
}