package com.yellowtwigs.knockin.ui.edit_contact

import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(private val editContactRepository: EditContactRepository) :
    ViewModel() {

    fun getContactById(id: Int) = editContactRepository.getContact(id)

    suspend fun updateContact(contact: ContactDB) = editContactRepository.updateContact(contact)

    suspend fun deleteContact(contact: ContactDB) = editContactRepository.deleteContact(contact)
}