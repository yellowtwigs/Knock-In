package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import javax.inject.Inject

class GetContactByIdFlowUseCase @Inject constructor(private val editContactRepository: EditContactRepository) {

    fun invoke(id: Int) = editContactRepository.getContact(id)
}