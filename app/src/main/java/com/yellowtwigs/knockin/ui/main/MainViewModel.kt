package com.yellowtwigs.knockin.ui.main

import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) : ViewModel() {

    fun setSearchText(text: String) {
        contactsListRepository.setSearchBarText(text)
    }

    fun setSortedBy(sortedBy: Int) {
        contactsListRepository.setSortedBy(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        contactsListRepository.setFilterBy(filterBy)
    }
}