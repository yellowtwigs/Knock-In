package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.yellowtwigs.knockin.domain.notifications.*
import com.yellowtwigs.knockin.model.database.ContactsDatabase
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.dao.NotificationsDao
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
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
    @Singleton
    fun provideContactsDao(database: ContactsDatabase) = database.contactsDao()

    @Provides
    @Singleton
    fun provideNotificationsDao(database: ContactsDatabase) = database.notificationsDao()

    @Singleton
    @Provides
    fun provideNotificationsListenerUseCases(
        contactsDao: ContactsDao,
        notificationsRepository: NotificationsRepository
    ): NotificationsListenerUseCases {
        return NotificationsListenerUseCases(
            getContactByName = GetContactByName(contactsDao),
            getContactByMail = GetContactByMail(contactsDao),
            getContactByPhoneNumber = GetContactByPhoneNumber(contactsDao),
            saveNotification = SaveNotification(notificationsRepository)
        )
    }
}