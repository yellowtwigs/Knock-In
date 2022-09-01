package com.yellowtwigs.knockin.repositories.contacts.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class ContactsListRepositoryImpl @Inject constructor(private val dao: ContactsDao) :
    ContactsListRepository {

    private val searchTextChanged = MutableLiveData<String>()
    private val sortByLiveData = MutableLiveData<Int>()
    private val filterByLiveData = MutableLiveData<Int>()

    override fun getAllContacts() = dao.getAllContacts().asLiveData()
    override fun getContact(id: Int) = dao.getContact(id)

    override suspend fun updateContact(contact: ContactDB) = dao.updateContact(contact)

    override suspend fun updateContactPriorityById(id: Int, priority: Int) {
        dao.updateContactPriorityById(id, priority)
    }

    override suspend fun deleteContact(contact: ContactDB) = dao.deleteContact(contact)

    override fun getSearchBarText() = searchTextChanged

    override fun setSearchTextChanged(text: String) {
        searchTextChanged.postValue(text)
    }

    override fun getSortedBy() = sortByLiveData

    override fun setSortedBy(sortBy: Int) {
        sortByLiveData.postValue(sortBy)
    }

    override fun getFilterBy() = filterByLiveData

    override fun setFilterBy(filterBy: Int) {
        filterByLiveData.postValue(filterBy)
    }
}