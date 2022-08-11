package com.yellowtwigs.knockin.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yellowtwigs.knockin.model.dao.*
import com.yellowtwigs.knockin.model.data.*

@Database(
    entities = [ContactDB::class, NotificationDB::class, GroupDB::class, ContactDetailDB::class, LinkContactGroup::class, VipNotificationsDB::class, VipSbnDB::class],
    version = 20
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun contactDetailsDao(): ContactDetailsDao

    abstract fun GroupsDao(): GroupsDao
    abstract fun LinkContactGroupDao(): LinkContactGroupDao

    abstract fun VipNotificationsDao(): VipNotificationsDao
    abstract fun VipSbnDao(): VipSbnDao
}