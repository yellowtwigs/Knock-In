package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.yellowtwigs.knockin.model.AppDatabase
import com.yellowtwigs.knockin.model.dao.ContactDetailsDao
import com.yellowtwigs.knockin.model.dao.ContactsDao
import com.yellowtwigs.knockin.model.dao.GroupsDao
import com.yellowtwigs.knockin.model.dao.LinkContactGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            ContactsDatabase::class.java,
            "contact_database"
        )
            .allowMainThreadQueries()
            .addMigrations()
            .build()

    @Provides
    fun provideContactsDao(database: ContactsDatabase) = database.contactsDao()

    @Provides
    fun provideContactsRepository(dao: ContactsDao) = ContactsRepository(dao)

    @Provides
    fun provideContactDetailsDao(database: ContactsDatabase) = database.contactDetailsDao()

    @Provides
    fun provideContactDetailsRepository(dao: ContactDetailsDao) = ContactDetailsRepository(dao)

    @Provides
    fun provideGroupsDao(database: ContactsDatabase) = database.GroupsDao()

    @Provides
    fun provideGroupsRepository(dao: GroupsDao) = GroupsRepository(dao)

    @Provides
    fun provideLinkContactGroupDao(database: ContactsDatabase) = database.LinkContactGroupDao()

    @Provides
    fun provideLinkContactGroupRepository(dao: LinkContactGroupDao) = LinkContactGroupRepository(dao)
}