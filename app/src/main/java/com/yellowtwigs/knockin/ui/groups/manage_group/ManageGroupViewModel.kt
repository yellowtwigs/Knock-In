package com.yellowtwigs.knockin.ui.groups.manage_group

import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.GetAllContactsSortByFullNameUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageGroupViewModel @Inject constructor(
    private val manageGroupRepository: ManageGroupRepository,
    private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
    private val groupsListRepository: GroupsListRepository
) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<ManageGroupViewState>()

    private val groupIdLiveData = MutableLiveData<Int>()
    private val listOfContactsInGroupLiveData = MutableLiveData<List<String>>()

    init {
        val contactsListViewStateLiveDataSortByFullName = liveData(Dispatchers.IO) {
            getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
                emit(contacts.map {
                    transformContactDbToContactsListViewState(it)
                })
            }
        }

        val groupById = Transformations.switchMap(groupIdLiveData) { id ->
            return@switchMap groupsListRepository.getGroupById(id)
        }

        viewStateLiveData.addSource(contactsListViewStateLiveDataSortByFullName) { contacts ->
            combine(contacts, groupById.value)
        }

        viewStateLiveData.addSource(groupById) { group ->
            combine(contactsListViewStateLiveDataSortByFullName.value, group)
        }
    }

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        return ContactsListViewState(
            contact.id,
            contact.firstName,
            contact.lastName,
            contact.profilePicture,
            contact.profilePicture64,
            firstPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, true),
            secondPhoneNumber = ContactGesture.transformPhoneNumberToSinglePhoneNumberWithSpinner(contact.listOfPhoneNumbers, false),
            contact.listOfMails,
            contact.priority,
            contact.isFavorite == 1,
            contact.messengerId,
            contact.listOfMessagingApps.contains("com.whatsapp"),
            contact.listOfMessagingApps.contains("org.telegram.messenger"),
            contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
        )
    }

    private fun combine(allContacts: List<ContactsListViewState>?, group: GroupDB?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (allContacts?.isNotEmpty() == true) {
                if (group != null) {
                    withContext(Dispatchers.Main) {
                        viewStateLiveData.value = ManageGroupViewState(
                            group.id,
                            group.name,
                            group.section_color,
                            sortedContactsList(
                                contactsToContactsManageGroupViewState(allContacts)
                            ),
                            group.listOfContactsData
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        viewStateLiveData.value = ManageGroupViewState(
                            0,
                            "",
                            0,
                            sortedContactsList(
                                contactsToContactsManageGroupViewState(allContacts)
                            ), arrayListOf()
                        )
                    }
                }
            }
        }
    }

    private fun sortedContactsList(
        list: ArrayList<ContactManageGroupViewState>
    ): List<ContactManageGroupViewState> {
        return list.sortedBy {
            if (it.firstName == "" || it.firstName == " " || it.firstName.isBlank() || it.firstName.isEmpty()) {
                it.lastName.uppercase().unAccent()
            } else {
                it.firstName.uppercase().unAccent()
            }
        }
    }

    private fun contactsToContactsManageGroupViewState(
        contacts: List<ContactsListViewState>
    ): ArrayList<ContactManageGroupViewState> {
        val listOfContactManageGroupViewState = arrayListOf<ContactManageGroupViewState>()

        for (contact in contacts) {
            listOfContactManageGroupViewState.add(
                ContactManageGroupViewState(
                    contact.id,
                    contact.firstName,
                    contact.lastName,
                    contact.profilePicture,
                    contact.profilePicture64,
                    contact.priority
                )
            )
//            val name: String =
//                if (contact.firstName == "" || contact.firstName.isBlank() ||
//                    contact.firstName.isEmpty()
//                ) {
//                    contact.lastName
//                } else if (contact.lastName == "" || contact.lastName.isBlank() ||
//                    contact.lastName.isEmpty()
//                ) {
//                    contact.firstName
//                } else {
//                    contact.firstName + " " + contact.lastName
//                }
//
//            if (group.listOfContactsData.contains(name) || group.listOfContactsData.contains(
//                    contact.id.toString()
//                )
//            ) {
//                listOfContactManageGroupViewState.add(
//                    ContactManageGroupViewState(
//                        contact.id,
//                        contact.firstName,
//                        contact.lastName,
//                        contact.profilePicture,
//                        contact.profilePicture64
//                    )
//                )
//            }
        }

        return listOfContactManageGroupViewState
    }

    fun getManageGroupViewState(): LiveData<ManageGroupViewState> {
        return viewStateLiveData
    }

    suspend fun createNewGroup(group: GroupDB) = viewModelScope.launch {
        manageGroupRepository.insertGroup(group)
    }

    suspend fun updateGroup(group: GroupDB) = viewModelScope.launch {
        manageGroupRepository.updateGroup(group)
    }

    fun setGroupById(id: Int) {
        groupIdLiveData.value = id
    }

    fun getListOfContactsInGroupLiveData(): LiveData<List<String>> {
        return listOfContactsInGroupLiveData
    }
}