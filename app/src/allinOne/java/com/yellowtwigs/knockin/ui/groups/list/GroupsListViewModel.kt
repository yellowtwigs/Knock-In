package com.yellowtwigs.knockin.ui.groups.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.domain.contact.list.GetAllContactsSortByFullNameUseCase
import com.yellowtwigs.knockin.domain.group.SetGroupIdFlowUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.ui.groups.list.section.SectionViewState
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.Converter.unAccent
import com.yellowtwigs.knockin.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class GroupsListViewModel @Inject constructor(
    groupsListRepository: GroupsListRepository,
    private val getAllContactsSortByFullNameUseCase: GetAllContactsSortByFullNameUseCase,
    private val manageGroupRepository: ManageGroupRepository,
    private val setGroupIdFlowUseCase: SetGroupIdFlowUseCase,
    ) : ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<SectionViewState>>()

    init {
        val allGroups = groupsListRepository.getAllGroupsLiveData()
        val contactsListViewStateLiveDataSortByFullName = liveData(Dispatchers.IO) {
            getAllContactsSortByFullNameUseCase.invoke().collect { contacts ->
                emit(contacts.map {
                    transformContactDbToContactsListViewState(it)
                })
            }
        }

        viewStateLiveData.addSource(allGroups) { groups ->
            combine(groups, contactsListViewStateLiveDataSortByFullName.value)
        }

        viewStateLiveData.addSource(contactsListViewStateLiveDataSortByFullName) { contacts ->
            combine(allGroups.value, contacts)
        }
    }

    private fun combine(allGroups: List<GroupDB>?, allContacts: List<ContactsListViewState>?) {
        val listOfSections = arrayListOf<SectionViewState>()

        if (allGroups != null && allContacts != null) {
            for (group in allGroups) {
                val listOfContactsInGroup = arrayListOf<ContactInGroupViewState>()
                val phoneNumbers = arrayListOf<String>()
                val emails = arrayListOf<String>()

                for (contact in allContacts) {
                    val name: String = if (contact.firstName == "" || contact.firstName.isBlank() || contact.firstName.isEmpty()) {
                        contact.lastName
                    } else if (contact.lastName == "" || contact.lastName.isBlank() || contact.lastName.isEmpty()) {
                        contact.firstName
                    } else {
                        contact.firstName + " " + contact.lastName
                    }

                    if (group.listOfContactsData.contains(name) || group.listOfContactsData.contains(contact.id.toString())) {
                        if (contact.listOfMails[0] != "" && contact.listOfMails[0].isNotEmpty() && contact.listOfMails[0].isNotBlank()) {
                            emails.add(contact.listOfMails[0])
                        }

                        val secondPhoneNumber = if (contact.secondPhoneNumber.phoneNumber.isBlank()) {
                            PhoneNumberWithSpinner(null, "")
                        } else {
                            contact.secondPhoneNumber
                        }

                        listOfContactsInGroup.add(
                            ContactInGroupViewState(
                                contact.id,
                                contact.firstName,
                                contact.lastName,
                                contact.profilePicture,
                                contact.profilePicture64,
                                firstPhoneNumber = contact.firstPhoneNumber,
                                secondPhoneNumber = secondPhoneNumber,
                                contact.listOfMails,
                                contact.priority,
                                contact.hasWhatsapp,
                                contact.hasTelegram,
                                contact.hasSignal,
                                contact.messengerId,
                                group.name
                            )
                        )
                    }
                }

                listOfSections.add(
                    SectionViewState(
                        group.id, group.name, group.section_color, sortedContactsList(listOfContactsInGroup), phoneNumbers, emails,
                        EquatableCallback {
                            Log.i("GetContactsFromGroup", "group.id : ${group.id}")
                            setGroupIdFlowUseCase.invoke(group.id)
                        }
                    )
                )
            }
        }

        viewStateLiveData.value = listOfSections
    }

    private fun sortedContactsList(
        list: ArrayList<ContactInGroupViewState>
    ): List<ContactInGroupViewState> {
        return list.sortedBy {
            if (it.firstName == "" || it.firstName == " " || it.firstName.isBlank() || it.firstName.isEmpty()) {
                it.lastName.uppercase().unAccent()
            } else {
                it.firstName.uppercase().unAccent()
            }
        }
    }

    private fun transformContactDbToContactsListViewState(contact: ContactDB): ContactsListViewState {
        val fullName = if (contact.firstName.isEmpty() || contact.firstName.isBlank() || contact.firstName == " ") {
            contact.lastName
        } else if (contact.lastName.isEmpty() || contact.lastName.isBlank() || contact.lastName == " ") {
            contact.firstName
        } else {
            "${contact.firstName} ${contact.firstName}"
        }

        return ContactsListViewState(
            id = contact.id,
            fullName = fullName,
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
            contact.listOfMessagingApps.contains("org.thoughtcrime.securesms"),
            EquatableCallback {  }
        )
    }

    fun getAllGroups(): LiveData<List<SectionViewState>> {
        return viewStateLiveData
    }

    suspend fun deleteGroupById(id: Int) {
        manageGroupRepository.deleteGroupById(id)
    }
}