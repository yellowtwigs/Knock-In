package com.yellowtwigs.knockin.di

import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepository
import com.yellowtwigs.knockin.repositories.contacts.create.CreateContactRepositoryImpl
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepository
import com.yellowtwigs.knockin.repositories.contacts.edit.EditContactRepositoryImpl
import com.yellowtwigs.knockin.repositories.contacts.id.CurrentContactIdRepository
import com.yellowtwigs.knockin.repositories.contacts.id.CurrentContactIdRepositoryImpl
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepositoryImpl
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepository
import com.yellowtwigs.knockin.repositories.firebase.FirebaseFirestoreRepositoryImpl
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepositoryImpl
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepositoryImpl
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepositoryImpl
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
    abstract fun bindCurrentContactIdRepository(impl: CurrentContactIdRepositoryImpl): CurrentContactIdRepository

    @Binds
    @Singleton
    abstract fun bindInsertContactRepository(impl: CreateContactRepositoryImpl): CreateContactRepository

    @Binds
    @Singleton
    abstract fun bindEditContactRepository(impl: EditContactRepositoryImpl): EditContactRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    @Binds
    @Singleton
    abstract fun bindCreateGroupRepository(impl: ManageGroupRepositoryImpl): ManageGroupRepository

    @Binds
    @Singleton
    abstract fun bindGroupsListRepository(impl: GroupsListRepositoryImpl): GroupsListRepository

    @Binds
    @Singleton
    abstract fun bindFirebaseFirestoreRepository(impl: FirebaseFirestoreRepositoryImpl): FirebaseFirestoreRepository
}