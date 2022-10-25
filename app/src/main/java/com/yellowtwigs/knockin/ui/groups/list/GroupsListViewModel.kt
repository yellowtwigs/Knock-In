package com.yellowtwigs.knockin.ui.groups.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.ui.groups.list.section.SectionViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupsListViewModel @Inject constructor(
    groupsListRepository: GroupsListRepository,
    contactsListRepository: ContactsListRepository,
    private val manageGroupRepository: ManageGroupRepository
) :
    ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<SectionViewState>>()

    init {
        val allGroups = groupsListRepository.getAllGroups()
        val allContacts = contactsListRepository.getAllContacts()

        viewStateLiveData.addSource(allGroups) { groups ->
            combine(groups, allContacts.value)
        }

        viewStateLiveData.addSource(allContacts) { contacts ->
            combine(allGroups.value, contacts)
        }
    }

    private fun combine(allGroups: List<GroupDB>?, allContacts: List<ContactDB>?) {
        val listOfSections = arrayListOf<SectionViewState>()

        if (allGroups != null && allContacts != null) {

            for (group in allGroups) {
                val listOfContactsInGroup = arrayListOf<ContactInGroupViewState>()

                for (contact in allContacts) {
                    val name: String = if (contact.firstName == "" || contact.firstName.isBlank() ||
                        contact.firstName.isEmpty()
                    ) {
                        contact.lastName
                    } else if (contact.lastName == "" || contact.lastName.isBlank() ||
                        contact.lastName.isEmpty()
                    ) {
                        contact.firstName
                    } else {
                        contact.firstName + " " + contact.lastName
                    }

                    if (group.listOfContactsData.contains(name) || group.listOfContactsData.contains(
                            contact.id.toString()
                        )
                    ) {
                        listOfContactsInGroup.add(
                            ContactInGroupViewState(
                                0,
                                contact.firstName,
                                contact.lastName,
                                contact.profilePicture,
                                contact.profilePicture64,
                                contact.listOfPhoneNumbers,
                                contact.listOfMails,
                                contact.priority,
                                contact.listOfMessagingApps.contains("com.whatsapp"),
                                contact.listOfMessagingApps.contains("org.telegram.messenger"),
                                contact.listOfMessagingApps.contains("org.thoughtcrime.securesms")
                            )
                        )
                    }
                }

                Log.i("sectionColor", "group.section_color : ${group.section_color}")

                listOfSections.add(
                    SectionViewState(
                        group.id,
                        group.name,
                        group.section_color,
                        sortedContactsList(listOfContactsInGroup)
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
                it.lastName.uppercase()
            } else {
                it.firstName.uppercase()
            }
        }
    }

    fun getAllGroups(): LiveData<List<SectionViewState>> {
        return viewStateLiveData
    }

    suspend fun deleteGroupById(id: Int) {
        manageGroupRepository.deleteGroupById(id)
    }
}