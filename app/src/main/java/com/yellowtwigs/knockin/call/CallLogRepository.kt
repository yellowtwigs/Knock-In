package com.yellowtwigs.knockin.call

import com.yellowtwigs.knockin.ui.call.CallLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepository @Inject constructor() {
    private val callLogEntries = mutableListOf<CallLogEntry>()
    private val callLogEntriesFlow = MutableStateFlow<List<CallLogEntry>>(callLogEntries)

    fun getCallLogEntries(): Flow<List<CallLogEntry>> = callLogEntriesFlow

    fun addCallLogEntry(callLogEntry: CallLogEntry) {
        callLogEntries.add(callLogEntry)
        callLogEntriesFlow.value = callLogEntries.toList()
    }
}