package com.yellowtwigs.knockin.ui.contacts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(contactsRepository: ContactsRepository) :
    ViewModel() {

    private val viewStateLiveData: MediatorLiveData<List<ContactsListViewState>> =
        MediatorLiveData()

    init {
        viewStateLiveData.addSource(
            contactsRepository.getAllContacts().asLiveData()
        ) { allContacts ->
            combine(allContacts)
        }
    }

    private fun combine(allContacts: List<ContactDB>) {
        val listOfContacts = arrayListOf<ContactsListViewState>()

        if (allContacts.isNotEmpty()) {
            for (contact in allContacts) {
                listOfContacts.add(
                    ContactsListViewState(
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
                        contact.listOfMessagingApps.contains("whatsapp"),
                        contact.listOfMessagingApps.contains("telegram"),
                        contact.listOfMessagingApps.contains("signal")
                    )
                )
            }
        }

        viewStateLiveData.value = listOfContacts
    }

    public fun getAllContacts(): LiveData<List<ContactsListViewState>> {
        return viewStateLiveData
    }
}