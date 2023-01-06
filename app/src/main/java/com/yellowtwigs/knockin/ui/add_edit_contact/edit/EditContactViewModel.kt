package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.domain.contact.CreateContactUseCase
import com.yellowtwigs.knockin.domain.group.UpdateFavoriteGroupUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    private val editContactRepository: EditContactRepository,
//    private val updateFavoriteGroupUseCase: UpdateFavoriteGroupUseCase,
    private val createContactUseCase: CreateContactUseCase
) : ViewModel() {

    fun getContactById(id: Int) = editContactRepository.getContact(id)

    suspend fun updateContact(contact: ContactDB) {
        editContactRepository.updateContact(contact)
    }

    fun checkDuplicateContact(contact: ContactDB): Boolean {
        return createContactUseCase.checkIfContactDuplicate(contact)
    }

    suspend fun addNewContact(contact: ContactDB) = editContactRepository.addNewContact(contact)

    suspend fun deleteContact(contact: ContactDB) = editContactRepository.deleteContact(contact)

    suspend fun updateFavorite(contactId: String) {
//        updateFavoriteGroupUseCase.updateFavoriteGroup(contactId)
    }
}