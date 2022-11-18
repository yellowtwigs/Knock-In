package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import javax.inject.Inject

class GetAllContactsSortByFullNameUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    fun invoke() = contactsListRepository.getAllContactsByFullName()
}