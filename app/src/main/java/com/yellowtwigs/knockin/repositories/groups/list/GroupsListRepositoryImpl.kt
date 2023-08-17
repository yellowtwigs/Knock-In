package com.yellowtwigs.knockin.repositories.groups.list

import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.GroupsDao
import javax.inject.Inject

class GroupsListRepositoryImpl @Inject constructor(
    private val dao: GroupsDao
) : GroupsListRepository {

    override fun getAllGroupsLiveData() = dao.getAllGroupsFlow().asLiveData()
    override fun getAllGroupsFlow() = dao.getAllGroupsFlow()

    override fun getGroupById(id: Int) = dao.getGroup(id)
}