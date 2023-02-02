package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.GetAllContactsSortByFullNameUseCase
import com.yellowtwigs.knockin.domain.contact.GetAllContactsUseCase
import com.yellowtwigs.knockin.domain.contact.GetNumbersContactsVipUseCase
import com.yellowtwigs.knockin.domain.contact.UpdateContactPriorityByIdUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstVipSelectionViewModel @Inject constructor(
    private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
    private val updateContactPriorityByIdUseCase: UpdateContactPriorityByIdUseCase,
    private val getNumbersContactsVipUseCase: GetNumbersContactsVipUseCase,
) : ViewModel() {

    val contactsListViewStateLiveDataSortByFullName = liveData(Dispatchers.IO) {
        getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
            emit(contacts.map {
                transformContactDbToFirstVipSelectionViewState(it)
            })
        }
    }

    private fun transformContactDbToFirstVipSelectionViewState(contact: ContactDB): FirstVipSelectionViewState {
        return FirstVipSelectionViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64,
            contact.priority
        )
    }

    fun updateContact(ids: ArrayList<Pair<Int, Int>>) {
        CoroutineScope(Dispatchers.IO).launch {
            ids.forEach { pair ->
                val id = pair.first
                val priority = pair.second

                updateContactPriorityByIdUseCase.updateContactPriorityById(id, priority)
            }
        }
    }

    fun getNumbersContactsVipUseCase() = getNumbersContactsVipUseCase.getNumbersOfContactsVip()
}