package com.yellowtwigs.knockin.domain.contact.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingViewState
import kotlinx.coroutines.*
import javax.inject.Inject

class GetAllContactsVipUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository
) {

    val contactsListViewStateLiveData: LiveData<List<TeleworkingViewState>> = liveData(Dispatchers.IO) {
        contactsListRepository.getAllContactsVIP().collect { contacts ->
            val listOfDeferred: List<Deferred<TeleworkingViewState>> = coroutineScope {
                contacts.map {
                    async {
                        transformContactDbToTeleworkingViewState(it)
                    }
                }
            }
            val results: List<TeleworkingViewState> = listOfDeferred.awaitAll()

            emit(results)
        }
    }

    private fun transformContactDbToTeleworkingViewState(contact: ContactDB): TeleworkingViewState {
        return TeleworkingViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64)
    }
}