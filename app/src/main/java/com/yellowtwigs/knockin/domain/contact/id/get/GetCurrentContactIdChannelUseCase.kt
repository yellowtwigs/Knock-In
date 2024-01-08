package com.yellowtwigs.knockin.domain.contact.id.get

import com.yellowtwigs.knockin.repositories.contacts.id.CurrentContactIdRepository
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class GetCurrentContactIdChannelUseCase @Inject constructor(private val currentContactIdRepository: CurrentContactIdRepository) {

    fun invoke() = currentContactIdRepository.getCurrentContactIdChannel().receiveAsFlow()
}