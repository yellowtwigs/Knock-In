package com.yellowtwigs.knockin.repositories.contacts.edit

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class EditContactRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    EditContactRepository {

    override suspend fun updateContactPriority1To0() = dao.updateContactPriority1To0()
    override suspend fun updateContactPriority0To1() = dao.updateContactPriority0To1()
    override suspend fun disabledAllPhoneCallContacts(resolver: ContentResolver) {
        dao.getAllContacts().collect {
            it.forEach { contact ->
                val contentValues = ContentValues()
                contentValues.put(ContactsContract.Contacts.SEND_TO_VOICEMAIL, 1)

                val where = ContactsContract.Contacts._ID + " = ?"
                val whereArgs = arrayOf(contact.androidId?.toString())

                resolver.update(ContactsContract.Contacts.CONTENT_URI, contentValues, where, whereArgs)
            }
        }
    }
    override suspend fun enabledAllPhoneCallContacts(resolver: ContentResolver) {
        dao.getAllContacts().collect {
            it.forEach { contact ->
                val contentValues = ContentValues()
                contentValues.put(ContactsContract.Contacts.SEND_TO_VOICEMAIL, 0)

                val where = ContactsContract.Contacts._ID + " = ?"
                val whereArgs = arrayOf(contact.androidId?.toString())

                resolver.update(ContactsContract.Contacts.CONTENT_URI, contentValues, where, whereArgs)
            }
        }
    }

    override fun getContact(id: Int) = dao.getContact(id).asLiveData()

    override suspend fun addNewContact(contact: ContactDB) = dao.insertContact(contact)

    override suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContactById(id: Int) = dao.deleteContactById(id)
}