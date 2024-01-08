package com.yellowtwigs.knockin.domain.contact.list

import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.EquatableCallback
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
        val fullName = if (contact.firstName.isEmpty() || contact.firstName.isBlank() || contact.firstName == " ") {
            contact.lastName
        } else if (contact.lastName.isEmpty() || contact.lastName.isBlank() || contact.lastName == " ") {
            contact.firstName
        } else {
            "${contact.firstName} ${contact.firstName}"
        }

        return ContactsListViewState(
            id = contact.id,
            fullName = fullName,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64,
            firstPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true),
            secondPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, false),
            contact.listOfMails,
            contact.priority,
            contact.isFavorite == 1,
            contact.messengerId,
            contact.listOfMessagingApps.contains("com.whatsapp"),
            contact.listOfMessagingApps.contains("org.telegram.messenger"),
            contact.listOfMessagingApps.contains("org.thoughtcrime.securesms"),
            EquatableCallback {  }
        )
    }
}