package com.yellowtwigs.knockin.ui.groups.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupsListViewModel @Inject constructor(private val groupsListRepository: GroupsListRepository) :
    ViewModel() {

    fun getAllGroups(): LiveData<List<GroupDB>> {
        return groupsListRepository.getAllGroups()
    }
}