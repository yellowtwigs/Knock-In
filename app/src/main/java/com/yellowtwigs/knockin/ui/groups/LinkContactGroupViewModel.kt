package com.yellowtwigs.knockin.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import com.yellowtwigs.knockin.repositories.groups.LinkContactGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkContactGroupViewModel @Inject constructor(private val repository: LinkContactGroupRepository) :
    ViewModel() {

    fun getAllLinkContactGroup() = repository.getAllLinkContactGroup().asLiveData()

    suspend fun insert(linkContactGroup: LinkContactGroup) = viewModelScope.launch {
        repository.insert(linkContactGroup)
    }

    suspend fun deleteContactInGroup(idContact: Int, idGroup: Int) = viewModelScope.launch {
        repository.deleteContactInGroup(idContact, idGroup)
    }
}