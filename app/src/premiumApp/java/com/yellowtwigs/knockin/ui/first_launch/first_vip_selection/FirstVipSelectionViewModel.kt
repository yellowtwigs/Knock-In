package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import android.util.Log
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class FirstVipSelectionViewModel @Inject constructor(
        private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
        private val updateContactPriorityByIdUseCase: UpdateContactPriorityByIdUseCase,
        private val getNumbersContactsVipUseCase: GetNumbersContactsVipUseCase,
        private val contactsListRepository: ContactsListRepository
) : ViewModel() {

    val nbVipContacts = MutableLiveData(getNumbersContactsVipUseCase.getNumbersOfContactsVip())

    val contactsIds = contactsListRepository.getContactsVIPIds()
    val listOfItemsSelected: MutableLiveData<ArrayList<Int>> = MutableLiveData(contactsIds)

    fun addItemToList(item: Int) {
        if (contactsIds.contains(item)) {
            contactsIds.remove(item)
            listOfItemsSelected.value = contactsIds
        } else {
            contactsIds.add(item)
            listOfItemsSelected.value = contactsIds
        }
    }

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
                contact.androidId,
                contact.firstName,
                contact.lastName,
                contact.profilePicture,
                contact.profilePicture64,
                contact.priority
        )
    }

    fun updateContact(ids: ArrayList<Triple<Int, Int?, Int>>, contentResolver: ContentResolver) {
        CoroutineScope(Dispatchers.IO).launch {
            ids.forEach { pair ->
                val id = pair.first
                val androidId = pair.second
                val priority = pair.third

                try {
                    androidId?.toString()?.let { setSendToVoicemailFlag(it, contentResolver) }
                } catch (e: Exception) {
                    Log.e("Exception", "exception : $e")
                }

                updateContactPriorityByIdUseCase.updateContactPriorityById(id, priority)
            }
        }
    }

    private fun setSendToVoicemailFlag(contactId: String, resolver: ContentResolver) {
        val contentValues = ContentValues()
        contentValues.put(ContactsContract.Contacts.SEND_TO_VOICEMAIL, 0)

        val where = ContactsContract.Contacts._ID + " = ?"
        val whereArgs = arrayOf(contactId)

        resolver.update(ContactsContract.Contacts.CONTENT_URI, contentValues, where, whereArgs)
    }

    fun getNumbersContactsVipUseCase() = getNumbersContactsVipUseCase.getNumbersOfContactsVip()
}