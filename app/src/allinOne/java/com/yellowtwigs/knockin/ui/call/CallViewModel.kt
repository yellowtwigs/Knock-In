package com.yellowtwigs.knockin.ui.call

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.call.CallLogRepository
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    val callLogRepository: CallLogRepository, val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    val callLogsLiveData: LiveData<List<CallLogEntry>> = callLogRepository.getCallLogEntries().asLiveData(coroutineDispatcherProvider.io)
}