package com.yellowtwigs.knockin.ui.contacts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(contactsListRepository: ContactsListRepository) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<ContactsListViewState>>()

    private val searchBarTextLiveData = MutableLiveData<String>()
    private val sortedByLiveData = MutableLiveData<Int>()
    private val filterByLiveData = MutableLiveData<Int>()

    init {
        val allContacts = contactsListRepository.getAllContacts()

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(
                contacts,
                searchBarTextLiveData.value,
                sortedByLiveData.value,
                filterByLiveData.value
            )
        }

        viewStateLiveData.addSource(searchBarTextLiveData) { input ->
            combine(allContacts.value, input, sortedByLiveData.value, filterByLiveData.value)
        }

        viewStateLiveData.addSource(sortedByLiveData) {
            combine(allContacts.value, searchBarTextLiveData.value, it, filterByLiveData.value)
        }

        viewStateLiveData.addSource(filterByLiveData) {
            combine(allContacts.value, searchBarTextLiveData.value, sortedByLiveData.value, it)
        }
    }

    private fun combine(
        allContacts: List<ContactDB>?,
        input: String?,
        sortedBy: Int?,
        filterBy: Int?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfContacts = arrayListOf<ContactsListViewState>()

            if (allContacts?.isNotEmpty() == true) {
                for (contact in allContacts) {
                    addContactInList(listOfContacts, contact)
                }

                withContext(Dispatchers.Main) {
                    viewStateLiveData.value = sortedContactsList(
                        sortedBy,
                        filterBy,
                        input,
                        listOfContacts
                    )
                }
            }
        }
    }

    private fun sortedContactsList(
        sortedBy: Int?,
        filterBy: Int?,
        input: String?,
        listOfContacts: ArrayList<ContactsListViewState>
    ): List<ContactsListViewState> {
        if (sortedBy != null) {
            when (sortedBy) {
                R.id.sort_by_first_name -> {
                    return filterWithInput(filterBy, input, listOfContacts).sortedBy {
                        if (it.firstName == "" || it.firstName == " " || it.firstName.isBlank() || it.firstName.isEmpty()) {
                            it.lastName
                        } else {
                            it.firstName
                        }
                    }
                }
                R.id.sort_by_last_name -> {
                    return filterWithInput(filterBy, input, listOfContacts).sortedBy {
                        if (it.lastName == "" || it.lastName == " " || it.lastName.isBlank() || it.lastName.isEmpty()) {
                            it.firstName
                        } else {
                            it.lastName
                        }
                    }
                }
                R.id.sort_by_priority -> {
                    return filterWithInput(filterBy, input, listOfContacts)
                        .sortedBy {
                            it.firstName.uppercase().unAccent() + it.lastName.uppercase()
                                .unAccent()
                        }.sortedByDescending { it.priority }
                }
                R.id.sort_by_favorite -> {
                    return filterWithInput(
                        filterBy,
                        input,
                        listOfContacts
                    ).sortedBy {
                        it.firstName.uppercase().unAccent() + it.lastName.uppercase()
                            .unAccent()
                    }.sortedByDescending { it.isFavorite }

                }
                else -> {
                    return filterWithInput(filterBy, input, listOfContacts).sortedBy {
                        it.firstName.uppercase().unAccent() + it.lastName.uppercase().unAccent()
                    }
                }
            }
        } else {
            return filterWithInput(filterBy, input, listOfContacts).sortedBy {
                it.firstName.uppercase().unAccent() + it.lastName.uppercase().unAccent()
            }
        }
    }

    private fun filterWithInput(
        filterBy: Int?,
        input: String?,
        listOfContacts: ArrayList<ContactsListViewState>
    ): List<ContactsListViewState> {
        return if (input != null) {
            filterContactsList(filterBy, listOfContacts.filter { contact ->
                val name = contact.firstName + " " + contact.lastName
                name.contains(input) || name.uppercase().contains(input.uppercase()) ||
                        name.lowercase().contains(input.lowercase())
            })
        } else {
            filterContactsList(filterBy, listOfContacts)
        }
    }

    private fun filterContactsList(
        filterBy: Int?,
        listOfContacts: List<ContactsListViewState>
    ): List<ContactsListViewState> {
        if (filterBy != null) {
            when (filterBy) {
                R.id.sms_filter -> {
                    return listOfContacts.filter { it.listOfPhoneNumbers.isNotEmpty() && it.listOfPhoneNumbers[0].isNotEmpty() }
                }
                R.id.mail_filter -> {
                    return listOfContacts.filter { it.listOfMails.isNotEmpty() && it.listOfMails[0].isNotEmpty() }
                }
                R.id.whatsapp_filter -> {
                    return listOfContacts.filter { it.hasWhatsapp }
                }
                R.id.messenger_filter -> {
                    return listOfContacts.filter {
                        it.messengerId != "" && it.messengerId.isNotBlank() && it.messengerId.isNotEmpty()
                    }
                }
                R.id.signal_filter -> {
                    return listOfContacts.filter { it.hasSignal }
                }
                R.id.telegram_filter -> {
                    return listOfContacts.filter { it.hasTelegram }
                }
                else -> {
                    return listOfContacts
                }
            }
        } else {
            return listOfContacts
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
                contact.listOfMessagingApps.contains("com.whatsapp"),
                contact.listOfMessagingApps.contains("org.telegram.messenger"),
                contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
            )
        )
    }

    fun getAllContacts(): LiveData<List<ContactsListViewState>> {
        return viewStateLiveData
    }

    fun setSearchTextChanged(text: String) {
        searchBarTextLiveData.value = text
    }

    fun setSortedBy(sortedBy: Int) {
        sortedByLiveData.value = sortedBy
    }

    fun setFilterBy(filterBy: Int) {
        filterByLiveData.value = filterBy
    }
}