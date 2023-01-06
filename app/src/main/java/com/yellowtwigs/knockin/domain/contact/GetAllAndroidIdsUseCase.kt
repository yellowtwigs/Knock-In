package com.yellowtwigs.knockin.domain.contact

import android.util.Log
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllAndroidIdsUseCase @Inject constructor(val contactsListRepository: ContactsListRepository) {

    fun invoke(): List<Int> {
        Log.i(
            "GetAllAndroidsIds",
            "contactsListRepository.getAllAndroidIds() : ${contactsListRepository.getAllAndroidIds()}"
        )
        return contactsListRepository.getAllAndroidIds()
    }
}