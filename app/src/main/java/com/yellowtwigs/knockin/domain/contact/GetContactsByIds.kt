package com.yellowtwigs.knockin.domain.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import kotlinx.coroutines.*
import javax.inject.Inject

class GetContactsByIds @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    fun getContactsByIds(ids: List<Int>) = liveData(Dispatchers.IO) {
        contactsListRepository.getAllContacts().collect { contacts ->
            val results = mutableListOf<ContactsListViewState>()
            coroutineScope {
                contacts.map {
                    if (ids.contains(it.id)) {
                        results.add(transformContactDbToContactsListViewState(it))
                    }
                }
            }

            emit(results)
        }
    }

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        return ContactsListViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64,
            contact.listOfPhoneNumbers,
            contact.listOfMails,
            contact.priority,
            contact.isFavorite == 1,
            contact.messengerId,
            contact.listOfMessagingApps.contains("com.whatsapp"),
            contact.listOfMessagingApps.contains("org.telegram.messenger"),
            contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
        )
    }
}