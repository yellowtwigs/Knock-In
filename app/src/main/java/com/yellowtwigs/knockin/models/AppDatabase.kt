package com.yellowtwigs.knockin.models

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yellowtwigs.knockin.models.data.*
import com.yellowtwigs.knockin.models.dao.*

/**
 * La Classe qui permet de créer la base de données et de la garder à jour
 * @author Ryan Granet
 */
@Database(
    entities = [Contact::class, NotificationDB::class, GroupDB::class, ContactDetailDB::class, LinkContactGroup::class, VipNotificationsDB::class, VipSbnDB::class],
    version = 18
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