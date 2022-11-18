package com.yellowtwigs.knockin.repositories.groups.list

import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.dao.GroupsDao
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class GroupsListRepositoryImpl @Inject constructor(
    private val dao: GroupsDao
) : GroupsListRepository {

    override fun getAllGroups() = dao.getAllGroups().asLiveData()
    override fun getAllGroupsFlow() = dao.getAllGroups()

    override fun getGroupById(id: Int) = dao.getGroup(id).asLiveData()
}