package com.yellowtwigs.knockin.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) : ViewModel() {

    private val stringIdLiveData = MutableLiveData<Int>()

    fun setSearchTextChanged(text: String) {
        contactsListRepository.setSearchTextChanged(text)
    }

    fun setSortedBy(sortedBy: Int) {
        contactsListRepository.setSortedBy(sortedBy)
    }

    fun setFilterBy(filterBy: Int) {
        contactsListRepository.setFilterBy(filterBy)
    }

    fun setToolbarTitle(stringId: Int) {
        stringIdLiveData.postValue(stringId)
    }

    fun getToolbarTitle(): LiveData<Int> {
        return stringIdLiveData
    }
}