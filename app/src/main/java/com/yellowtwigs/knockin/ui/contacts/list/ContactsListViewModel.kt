package com.yellowtwigs.knockin.ui.contacts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(contactsListRepository: ContactsListRepository) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<ContactsListViewState>>()

    init {
        val allContacts = contactsListRepository.getAllContacts()
        val searchBarText = contactsListRepository.getSearchBarText()
        val sortedBy = contactsListRepository.getSortedBy()
        val filterBy = contactsListRepository.getFilterBy()

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(contacts, searchBarText.value, sortedBy.value, filterBy.value)
        }

        viewStateLiveData.addSource(searchBarText) { input ->
            combine(allContacts.value, input, sortedBy.value, filterBy.value)
        }

        viewStateLiveData.addSource(sortedBy) {
            combine(allContacts.value, searchBarText.value, it, filterBy.value)
        }

        viewStateLiveData.addSource(filterBy) {
            combine(allContacts.value, searchBarText.value, sortedBy.value, it)
        }
    }

    private fun combine(
        allContacts: List<ContactDB>?,
        input: String?,
        sortedBy: Int?,
        filterBy: Int?
    ) {
        val listOfContacts = arrayListOf<ContactsListViewState>()

        if (allContacts?.isNotEmpty() == true) {
            for (contact in allContacts) {
                contact.apply {
                    if (input != null && input != "") {
                        if (firstName.contains(input) || lastName.contains(input) ||
                            firstName.contains(input.uppercase()) || lastName.contains(input.uppercase()) ||
                            firstName.contains(input.lowercase()) || lastName.contains(input.lowercase())
                        ) {
                            addContactInList(listOfContacts, contact)
                        }
                    } else {
                        addContactInList(listOfContacts, contact)
                    }

                    if (sortedBy != null) {
                        when (sortedBy) {
                            R.id.sort_by_first_name -> {
                                if (firstName == "" || firstName.isBlank() || firstName.isEmpty()) {
                                    listOfContacts.sortedBy { it.lastName }
                                } else {
                                    listOfContacts.sortedBy { it.firstName }
                                }
                            }
                            R.id.sort_by_last_name -> {
                                if (lastName == "" || lastName.isBlank() || lastName.isEmpty()) {
                                    listOfContacts.sortedBy { it.firstName }
                                } else {
                                    listOfContacts.sortedBy { it.lastName }
                                }
                            }
                            R.id.sort_by_priority -> {
                                listOfContacts.sortedByDescending { it.priority }
                            }
                            R.id.sort_by_favorite -> {
                                listOfContacts.sortedByDescending { it.isFavorite }
                            }
                        }
                    }

                    if (filterBy != null) {
                        when (filterBy) {
                            R.id.sms_filter -> {
                                listOfContacts.filter { it.listOfPhoneNumbers.isNotEmpty() }
                            }
                            R.id.mail_filter -> {
                                listOfContacts.filter { it.listOfMails.isNotEmpty() }
                            }
                            R.id.whatsapp_filter -> {
                                listOfContacts.filter { it.hasWhatsapp }
                            }
                            R.id.messenger_filter -> {
                                listOfContacts.filter {
                                    it.messengerId != "" && it.messengerId.isNotBlank() && it.messengerId.isNotEmpty()
                                }
                            }
                            R.id.signal_filter -> {
                                listOfContacts.filter { it.hasSignal }
                            }
                            R.id.telegram_filter -> {
                                listOfContacts.filter { it.hasTelegram }
                            }
                        }
                    }
                }
            }
        }

        viewStateLiveData.value = listOfContacts.sortedBy {
            it.firstName.uppercase().unAccent() + it.lastName.uppercase().unAccent()
        }
    }

    private fun addContactInList(contacts: ArrayList<ContactsListViewState>, contact: ContactDB) {
        contacts.add(
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

    fun getAllContacts(): LiveData<List<ContactsListViewState>> {
        return viewStateLiveData
    }
}