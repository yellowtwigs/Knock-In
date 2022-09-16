package com.yellowtwigs.knockin.ui.groups.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.ui.groups.list.section.SectionViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupsListViewModel @Inject constructor(
    groupsListRepository: GroupsListRepository,
    contactsListRepository: ContactsListRepository
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
                val listOfGroups = arrayListOf<GroupsListViewState>()

//                Log.i("getAllGroups", "group.listOfContactsData : ${group.listOfContactsData}")

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

//                    if (name.contains("Top") && name.contains("Mess")) {
//                        name.toCharArray().forEach {
//                            Log.i("getAllGroups", "Char : ${it}")
//                        }
//                    }


                    if (group.listOfContactsData.contains(name)) {
                        listOfGroups.add(
                            GroupsListViewState(
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

                listOfSections.add(
                    SectionViewState(
                        group.id,
                        group.name,
                        listOfGroups
                    )
                )
            }
        }

//        Log.i("getAllGroups", "${listOfSections}")

        viewStateLiveData.value = listOfSections
    }


    fun getAllGroups(): LiveData<List<SectionViewState>> {
        return viewStateLiveData
    }
}