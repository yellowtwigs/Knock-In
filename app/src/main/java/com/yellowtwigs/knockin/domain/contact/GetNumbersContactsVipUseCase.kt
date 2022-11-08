package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import javax.inject.Inject

class GetNumbersContactsVipUseCase @Inject constructor(private val contactsListRepository: ContactsListRepository) {

    fun getNumbersOfContactsVip() = contactsListRepository.getNumbersOfContactsVip()
}