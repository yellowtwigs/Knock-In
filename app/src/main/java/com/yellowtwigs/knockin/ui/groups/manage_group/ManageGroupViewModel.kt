package com.yellowtwigs.knockin.ui.groups.manage_group

import android.util.Log
import androidx.lifecycle.*
import com.yellowtwigs.knockin.domain.contact.GetAllContactsSortByFullNameUseCase
import com.yellowtwigs.knockin.domain.group.GetGroupIdFlowUseCase
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.ContactGesture.transformContactDbToContactsListViewState
import com.yellowtwigs.knockin.utils.Converter.unAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageGroupViewModel @Inject constructor(
    private val manageGroupRepository: ManageGroupRepository,
    private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
//    private val getGroupIdFlowUseCase: GetGroupIdFlowUseCase,
    private val groupsListRepository: GroupsListRepository
) : ViewModel() {

    private val _groupViewState = MutableSharedFlow<ManageGroupViewState>(replay = 1)
    val groupViewState: SharedFlow<ManageGroupViewState> = _groupViewState

    private var groupStateMutableSharedFlow = MutableSharedFlow<ManageGroupViewState>(replay = 1)
    private var groupIdStateMutableSharedFlow = MutableSharedFlow<Int>(replay = 1)
    private val groupIdFlow = MutableStateFlow(0)
    private val listOfContactsInGroupLiveData = MutableLiveData<List<String>>()

    val manageGroupViewStateLiveData: LiveData<ManageGroupViewState> = liveData(Dispatchers.IO) {
        combine(getAllContactsSortByFullNameUseCase.invoke(),
            groupIdFlow,
            groupStateMutableSharedFlow) { contacts,groupId,   group ->
            var manageGroupViewState = ManageGroupViewState(
                group.id,
                group.groupName,
                group.section_color,
                group.listOfContacts,
                group.listOfIds
            )

            Log.i("GetContactsFromGroup", "contacts : $contacts")
            Log.i("GetContactsFromGroup", "groupId : $groupId")

            val allContacts = contacts.map {
                transformContactDbToContactsListViewState(it)
            }

            if (allContacts.isNotEmpty()) {
                withContext(Dispatchers.Default) {
                    manageGroupViewState = ManageGroupViewState(
                        group.id,
                        group.groupName,
                        group.section_color,
                        sortedContactsList(
                            contactsToContactsManageGroupViewState(allContacts)
                        ),
                        group.listOfIds
                    )
                }
            }

            emit(manageGroupViewState)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("GetContactsFromGroup", "ManageGroupViewModel : Pre Collect")
//            getGroupIdFlowUseCase.invoke().collect { id ->
//                Log.i("GetContactsFromGroup", "ManageGroupViewModel : Post Collect")
//                Log.i("GetContactsFromGroup", "id : $id")
//
//            }
        }
    }

//    private fun combine(allContacts: List<ContactsListViewState>?, group: GroupDB?) {
//        CoroutineScope(Dispatchers.IO).launch {
//            if (allContacts?.isNotEmpty() == true) {
//                if (group != null) {
//                    withContext(Dispatchers.Main) {
//                        viewStateLiveData.value = ManageGroupViewState(
//                            group.id,
//                            group.name,
//                            group.section_color,
//                            sortedContactsList(
//                                contactsToContactsManageGroupViewState(allContacts)
//                            ),
//                            group.listOfContactsData
//                        )
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        viewStateLiveData.value = ManageGroupViewState(
//                            0,
//                            "",
//                            0,
//                            sortedContactsList(
//                                contactsToContactsManageGroupViewState(allContacts)
//                            ), arrayListOf()
//                        )
//                    }
//                }
//            }
//        }
//    }

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

    suspend fun createNewGroup(group: GroupDB) = viewModelScope.launch {
        manageGroupRepository.insertGroup(group)
    }

    suspend fun updateGroup(group: GroupDB) = viewModelScope.launch {
        manageGroupRepository.updateGroup(group)
    }

    fun setGroupById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            groupIdStateMutableSharedFlow.tryEmit(id)

            val manageGroupViewState = id.let { groupsListRepository.getGroupById(it).firstOrNull() }
            val contacts = getAllContactsSortByFullNameUseCase.invoke().firstOrNull()

            Log.i("GetContactsFromGroup", "manageGroupViewState : $manageGroupViewState")

            Log.i("GetContactsFromGroup", "contacts : $contacts")
            Log.i("GetContactsFromGroup", "groupId : $id")

            if (manageGroupViewState == null) {
                if (contacts != null) {
                    val allContacts = contacts.map {
                        transformContactDbToContactsListViewState(it)
                    }
                    _groupViewState.tryEmit(
                        ManageGroupViewState(
                            id = 0,
                            groupName = "",
                            section_color = 0,
                            listOfContacts = sortedContactsList(
                                contactsToContactsManageGroupViewState(allContacts)
                            ),
                            listOfIds = listOf(),
                        )
                    )
                    groupStateMutableSharedFlow.emit(
                        ManageGroupViewState(
                            id = 0,
                            groupName = "",
                            section_color = 0,
                            listOfContacts = sortedContactsList(
                                contactsToContactsManageGroupViewState(allContacts)
                            ),
                            listOfIds = listOf(),
                        )
                    )
                } else {
                    _groupViewState.tryEmit(ManageGroupViewState(
                        id = 0,
                        groupName = "",
                        section_color = 0,
                        listOfContacts = listOf(),
                        listOfIds = listOf(),
                    ))
                    groupStateMutableSharedFlow.emit(
                        ManageGroupViewState(
                            id = 0,
                            groupName = "",
                            section_color = 0,
                            listOfContacts = listOf(),
                            listOfIds = listOf(),
                        )
                    )
                }
            } else {
                if (contacts != null) {
                    val allContacts = contacts.map {
                        transformContactDbToContactsListViewState(it)
                    }
                    _groupViewState.tryEmit(ManageGroupViewState(
                        id = manageGroupViewState.id,
                        groupName = manageGroupViewState.name,
                        section_color = manageGroupViewState.section_color,
                        listOfContacts = sortedContactsList(
                            contactsToContactsManageGroupViewState(allContacts)
                        ),
                        listOfIds = manageGroupViewState.listOfContactsData,
                    ))
                    groupStateMutableSharedFlow.emit(
                        ManageGroupViewState(
                            id = manageGroupViewState.id,
                            groupName = manageGroupViewState.name,
                            section_color = manageGroupViewState.section_color,
                            listOfContacts = sortedContactsList(
                                contactsToContactsManageGroupViewState(allContacts)
                            ),
                            listOfIds = manageGroupViewState.listOfContactsData,
                        )
                    )
                } else {
                    _groupViewState.tryEmit(ManageGroupViewState(
                        id = manageGroupViewState.id,
                        groupName = manageGroupViewState.name,
                        section_color = manageGroupViewState.section_color,
                        listOfContacts = listOf(),
                        listOfIds = manageGroupViewState.listOfContactsData,
                    ))
                    groupStateMutableSharedFlow.emit(
                        ManageGroupViewState(
                            id = manageGroupViewState.id,
                            groupName = manageGroupViewState.name,
                            section_color = manageGroupViewState.section_color,
                            listOfContacts = listOf(),
                            listOfIds = manageGroupViewState.listOfContactsData,
                        )
                    )
                }

            }
        }
    }

    fun getListOfContactsInGroupLiveData(): LiveData<List<String>> {
        return listOfContactsInGroupLiveData
    }
}