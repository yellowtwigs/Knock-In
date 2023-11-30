package com.yellowtwigs.knockin.ui.notifications.settings

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(private val editContactRepository: EditContactRepository) :
    ViewModel() {

    fun updateContactPriority0To1() = viewModelScope.launch {
        editContactRepository.updateContactPriority0To1()
    }

    fun updateContactPriority1To0() = viewModelScope.launch {
        editContactRepository.updateContactPriority1To0()
    }

    fun disabledAllPhoneCallContacts(resolver: ContentResolver) = viewModelScope.launch {
        editContactRepository.disabledAllPhoneCallContacts(resolver)
    }

    fun enabledAllPhoneCallContacts(resolver: ContentResolver) = viewModelScope.launch {
        editContactRepository.enabledAllPhoneCallContacts(resolver)
    }
}