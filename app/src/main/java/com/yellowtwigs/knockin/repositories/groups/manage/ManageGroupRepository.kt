package com.yellowtwigs.knockin.repositories.groups.manage

import com.yellowtwigs.knockin.model.database.data.GroupDB

interface ManageGroupRepository {

    suspend fun insertGroup(group: GroupDB)
    suspend fun updateGroup(group: GroupDB)

    suspend fun deleteGroupById(id: Int)
}