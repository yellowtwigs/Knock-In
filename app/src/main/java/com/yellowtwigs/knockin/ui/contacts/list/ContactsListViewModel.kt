package com.yellowtwigs.knockin.ui.contacts.list

import android.util.Log
import androidx.lifecycle.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.contact.DeleteContactUseCase
import com.yellowtwigs.knockin.domain.contact.GetAllContactsUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.transformContactDbToContactsListViewState
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

    private val filteringMutableStateFlow = MutableStateFlow(R.id.empty_filter)
    private val sortingMutableStateFlow = MutableStateFlow(R.id.sort_by_priority)
    private val searchBarTextFlow = MutableStateFlow("")

    val contactsListViewStateLiveData = liveData(coroutineDispatcherProvider.io) {
        combine(
            getAllContactsUseCase.contactsListViewStateLiveData.asFlow(),
            sortingMutableStateFlow,
            filteringMutableStateFlow,
            searchBarTextFlow
        ) { contacts: List<ContactDB>, sort: Int, filterId: Int, input: String ->
            emit(filterWithInput(input, sortContactsList(sort, filterId, contacts.map { transformContactDbToContactsListViewState(it) })))
        }.collect()
    }


    private fun filterWithInput(input: String, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        return listOfContacts.filter { contact ->
            val name = contact.firstName + " " + contact.lastName
            name.contains(input) || name.uppercase().contains(input.uppercase()) || name.lowercase().contains(input.lowercase())
        }
    }

    private fun sortContactsList(sortedBy: Int, filterBy: Int, listOfContacts: List<ContactsListViewState>): List<ContactsListViewState> {
        return when (sortedBy) {
            R.id.sort_by_full_name -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName })
            }
            R.id.sort_by_priority -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName }.sortedByDescending { it.priority })
            }
            R.id.sort_by_favorite -> {
                filterContactsList(filterBy, listOfContacts.sortedBy { it.fullName }.sortedByDescending { it.isFavorite })
            }
            else -> {
                filterContactsList(filterBy, listOfContacts.sortedByDescending { it.priority }.sortedBy { it.fullName })
            }
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
        searchBarTextFlow.tryEmit(text)
    }

    fun setSortedBy(sortedBy: Int) {
        sortingMutableStateFlow.tryEmit(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        filteringMutableStateFlow.tryEmit(filterBy)
    }

    suspend fun deleteContactsSelected(listOfContacts: List<Int>) {
        listOfContacts.map {
            deleteContactUseCase.deleteContactById(it)
        }
    }
}