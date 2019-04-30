package com.example.firsttestknocker

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context

@Database(entities = [Contacts::class, Notifications::class], version = 6)
 abstract  class ContactsRoomDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun notificationsDao(): NotificationsDao
    companion object {
        private var INSTANCE: ContactsRoomDatabase? = null

        //creation de la base de données
        fun getDatabase(context: Context): ContactsRoomDatabase? {
            if (INSTANCE != null) {
                return INSTANCE
            }
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        ContactsRoomDatabase::class.java,
                        "Contact_database")
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .addMigrations(MIGRATION_4_5)
                        .addMigrations(MIGRATION_5_6)
                        .allowMainThreadQueries()
                        .build()
                return INSTANCE
            }
        }
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN mail TEXT DEFAULT '' NOT NULL")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS 'notifications_table' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'title' TEXT NOT NULL, 'contact_name' TEXT NOT NULL, 'description' TEXT NOT NULL, 'platform' TEXT NOT NULL)")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN contact_priority INTEGER DEFAULT 0 NOT NULL ")
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN contact_priority INTEGER DEFAULT 0 NOT NULL ")
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN is_blacklist INTEGER DEFAULT 0 NOT NULL ")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN profile_picture_str TEXT DEFAULT '' NOT NULL")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN date_time TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN timestamp INTEGER DEFAULT 0 NOT NULL")
            }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}