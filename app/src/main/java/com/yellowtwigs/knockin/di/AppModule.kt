package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.yellowtwigs.knockin.model.ContactsDatabase
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.notifications.listener.NotificationsListenerViewModel
import dagger.Binds
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
}