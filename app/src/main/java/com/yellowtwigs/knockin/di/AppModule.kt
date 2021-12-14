package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.yellowtwigs.knockin.models.AppDatabase
import com.yellowtwigs.knockin.models.dao.ContactsDao
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
            AppDatabase::class.java,
            "app_database"
        )
            .allowMainThreadQueries()
            .addMigrations()
            .build()

    @Provides
    fun provideContactsDao(database: AppDatabase) = database.contactsDao()

    @Provides
    fun provideContactDetailsDao(database: AppDatabase) = database.contactDetailsDao()

    @Provides
    fun provideContactsRepository(contactsDao: ContactsDao): DefaultRepository = MainRepository(contactsDao)
}