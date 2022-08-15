package com.yellowtwigs.knockin.repositories.contacts.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.data.ContactDB
import javax.inject.Inject

class ContactsListRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    ContactsListRepository {

    private val searchBarText = MutableLiveData<String>()

    override fun getAllContacts() = dao.getAllContacts()
    override fun getContact(id: Int) = dao.getContact(id)

    override suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)
    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)

    override fun getSearchBarText() = searchBarText

    override fun setSearchBarText(text: String) {
        searchBarText.postValue(text)
    }

    override fun getSortedBy(): LiveData<Int> {
        TODO("Not yet implemented")
    }

    override fun setSortedBy(sortBy: Int) {
        TODO("Not yet implemented")
    }

    override fun getFilterBy(): LiveData<Int> {
        TODO("Not yet implemented")
    }

    override fun setFilterBy(sortBy: Int) {
        TODO("Not yet implemented")
    }
}