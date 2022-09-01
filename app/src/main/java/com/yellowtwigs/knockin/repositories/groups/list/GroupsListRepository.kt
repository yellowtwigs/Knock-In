package com.yellowtwigs.knockin.repositories.groups.list

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.GroupDB

interface GroupsListRepository {

    fun getAllGroups(): LiveData<List<GroupDB>>
}