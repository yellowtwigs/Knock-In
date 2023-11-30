package com.yellowtwigs.knockin.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.yellowtwigs.knockin.background.DemoWorkerDependencies
import com.yellowtwigs.knockin.background.alarm.AlarmManagerHelper
import com.yellowtwigs.knockin.background.service.CheckDuplicateNotificationUseCase
import com.yellowtwigs.knockin.domain.contact.*
import com.yellowtwigs.knockin.domain.group.UpdateFavoriteGroupUseCase
import com.yellowtwigs.knockin.domain.notifications.*
import com.yellowtwigs.knockin.model.database.ContactsDatabase
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.groups.list.GroupsListRepository
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context) = context

    @Provides
    @Singleton
    fun provideDemoWorkerDependencies(): DemoWorkerDependencies {
        return DemoWorkerDependencies()
    }

    @Provides
    @Singleton
    fun provideAlarmManagerHelper(@ApplicationContext context: Context): AlarmManagerHelper {
        return AlarmManagerHelper(context)
    }

    @Provides
    @Singleton
    fun provideContactsDao(database: ContactsDatabase) = database.contactsDao()

    @Provides
    @Singleton
    fun provideNotificationsDao(database: ContactsDatabase) = database.notificationsDao()

    @Provides
    @Singleton
    fun provideGroupsDao(database: ContactsDatabase) = database.groupsDao()

    @Provides
    @Singleton
    fun provideGetAllContactsSortByFullNameUseCase(contactsListRepository: ContactsListRepository) =
        GetAllContactsSortByFullNameUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideGetAllContactsSortByFavoriteUseCase(contactsListRepository: ContactsListRepository) =
        GetAllContactsSortByFavoriteUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideUpdateContactPriorityByIdUseCase(contactsListRepository: ContactsListRepository) =
        UpdateContactPriorityByIdUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideGetNumbersContactsVipUseCase(contactsListRepository: ContactsListRepository) =
        GetNumbersContactsVipUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideDeleteContactUseCase(contactsListRepository: ContactsListRepository) =
        DeleteContactUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideGetAllContactsVipUseCase(contactsListRepository: ContactsListRepository) =
        GetAllContactsVipUseCase(contactsListRepository)

    @Provides
    @Singleton
    fun provideUpdateFavoriteGroupUseCase(
        groupsListRepository: GroupsListRepository,
        manageGroupRepository: ManageGroupRepository
    ) =
        UpdateFavoriteGroupUseCase(groupsListRepository, manageGroupRepository)

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
            saveNotification = SaveNotification(notificationsRepository),
            checkDuplicateNotificationUseCase = CheckDuplicateNotificationUseCase(notificationsRepository)
        )
    }
}