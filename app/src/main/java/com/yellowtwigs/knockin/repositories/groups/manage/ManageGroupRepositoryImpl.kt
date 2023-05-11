package com.yellowtwigs.knockin.repositories.groups.manage

import com.yellowtwigs.knockin.model.database.dao.GroupsDao
import com.yellowtwigs.knockin.model.database.data.GroupDB
import javax.inject.Inject

class ManageGroupRepositoryImpl @Inject constructor(private val dao: GroupsDao) : ManageGroupRepository {

    override suspend fun insertGroup(group: GroupDB) = dao.insertGroup(group)
    override suspend fun updateGroup(group: GroupDB) = dao.updateGroup(group)

    override fun getAllGroups() = dao.getAllGroups()

    override suspend fun deleteGroupById(id: Int) = dao.deleteGroupById(id)
}