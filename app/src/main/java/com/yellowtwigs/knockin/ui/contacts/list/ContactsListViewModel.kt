package com.yellowtwigs.knockin.ui.contacts.list

import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.DeleteContactUseCase
import com.yellowtwigs.knockin.domain.contact.GetAllContactsUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class ContactsListViewModel @Inject constructor(
    private val getAllContactsUseCase: GetAllContactsUseCase,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val deleteContactUseCase: DeleteContactUseCase
) : ViewModel() {

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        return ContactsListViewState(
            id = contact.id,
            firstName = contact.firstName,
            lastName = contact.lastName,
            profilePicture = contact.profilePicture,
            profilePicture64 = contact.profilePicture64,
            firstPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true),
            secondPhoneNumber = transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, false),
            listOfMails = contact.listOfMails,
            priority = contact.priority,
            isFavorite = contact.isFavorite == 1,
            messengerId = contact.messengerId,
            hasWhatsapp = contact.listOfMessagingApps.contains("com.whatsapp"),
            hasTelegram = contact.listOfMessagingApps.contains("org.telegram.messenger"),
            hasSignal = contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
        )
    }

    private val filteringMutableStateFlow = MutableStateFlow(R.id.empty_filter)
    private val searchBarTextFlow = MutableStateFlow("")

    val contactsListViewStateLiveData = liveData(coroutineDispatcherProvider.io) {
        combine(
            getAllContactsUseCase.contactsListViewStateLiveData.asFlow(), filteringMutableStateFlow, searchBarTextFlow
        ) { contacts: List<ContactDB>, filterId: Int, input: String ->
            emit(filterWithInput(input, filterContactsList(filterId, contacts.map { transformContactDbToContactsListViewState(it) })))
        }.collect()
    }


//    init {
//        contactsListViewStateLiveData = Transformations.switchMap(sortedByLiveData) { sortBy ->
//            when (sortBy) {
//                R.id.sort_by_full_name -> {
//                    liveData(Dispatchers.IO) {
//                        getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
//                            emit(contacts.map {
//                                transformContactDbToContactsListViewState(it)
//                            })
//                        }
//                    }
//                }
//                R.id.sort_by_priority -> {
//                    liveData(Dispatchers.IO) {
//                        getAllContactsUseCase.invoke().collect { contacts ->
//                            emit(contacts.map {
//                                transformContactDbToContactsListViewState(it)
//                            })
//                        }
//                    }
//                }
//                R.id.sort_by_favorite -> {
//                    liveData(Dispatchers.IO) {
//                        getAllContactsSortByFavoriteUseCase.invoke().collect { contacts ->
//                            emit(contacts.map {
//                                transformContactDbToContactsListViewState(it)
//                            })
//                        }
//                    }
//                }
//                else -> {
//                    liveData(Dispatchers.IO) {
//                        getAllContactsUseCase.invoke().collect { contacts ->
//                            emit(contacts.map {
//                                transformContactDbToContactsListViewState(it)
//                            })
//                        }
//                    }
//                }
//            }
//        }
//
//        viewStateLiveData.addSource(contactsListViewStateLiveData) {
//            combine(it, searchBarTextLiveData.value, filterByLiveData.value)
//        }
//        viewStateLiveData.addSource(searchBarTextLiveData) {
//            combine(contactsListViewStateLiveData.value, it, filterByLiveData.value)
//        }
//        viewStateLiveData.addSource(filterByLiveData) {
//            combine(contactsListViewStateLiveData.value, searchBarTextLiveData.value, it)
//        }
//    }

//    private fun combine(
//        allContacts: List<ContactsListViewState>?, input: String?, filterBy: Int?
//    ) {
//        val listOfContacts = arrayListOf<ContactsListViewState>()
//
//        if (allContacts != null && allContacts.isNotEmpty() && input != null) {
//            if (input == "" || input == " " || input.isBlank()) {
//                if (filterBy != null) {
//                    listOfContacts.addAll(filterContactsList(filterBy, allContacts))
//                } else {
//                    listOfContacts.addAll(allContacts)
//                }
//            } else {
//                if (filterBy != null) {
//                    listOfContacts.addAll(
//                        filterContactsList(
//                            filterBy, filterWithInput(input, allContacts)
//                        )
//                    )
//                } else {
//                    listOfContacts.addAll(filterWithInput(input, allContacts))
//                }
//            }
//        }
//
//        viewStateLiveData.value = listOfContacts
//    }

    private fun filterWithInput(input: String, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        return listOfContacts.filter { contact ->
            val name = contact.firstName + " " + contact.lastName
            name.contains(input) || name.uppercase().contains(input.uppercase()) || name.lowercase().contains(input.lowercase())
        }
    }

    private fun filterContactsList(filterBy: Int, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        when (filterBy) {
            R.id.sms_filter -> {
                return listOfContacts.filter { it.firstPhoneNumber.phoneNumber.isNotBlank() && it.secondPhoneNumber.phoneNumber.isNotBlank() }
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
        searchBarTextFlow.value = text
    }

    fun setSortedBy(sortedBy: Int) {
        getAllContactsUseCase.setSortedBy(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        filteringMutableStateFlow.value = filterBy
    }

    suspend fun deleteContactsSelected(listOfContacts: List<Int>) {
        listOfContacts.map {
            deleteContactUseCase.deleteContactById(it)
        }
    }
}