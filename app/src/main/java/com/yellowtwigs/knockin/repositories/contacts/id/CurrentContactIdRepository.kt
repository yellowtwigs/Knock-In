package com.yellowtwigs.knockin.repositories.contacts.id

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

interface CurrentContactIdRepository {

    fun getCurrentContactIdFlow(): StateFlow<Int?>
    fun getCurrentContactIdChannel(): Channel<Int?>
    fun setCurrentContactIdFlow(id: Int?)
}