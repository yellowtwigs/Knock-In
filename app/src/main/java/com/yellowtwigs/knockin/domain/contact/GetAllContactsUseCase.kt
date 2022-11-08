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
    contactsListRepository: ContactsListRepository
) {

    val contactsListViewStateLiveData: LiveData<List<ContactsListViewState>> = liveData(Dispatchers.IO) {
        contactsListRepository.getAllContacts().collect { contacts ->
            val listOfDeferred: List<Deferred<ContactsListViewState>> = coroutineScope {
                contacts.map {
                    async {
                        transformContactDbToContactsListViewState(it)
                    }
                }
            }
            val results: List<ContactsListViewState> = listOfDeferred.awaitAll()

            emit(results)
        }
    }

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        return ContactsListViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64,
            contact.listOfPhoneNumbers,
            contact.listOfMails,
            contact.priority,
            contact.isFavorite == 1,
            contact.messengerId,
            contact.listOfMessagingApps.contains("com.whatsapp"),
            contact.listOfMessagingApps.contains("org.telegram.messenger"),
            contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
        )
    }


    val firstVipSelectionViewStateLiveData: LiveData<List<FirstVipSelectionViewState>> = liveData(Dispatchers.IO) {
        contactsListRepository.getAllContacts().collect { contacts ->
            val listOfDeferred: List<Deferred<FirstVipSelectionViewState>> = coroutineScope {
                contacts.map {
                    async {
                        transformContactDbToFirstVipSelectionViewState(it)
                    }
                }
            }
            val results: List<FirstVipSelectionViewState> = listOfDeferred.awaitAll()

            emit(results)
        }
    }

    private fun transformContactDbToFirstVipSelectionViewState(contact: ContactDB): FirstVipSelectionViewState {
        return FirstVipSelectionViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64)
    }
}