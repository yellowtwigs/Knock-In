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
import kotlinx.coroutines.flow.collect
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

    val viewStateLiveData = MediatorLiveData<List<ContactsListViewState>>()

    private var contactsListViewStateLiveData: LiveData<List<ContactsListViewState>>

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        return ContactsListViewState(
            id = contact.id,
            firstName = contact.firstName,
            lastName = contact.lastName,
            profilePicture = contact.profilePicture,
            profilePicture64 = contact.profilePicture64,
            listOfPhoneNumbers = contact.listOfPhoneNumbers,
            listOfMails = contact.listOfMails,
            priority = contact.priority,
            isFavorite = contact.isFavorite == 1,
            messengerId = contact.messengerId,
            hasWhatsapp = contact.listOfMessagingApps.contains("com.whatsapp"),
            hasTelegram = contact.listOfMessagingApps.contains("org.telegram.messenger"),
            hasSignal = contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
        )
    }

    private val searchBarTextLiveData = MutableLiveData("")
    private val sortedByLiveData = MutableLiveData(R.id.sort_by_priority)
    private val filterByLiveData = MutableLiveData<Int>()

    init {
        contactsListViewStateLiveData = Transformations.switchMap(sortedByLiveData) { sortBy ->
            when (sortBy) {
                R.id.sort_by_full_name -> {
                    liveData(Dispatchers.IO) {
                        getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
                            emit(contacts.map {
                                transformContactDbToContactsListViewState(it)
                            })
                        }
                    }
                }
                R.id.sort_by_priority -> {
                    liveData(Dispatchers.IO) {
                        getAllContactsUseCase.invoke().collect { contacts ->
                            emit(contacts.map {
                                transformContactDbToContactsListViewState(it)
                            })
                        }
                    }
                }
                R.id.sort_by_favorite -> {
                    liveData(Dispatchers.IO) {
                        getAllContactsSortByFavoriteUseCase.invoke().collect { contacts ->
                            emit(contacts.map {
                                transformContactDbToContactsListViewState(it)
                            })
                        }
                    }
                }
                else -> {
                    liveData(Dispatchers.IO) {
                        getAllContactsUseCase.invoke().collect { contacts ->
                            emit(contacts.map {
                                transformContactDbToContactsListViewState(it)
                            })
                        }
                    }
                }
            }
        }

        viewStateLiveData.addSource(contactsListViewStateLiveData) {
            combine(
                it,
                searchBarTextLiveData.value,
                filterByLiveData.value,
            )
        }
        viewStateLiveData.addSource(searchBarTextLiveData) {
            combine(
                contactsListViewStateLiveData.value,
                it,
                filterByLiveData.value,
            )
        }
        viewStateLiveData.addSource(filterByLiveData) {
            combine(
                contactsListViewStateLiveData.value,
                searchBarTextLiveData.value,
                it,
            )
        }
    }

    private fun combine(
        allContacts: List<ContactsListViewState>?, input: String?, filterBy: Int?
    ) {
        val listOfContacts = arrayListOf<ContactsListViewState>()

        if (allContacts != null && allContacts.isNotEmpty() && input != null) {
            if (input == "" || input == " " || input.isBlank()) {
                if (filterBy != null) {
                    listOfContacts.addAll(filterContactsList(filterBy, allContacts))
                } else {
                    listOfContacts.addAll(allContacts)
                }
            } else {
                if (filterBy != null) {
                    listOfContacts.addAll(
                        filterContactsList(
                            filterBy, filterWithInput(input, allContacts)
                        )
                    )
                } else {
                    listOfContacts.addAll(filterWithInput(input, allContacts))
                }
            }
        }

        viewStateLiveData.value = listOfContacts
    }

    private fun filterWithInput(
        input: String, listOfContacts: List<ContactsListViewState>
    ): List<ContactsListViewState> {
        return listOfContacts.filter { contact ->
            val name = contact.firstName + " " + contact.lastName
            name.contains(input) || name.uppercase().contains(input.uppercase()) || name.lowercase().contains(input.lowercase())
        }
    }

    private fun filterContactsList(
        filterBy: Int, listOfContacts: List<ContactsListViewState>
    ): List<ContactsListViewState> {
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