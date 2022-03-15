package com.yellowtwigs.knockin.ui.groups

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.model.data.GroupDB
import com.yellowtwigs.knockin.model.data.GroupWithContact
import com.yellowtwigs.knockin.repositories.groups.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(private val repository: GroupsRepository) :
    ViewModel() {

    fun getAllGroups() = repository.getAllGroups().asLiveData()
    fun getGroupWithName(groupName: String) = repository.getGroupWithName(groupName).asLiveData()
    fun getGroupById(id: Int) = repository.getGroupById(id).asLiveData()
    fun getGroupsForContact(contactId: Int) = repository.getGroupsForContact(contactId).asLiveData()

    suspend fun insertGroup(group: GroupDB) = repository.insertGroup(group)

    fun updateGroup(group: GroupDB) = viewModelScope.launch {
        repository.updateGroup(group)
    }

    fun updateGroupSectionColorById(id: Int, section_color: Int) = viewModelScope.launch {
        repository.updateGroupSectionColorById(id, section_color)
    }

    fun deleteGroup(group: GroupWithContact) = viewModelScope.launch {
        group.groupDB?.let { repository.deleteGroup(it) }
    }

    var groupWithContactLiveData = MutableLiveData<GroupWithContact>()
    fun setGroupWithContact(groupWithContact: GroupWithContact) {
        groupWithContactLiveData.value = groupWithContact
    }

    fun getContactGroupSync(resolver: ContentResolver): List<Triple<Int, String?, String?>> {
        val phoneContact = resolver.query(
            ContactsContract.Groups.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Groups.TITLE + " ASC"
        )
        var allGroupMembers = listOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val groupId =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.Groups._ID))
            var groupName =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.Groups.TITLE))

            if (groupName == "Starred in Android") {
                groupName = "Favorites"
            }

            if (groupName == "My Contacts") {
            } else {
                val groupMembers =
                    getMemberOfGroup(resolver, groupId.toString(), groupName)
                if (groupMembers.isNotEmpty() && allGroupMembers.isNotEmpty() && !isDuplicateGroup(
                        allGroupMembers,
                        groupMembers
                    )
                ) {
                    allGroupMembers = allGroupMembers.union(groupMembers).toList()
                } else if (allGroupMembers.isEmpty())
                    allGroupMembers = groupMembers

            }
        }
        phoneContact?.close()
        return allGroupMembers
    }

    private fun getMemberOfGroup(
        resolver: ContentResolver,
        groupId: String,
        groupName: String?
    ): List<Triple<Int, String?, String?>> {
        val where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId
        val phoneContact = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            where,
            null,
            ContactsContract.Data.DISPLAY_NAME + " ASC"
        )
        var member: Triple<Int, String?, String?>
        val groupMembers = arrayListOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val contactId =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.Data.CONTACT_ID))
            val contactName =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            member = Triple(contactId!!.toInt(), contactName, groupName)
            if (!groupMembers.contains(member)) {
                groupMembers.add(member)
            }
        }
        phoneContact?.close()
        return groupMembers
    }

    fun getGroupsAndLinks(
        id: Int,
        contactGroup: List<Triple<Int, String?, String?>>
    ): List<GroupDB> {
        val contactGroups = arrayListOf<GroupDB>()
        var linkAndGroup: GroupDB
        contactGroup.forEach {
            if (it.first == id) {
                linkAndGroup = GroupDB(it.third!!, "", -500138)
                contactGroups.add(linkAndGroup)
            }
        }
        return contactGroups
    }

    private fun isDuplicateGroup(
        member: List<Triple<Int, String?, String?>>,
        groupMembers: List<Triple<Int, String?, String?>>
    ): Boolean {
        groupMembers.forEach { _ ->
            groupMembers.forEachIndexed { index, it ->
                if (it.third == member[0].third)
                    return true
            }
        }
        return false
    }
}