package com.yellowtwigs.knockin.repositories.groups.list

import androidx.lifecycle.LiveData
import com.yellowtwigs.knockin.model.database.data.GroupDB
import kotlinx.coroutines.flow.Flow

interface GroupsListRepository {

    fun getAllGroupsLiveData(): LiveData<List<GroupDB>>
    fun getAllGroupsFlow(): Flow<List<GroupDB>>

    fun getGroupById(id: Int): LiveData<GroupDB>
}