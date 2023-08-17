package com.yellowtwigs.knockin.repositories.groups.manage

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentGroupIdRepository @Inject constructor() {

    private val currentGroupIdFlow = MutableStateFlow<Int?>(null)
    private val currentGroupIdChannel = Channel<Int?>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getCurrentGroupIdFlow(): StateFlow<Int?> = currentGroupIdFlow
    fun getCurrentGroupIdChannel(): Channel<Int?> = currentGroupIdChannel

    fun setCurrentGroupIdFlow(id: Int?) {
        currentGroupIdFlow.value = id
        currentGroupIdChannel.trySend(id)
    }
}