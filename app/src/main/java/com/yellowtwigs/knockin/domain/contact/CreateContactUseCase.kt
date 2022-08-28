package com.yellowtwigs.knockin.domain.contact

import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepository
import javax.inject.Inject

//private val createImageFromInitialsUseCase: CreateImageFromInitialsUseCase,
class CreateContactUseCase @Inject constructor(
    private val createContactRepository: CreateContactRepository,
) {

    suspend operator fun invoke(contactDB: ContactDB): Boolean {
        createContactRepository.insertContact(contactDB)

        return true
    }
}