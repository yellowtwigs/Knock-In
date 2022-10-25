package com.yellowtwigs.knockin.ui.groups.manage_group

import androidx.lifecycle.*
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageGroupViewModel @Inject constructor(
    contactsListRepository: ContactsListRepository,
    private val manageGroupRepository: ManageGroupRepository,
    private val groupsListRepository: GroupsListRepository
) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<ManageGroupViewState>()

    private val groupIdLiveData = MutableLiveData<Int>()
    private val listOfContactsInGroupLiveData = MutableLiveData<List<String>>()

    init {
        val allContacts = contactsListRepository.getAllContacts()

        val groupById = Transformations.switchMap(groupIdLiveData) { id ->
            return@switchMap groupsListRepository.getGroupById(id)
        }

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(contacts, groupById.value)
        }

        viewStateLiveData.addSource(groupById) { group ->
            combine(allContacts.value, group)
        }
    }

    private fun combine(allContacts: List<ContactDB>?, group: GroupDB?) {
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
                it.lastName
            } else {
                it.firstName
            }
        }
    }

    private fun contactsToContactsManageGroupViewState(
        contacts: List<ContactDB>
    ): ArrayList<ContactManageGroupViewState> {
        val listOfContactManageGroupViewState = arrayListOf<ContactManageGroupViewState>()

        for (contact in contacts) {
            listOfContactManageGroupViewState.add(
                ContactManageGroupViewState(
                    contact.id,
                    contact.firstName,
                    contact.lastName,
                    contact.profilePicture,
                    contact.profilePicture64
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