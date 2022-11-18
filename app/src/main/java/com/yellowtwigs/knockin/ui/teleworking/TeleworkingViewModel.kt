package com.yellowtwigs.knockin.ui.teleworking

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.domain.contact.GetAllContactsUseCase
import com.yellowtwigs.knockin.domain.contact.GetAllContactsVipUseCase
import com.yellowtwigs.knockin.domain.contact.UpdateContactPriorityByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeleworkingViewModel @Inject constructor(
    getAllContactsVipUseCase: GetAllContactsVipUseCase,
    private val updateContactPriorityByIdUseCase: UpdateContactPriorityByIdUseCase
) : ViewModel() {

    val liveData = getAllContactsVipUseCase.contactsListViewStateLiveData

    fun updateContactsPriority(priority: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            updateContactPriorityByIdUseCase.updateAllContactsPriority(priority)
        }
    }
}