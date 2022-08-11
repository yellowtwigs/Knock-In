package com.yellowtwigs.knockin.ui.contacts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val viewStateLiveData: MediatorLiveData<List<ContactListViewState>> = MediatorLiveData()

    init {
        viewStateLiveData.addSource(
            contactsRepository.getAllContacts().asLiveData()
        ) { allContacts ->
            combine(allContacts)
        }
    }

    private fun combine(allContacts: List<ContactDB>) {
        val listOfContacts = arrayListOf<ContactListViewState>()

        if (allContacts.isNotEmpty()) {
            for (contact in allContacts) {
                listOfContacts.add(
                    ContactListViewState(
                        contact.id,
                        contact.firstName,
                        contact.lastName,
                        contact.profilePicture,
                        contact.profilePicture64,
                        contact.listOfPhoneNumbers,
                        contact.listOfMails,
                        contact.contactPriority,
                        contact.favorite == 1,
                        contact.messengerId,
                        contact.hasWhatsapp == 1,
                        contact.hasTelegram == 1,
                        contact.hasSignal == 1
                    )
                )
            }
        }

        viewStateLiveData.value = listOfContacts
    }

    public fun getAllContacts(): LiveData<List<ContactListViewState>> {
        return viewStateLiveData
    }
}