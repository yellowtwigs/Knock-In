package com.yellowtwigs.knockin.ui.contacts

import android.content.Context
import androidx.lifecycle.*
import com.yellowtwigs.knockin.models.data.Contact
import com.yellowtwigs.knockin.repositories.contacts.DefaultContactRepository
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.openMail
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.ContactGesture.sendSms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(private val repository: DefaultContactRepository) :
    ViewModel() {

    val getAllContacts = repository.getAllContacts().asLiveData()
    fun getContact(id: Int): Contact? {
        var currentContact: Contact? = null
        Transformations.map(repository.getContact(id).asLiveData()) { contact ->
            currentContact = contact
        }
        return currentContact
    }

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



    fun onSendSmsClick(id: Int, context: Context) {
        getContact(id)?.phoneNumber?.let { sendSms(it, context) }
    }

    fun onPhoneCallClick(id: Int, context: Context) {
        getContact(id)?.phoneNumber?.let { callPhone(it, context) }
    }

    fun onOpenWhatsappClick(id: Int, context: Context) {
        getContact(id)?.phoneNumber?.let { openWhatsapp(it, context) }
    }

    fun onOpenMailClick(id: Int, context: Context) {
        getContact(id)?.mail?.let { openMail(it, context) }
    }

    var contactLiveData = MutableLiveData<Contact>()
    fun setContactLiveData(id: Int) {
        if (getContact(id) != null) {
            contactLiveData.postValue(getContact(id)!!)
        }
    }
}