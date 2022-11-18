package com.yellowtwigs.knockin.ui.contacts.list

import android.util.Log
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.*
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.toList
import java.nio.channels.Channel
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(
    private val getAllContactsUseCase: GetAllContactsUseCase,
    private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
    private val getAllContactsSortByFavoriteUseCase: GetAllContactsSortByFavoriteUseCase,
    private val getNumbersContactsVipUseCase: GetNumbersContactsVipUseCase,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    private var contactsListViewStateLiveData: LiveData<List<ContactsListViewState>>
    private var contactsListViewStateLiveDataSortByFullName: LiveData<List<ContactsListViewState>>
    private var contactsListViewStateLiveDataSortByFavorite: LiveData<List<ContactsListViewState>>

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

    val viewStateLiveData = MediatorLiveData<List<ContactsListViewState>>()

    private val searchBarTextLiveData = MutableLiveData("")
    private val sortedByLiveData = MutableLiveData(R.id.sort_by_priority)
    private val filterByLiveData = MutableLiveData(R.id.empty_filter)

    init {
        contactsListViewStateLiveData = liveData(Dispatchers.IO) {

            getAllContactsUseCase.invoke().collect { contacts ->
                emit(contacts.map {
                    transformContactDbToContactsListViewState(it)
                })
            }
        }
        contactsListViewStateLiveDataSortByFullName = liveData(Dispatchers.IO) {
            getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
                emit(contacts.map {
                    transformContactDbToContactsListViewState(it)
                })
            }
        }
        contactsListViewStateLiveDataSortByFavorite = liveData(Dispatchers.IO) {
            getAllContactsSortByFavoriteUseCase.invoke().collect { contacts ->
                emit(contacts.map {
                    transformContactDbToContactsListViewState(it)
                })
            }
        }

        viewStateLiveData.apply {
            addSource(contactsListViewStateLiveData) {
                combine(
                    it,
                    contactsListViewStateLiveDataSortByFullName.value,
                    contactsListViewStateLiveDataSortByFavorite.value,
                    searchBarTextLiveData.value,
                    sortedByLiveData.value,
                    filterByLiveData.value,
                )
            }
            addSource(contactsListViewStateLiveDataSortByFullName) {
                combine(
                    contactsListViewStateLiveData.value,
                    it,
                    contactsListViewStateLiveDataSortByFavorite.value,
                    searchBarTextLiveData.value,
                    sortedByLiveData.value,
                    filterByLiveData.value,
                )
            }
            addSource(contactsListViewStateLiveDataSortByFavorite) {
                combine(
                    contactsListViewStateLiveData.value,
                    contactsListViewStateLiveDataSortByFullName.value,
                    it,
                    searchBarTextLiveData.value,
                    sortedByLiveData.value,
                    filterByLiveData.value,
                )
            }
            addSource(searchBarTextLiveData) {
                combine(
                    contactsListViewStateLiveData.value,
                    contactsListViewStateLiveDataSortByFullName.value,
                    contactsListViewStateLiveDataSortByFavorite.value,
                    it,
                    sortedByLiveData.value,
                    filterByLiveData.value,
                )
            }
            addSource(sortedByLiveData) {
                combine(
                    contactsListViewStateLiveData.value,
                    contactsListViewStateLiveDataSortByFullName.value,
                    contactsListViewStateLiveDataSortByFavorite.value,
                    searchBarTextLiveData.value,
                    it,
                    filterByLiveData.value,
                )
            }
            addSource(filterByLiveData) {
                combine(
                    contactsListViewStateLiveData.value,
                    contactsListViewStateLiveDataSortByFullName.value,
                    contactsListViewStateLiveDataSortByFavorite.value,
                    searchBarTextLiveData.value,
                    sortedByLiveData.value,
                    it,
                )
            }
        }
    }

    private fun combine(
        allContacts: List<ContactsListViewState>?,
        allContactsSortByFullName: List<ContactsListViewState>?,
        allContactsSortByFavorite: List<ContactsListViewState>?,
        input: String?,
        sortedBy: Int?,
        filterBy: Int?
    ) {
        val listOfContacts = arrayListOf<ContactsListViewState>()

        if (sortedBy != null && input != null && filterBy != null) {
            when (sortedBy) {
                R.id.sort_by_full_name -> {
                    allContactsSortByFullName?.let {
                        if (input == "" || input == " " || input.isBlank()) {
                            listOfContacts.addAll(it)
                        } else {
                            listOfContacts.addAll(filterWithInput(input, it))
                        }
                    }
                }
                R.id.sort_by_priority -> {
                    allContacts?.let {
                        if (input == "" || input == " " || input.isBlank()) {
                            listOfContacts.addAll(it)
                        } else {
                            listOfContacts.addAll(filterWithInput(input, it))
                        }
                    }
                }
                R.id.sort_by_favorite -> {
                    allContactsSortByFavorite?.let {
                        if (input == "" || input == " " || input.isBlank()) {
                            listOfContacts.addAll(it)
                        } else {
                            listOfContacts.addAll(filterWithInput(input, it))
                        }
                    }
                }
            }

            filterContactsList(filterBy, listOfContacts)
        }

        viewStateLiveData.value = listOfContacts
    }

    private fun filterWithInput(
        input: String,
        listOfContacts: List<ContactsListViewState>
    ): List<ContactsListViewState> {
        return listOfContacts.filter { contact ->
            val name = contact.firstName + " " + contact.lastName
            name.contains(input) || name.uppercase().contains(input.uppercase()) ||
                    name.lowercase().contains(input.lowercase())
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

    fun setSearchTextChanged(text: String) {
        searchBarTextLiveData.value = text
    }

    fun setSortedBy(sortedBy: Int) {
        sortedByLiveData.value = sortedBy
    }

    fun setFilterBy(filterBy: Int) {
        filterByLiveData.value = filterBy
    }

    fun getNumbersContactsVip() = getNumbersContactsVipUseCase.getNumbersOfContactsVip()

    suspend fun deleteContactsSelected(listOfContacts: List<Int>) {
        listOfContacts.map {
            deleteContactUseCase.deleteContactById(it)
        }

    }
}