package com.yellowtwigs.knockin.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.models.data.Contact
import com.yellowtwigs.knockin.repositories.contacts.DefaultContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(private val repository: DefaultContactRepository) :
    ViewModel() {

    val getAllContacts = repository.getAllContacts().asLiveData()

    fun sortContactByFirstNameAZ() = repository.sortContactByFirstNameAZ()
    fun sortContactByLastNameAZ() = repository.sortContactByFirstNameAZ()
    fun sortContactByPriority20() = repository.sortContactByPriority20()
    fun sortContactByFavorite() = repository.sortContactByFavorite()

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        repository.deleteContact(contact)
    }

    fun deleteAll(contacts: List<Contact>) = viewModelScope.launch {
        repository.deleteAll(contacts)
    }
}