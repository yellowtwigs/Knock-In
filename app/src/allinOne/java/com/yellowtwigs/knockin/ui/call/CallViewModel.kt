package com.yellowtwigs.knockin.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.call.CallLogRepository
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    val callLogRepository: CallLogRepository, val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val callLogsLiveData: LiveData<List<CallLogEntry>> = callLogRepository.getCallLogEntries().asLiveData(coroutineDispatcherProvider.io)
}