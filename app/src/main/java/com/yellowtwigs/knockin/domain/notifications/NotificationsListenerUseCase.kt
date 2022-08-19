package com.yellowtwigs.knockin.domain.notifications

import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import javax.inject.Inject

class NotificationsListenerUseCase @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val contactsListRepository: ContactsListRepository
) {

    suspend operator fun invoke(): Boolean {



        return true
    }
}