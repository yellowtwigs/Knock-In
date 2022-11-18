package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdateContactPriorityByIdUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    suspend fun updateContactPriorityById(id: Int, priority: Int) {
        contactsListRepository.updateContactPriorityById(id, priority)
    }

    suspend fun updateAllContactsPriority(priority: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            for (contact in contactsListRepository.getAllContactsDB()) {
                if (contact.priority != 2) {
                    contactsListRepository.updateContactPriorityById(contact.id, priority)
                }
            }
        }
    }
}