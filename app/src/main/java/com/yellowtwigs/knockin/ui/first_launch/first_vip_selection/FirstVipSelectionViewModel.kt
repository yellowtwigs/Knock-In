package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import androidx.lifecycle.*
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstVipSelectionViewModel @Inject constructor(private val contactsListRepository: ContactsListRepository) :
    ViewModel() {

    private val viewStateLiveData: MediatorLiveData<List<FirstVipSelectionViewState>> =
        MediatorLiveData()

    init {
        val allContacts = contactsListRepository.getAllContacts()

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(contacts)
        }
    }

    private fun combine(allContacts: List<ContactDB>) {
        val listOfContacts = arrayListOf<FirstVipSelectionViewState>()

        if (allContacts.isNotEmpty()) {
            for (contact in allContacts) {
                contact.apply {
                    addContactInList(listOfContacts, contact)
                }
            }
        }

        viewStateLiveData.value = listOfContacts.sortedBy {
            it.firstName.uppercase().unAccent() + it.lastName.uppercase().unAccent()
        }
    }

    private fun addContactInList(
        contacts: ArrayList<FirstVipSelectionViewState>,
        contact: ContactDB
    ) {
        contacts.add(
            FirstVipSelectionViewState(
                contact.id,
                contact.firstName,
                contact.lastName,
                contact.profilePicture,
                contact.profilePicture64
            )
        )
    }

    fun getAllContacts(): LiveData<List<FirstVipSelectionViewState>> {
        return viewStateLiveData
    }

    suspend fun updateContact(ids: ArrayList<Int>) =
        viewModelScope.launch {
            for (id in ids) {
                contactsListRepository.updateContactPriorityById(id, 2)
            }
        }
}