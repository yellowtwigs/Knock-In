package com.yellowtwigs.knockin.repositories.groups.create

import com.yellowtwigs.knockin.model.database.data.GroupDB

interface CreateGroupRepository {

    suspend fun insertGroup(group: GroupDB)
}