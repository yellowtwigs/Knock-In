package com.yellowtwigs.knockin.domain.contact.get_number

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNumberOfContactsUseCase @Inject constructor(private val contactsListRepository: ContactsListRepository) {

    fun invoke(): NumberOfContacts{
        return NumberOfContacts(
            contactsListRepository.getNumbersOfContactsVip(),
            contactsListRepository.getNumbersOfContactsStandard(),
            contactsListRepository.getNumbersOfContactsSilent(),
        )
    }
}