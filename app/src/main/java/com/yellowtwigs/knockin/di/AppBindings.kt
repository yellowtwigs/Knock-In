package com.yellowtwigs.knockin.di

import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepository
import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepositoryImpl
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepositoryImpl
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepositoryImpl
import com.yellowtwigs.knockin.ui.notifications.listener.NotificationsListenerViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppBindings {

    @Binds
    @Singleton
    abstract fun bindContactsRepository(impl: ContactsListRepositoryImpl): ContactsListRepository

    @Binds
    @Singleton
    abstract fun bindInsertContactRepository(impl: CreateContactRepositoryImpl): CreateContactRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsListenerViewModel(notificationsRepository: NotificationsRepository,
                                           contactsListRepository: ContactsListRepository
    ): NotificationsListenerViewModel
}