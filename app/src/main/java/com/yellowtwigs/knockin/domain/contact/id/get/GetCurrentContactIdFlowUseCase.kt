package com.yellowtwigs.knockin.domain.contact.id.get

import com.yellowtwigs.knockin.repositories.contacts.id.CurrentContactIdRepository
import javax.inject.Inject

class GetCurrentContactIdFlowUseCase @Inject constructor(private val currentContactIdRepository: CurrentContactIdRepository) {

    fun invoke() = currentContactIdRepository.getCurrentContactIdFlow()
}