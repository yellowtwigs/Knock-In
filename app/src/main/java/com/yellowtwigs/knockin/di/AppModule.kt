package com.suonk.coyali_test_app.di

import android.content.Context
import androidx.room.Room
import com.suonk.coyali_test_app.models.AppDatabase
import com.suonk.coyali_test_app.models.dao.MovieDao
import com.suonk.coyali_test_app.repositories.DefaultRepository
import com.suonk.coyali_test_app.repositories.MainRepository
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
    fun provideMovieDao(database: AppDatabase) = database.movieDao()

    @Provides
    fun provideRepository(movieDao: MovieDao) : DefaultRepository = MainRepository(movieDao)
}