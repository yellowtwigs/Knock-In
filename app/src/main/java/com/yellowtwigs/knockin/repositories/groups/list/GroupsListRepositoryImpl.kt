package com.yellowtwigs.knockin.repositories.groups.list

import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.GroupsDao
import javax.inject.Inject

class GroupsListRepositoryImpl @Inject constructor(private val dao: GroupsDao) :
    GroupsListRepository {

    override fun getAllGroups() = dao.getAllGroups().asLiveData()
}