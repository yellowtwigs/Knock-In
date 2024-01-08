package com.yellowtwigs.knockin.repositories.contacts.id

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentContactIdRepositoryImpl @Inject constructor() : CurrentContactIdRepository {
    private val currentContactIdFlow = MutableStateFlow<Int?>(null)
    private val currentContactIdChannel = Channel<Int?>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getCurrentContactIdFlow(): StateFlow<Int?> {
        return currentContactIdFlow
    }

    override fun getCurrentContactIdChannel(): Channel<Int?> {
        return currentContactIdChannel
    }

    override fun setCurrentContactIdFlow(id: Int?) {
        currentContactIdFlow.value = id
        currentContactIdChannel.trySend(id)
    }
}