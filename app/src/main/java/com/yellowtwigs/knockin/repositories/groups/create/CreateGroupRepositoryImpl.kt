package com.yellowtwigs.knockin.repositories.groups.create

import com.yellowtwigs.knockin.model.database.dao.GroupsDao
import com.yellowtwigs.knockin.model.database.data.GroupDB
import javax.inject.Inject

class CreateGroupRepositoryImpl @Inject constructor(private val dao: GroupsDao) :
    CreateGroupRepository {

    override suspend fun insertGroup(group: GroupDB) = dao.insertGroup(group)
}