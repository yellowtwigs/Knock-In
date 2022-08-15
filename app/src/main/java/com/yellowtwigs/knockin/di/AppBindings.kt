package com.yellowtwigs.knockin.di

import com.yellowtwigs.knockin.repositories.contacts.insert.InsertContactRepository
import com.yellowtwigs.knockin.repositories.contacts.insert.InsertContactRepositoryImpl
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepositoryImpl
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
    abstract fun bindInsertContactRepository(impl: InsertContactRepositoryImpl): InsertContactRepository
}