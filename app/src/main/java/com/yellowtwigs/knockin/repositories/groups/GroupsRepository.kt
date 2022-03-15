package com.yellowtwigs.knockin.repositories.groups

import com.yellowtwigs.knockin.model.dao.GroupsDao
import com.yellowtwigs.knockin.model.data.GroupDB
import javax.inject.Inject

class GroupsRepository @Inject constructor(private val dao: GroupsDao) {

    fun getAllGroups() = dao.getAllGroups()
    fun getGroupWithName(groupName: String) = dao.getGroupWithName(groupName)
    fun getGroupById(id: Int) = dao.getGroupById(id)
    fun getGroupsForContact(contactId: Int) = dao.getGroupsForContact(contactId)

    suspend fun insertGroup(group: GroupDB) = dao.insertGroup(group)
    suspend fun updateGroup(group: GroupDB) = dao.updateGroup(group)
    suspend fun updateGroupSectionColorById(id: Int, section_color: Int) =
        dao.updateGroupSectionColorById(id, section_color)
    suspend fun deleteGroup(group: GroupDB) = dao.deleteGroup(group)
}