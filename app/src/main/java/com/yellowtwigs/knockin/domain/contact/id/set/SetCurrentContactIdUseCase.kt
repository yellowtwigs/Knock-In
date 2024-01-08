package com.yellowtwigs.knockin.domain.contact.id.set

import com.yellowtwigs.knockin.repositories.contacts.id.CurrentContactIdRepository
import com.yellowtwigs.knockin.repositories.contacts.id.NavigateDestination
import javax.inject.Inject

class SetCurrentContactIdUseCase @Inject constructor(private val currentContactIdRepository: CurrentContactIdRepository) {

    fun invoke(id: Int?) {
        currentContactIdRepository.setCurrentContactIdFlow(id)
    }
}