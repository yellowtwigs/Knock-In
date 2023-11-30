package com.yellowtwigs.knockin.ui.contacts.multi_channel

import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.domain.contact.GetContactsByIds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MultiChannelViewModel @Inject constructor(
    private val getContactsByIds: GetContactsByIds
) : ViewModel() {

    fun getContactsByIds(ids: List<Int>) = getContactsByIds.getContactsByIds(ids)
}