package com.yellowtwigs.knockin.domain.contact

import androidx.lifecycle.liveData
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.utils.CoroutineDispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllContactsUseCase @Inject constructor(
    private val contactsListRepository: ContactsListRepository, coroutineDispatcherProvider: CoroutineDispatcherProvider
) {

    private val sortingMutableStateFlow = MutableStateFlow(R.id.sort_by_priority)

    fun setSortedBy(sortedBy: Int) {
        sortingMutableStateFlow.value = sortedBy
    }

    val contactsListViewStateLiveData = liveData(coroutineDispatcherProvider.io) {
        combine(
            contactsListRepository.getAllContacts(),
            contactsListRepository.getAllContactsByFavorite(),
            contactsListRepository.getAllContactsByFullName(),
            sortingMutableStateFlow
        ) { contactsByPriority, contactsByFavorite, contactsByFullName, sortBy ->
            emit(
                when (sortBy) {
                    R.id.sort_by_full_name -> {
                        contactsByFullName
                    }
                    R.id.sort_by_priority -> {
                        contactsByPriority
                    }
                    R.id.sort_by_favorite -> {
                        contactsByFavorite
                    }
                    else -> {
                        contactsByPriority
                    }
                }
            )
        }.collectLatest { }
    }
}