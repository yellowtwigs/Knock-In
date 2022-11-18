package com.yellowtwigs.knockin.domain.contact

import android.util.Log
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepository
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateContactUseCase @Inject constructor(
    private val createContactRepository: CreateContactRepository,
    private val contactsListRepository: ContactsListRepository
) {

    suspend operator fun invoke(contactDB: ContactDB): Boolean {
        createContactRepository.insertContact(contactDB)
        return true
    }

    fun checkIfContactDuplicate(contactDB: ContactDB): Boolean {
        var isDuplicate = false
        for (contact in contactsListRepository.getAllContactsDB()) {
            if (contact.firstName.trim() == contactDB.firstName.trim() && contact.lastName.trim() == contactDB.lastName.trim()) {
                isDuplicate = true
                break
            }
        }
        return isDuplicate
    }
}