package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import javax.inject.Inject

class UpdateContactPriorityByIdUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    suspend fun updateContactPriorityById(id: Int, priority: Int){
        contactsListRepository.updateContactPriorityById(id, priority)
    }
}