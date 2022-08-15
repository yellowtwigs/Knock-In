package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.yellowtwigs.knockin.model.ContactsDatabase
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

}