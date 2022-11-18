package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.GetAllContactsUseCase
import com.yellowtwigs.knockin.domain.contact.UpdateContactPriorityByIdUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstVipSelectionViewModel @Inject constructor(
    getAllContactsUseCase: GetAllContactsUseCase,
    private val updateContactPriorityByIdUseCase: UpdateContactPriorityByIdUseCase
) :
    ViewModel() {

    private val viewStateLiveData: MediatorLiveData<List<FirstVipSelectionViewState>> =
        MediatorLiveData()

    init {
        val allContacts = getAllContactsUseCase.firstVipSelectionViewStateLiveData

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(contacts)
        }
    }

    private fun combine(allContacts: List<FirstVipSelectionViewState>) {
        if (allContacts.isNotEmpty()) {
            viewStateLiveData.postValue(
                allContacts.sortedBy {
                    it.firstName.uppercase().unAccent() + it.lastName.uppercase().unAccent()
                }
            )
        }
    }

    fun getAllContacts(): LiveData<List<FirstVipSelectionViewState>> {
        return viewStateLiveData
    }

    fun updateContact(ids: ArrayList<Int>) =
        viewModelScope.launch {
            for (id in ids) {
                updateContactPriorityByIdUseCase.updateContactPriorityById(id, 2)
            }
        }
}