package com.yellowtwigs.knockin.repositories.contacts.edit

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.ContactDB
import kotlinx.coroutines.flow.Flow

interface EditContactRepository {

    suspend fun updateContactPriority1To0()
    suspend fun updateContactPriority0To1()

    suspend fun disabledAllPhoneCallContacts(resolver: ContentResolver)
    suspend fun enabledAllPhoneCallContacts(resolver: ContentResolver)

    fun getContact(id: Int): Flow<ContactDB>

    suspend fun updateContact(contact: ContactDB)

    suspend fun addNewContact(contact: ContactDB): Long

    suspend fun updateContactPriorityById(id: Int, priority: Int)

    suspend fun deleteContactById(id: Int)
}