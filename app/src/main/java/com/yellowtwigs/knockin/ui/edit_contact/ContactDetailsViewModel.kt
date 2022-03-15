package com.yellowtwigs.knockin.ui.edit_contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import com.yellowtwigs.knockin.repositories.contacts.ContactDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDetailsViewModel @Inject constructor(private val repository: ContactDetailsRepository) :
    ViewModel() {

    fun getPhoneNumberById(id: Int) = repository.getPhoneNumberById(id).asLiveData()
    fun getMailById(id: Int) = repository.getMailById(id).asLiveData()
    fun getAllProperties() = repository.getAllProperties().asLiveData()
    fun getDetailsForAContact(contactID: Int) =
        repository.getDetailsForAContact(contactID).asLiveData()

    fun updateContactDetail(contactDetailDB: ContactDetailDB) = viewModelScope.launch {
        repository.updateContactDetail(contactDetailDB)
    }

    fun insert(contactDetailDB: ContactDetailDB) = viewModelScope.launch {
        repository.insert(contactDetailDB)
    }

    fun deleteDetail(contactDetailDB: ContactDetailDB) = viewModelScope.launch {
        repository.deleteDetail(contactDetailDB)
    }

    fun getDetailsById(id: Int, contactNumberAndPic: List<Map<Int, Any>>): List<ContactDetailDB> {
        val contactDetails = arrayListOf<ContactDetailDB>()
        var fieldPosition = 0
        contactNumberAndPic.forEach {
            if (it[1] == id) {
                contactDetails.add(
                    ContactDetailDB(
                        null,
                        null,
                        it[2].toString(),
                        it[5].toString(),
                        it[3].toString(),
                        fieldPosition
                    )
                )
                fieldPosition++
            }
        }
        return contactDetails
    }
}