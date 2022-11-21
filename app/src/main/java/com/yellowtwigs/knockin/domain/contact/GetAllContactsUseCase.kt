package com.yellowtwigs.knockin.domain.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionViewState
import com.yellowtwigs.knockin.utils.Converter.unAccent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class GetAllContactsUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    fun invoke() = contactsListRepository.getAllContacts()
}