package com.example.knocker.model

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import com.example.knocker.model.ModelDB.*
import com.example.knocker.model.requestDB.*

/**
 * La Classe qui permet de créer la base de données et de la garder à jour
 * @author Ryan Granet
 */
@Database(entities = [ContactDB::class, NotificationDB::class, GroupDB::class, ContactDetailDB::class,LinkContactGroup::class], version = 10)
 abstract  class ContactsRoomDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun contactDetailsDao(): ContactDetailsDao
    abstract fun GroupsDao(): GroupsDao
    abstract fun LinkContactGroupDao(): LinkContactGroupDao
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
                        .addMigrations(MIGRATION_6_7)
                        .addMigrations(MIGRATION_7_8)
                        .addMigrations(MIGRATION_8_9)
                        .addMigrations(MIGRATION_9_10)
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
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS 'groups_table' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT NOT NULL, 'members' TEXT NOT NULL, 'nb_members' INTEGER DEFAULT 0 NOT NULL, 'profile_picture_str' TEXT NOT NULL)")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN is_cancellable INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN app_image TEXT DEFAULT '' NOT NULL")
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS 'contact_details_table' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'id_contact' INTEGER NOT NULL, 'contact_details' TEXT NOT NULL, 'tag' TEXT NOT NULL, FOREIGN KEY('id_contact') REFERENCES contact_details_table('id'))")
                database.execSQL("CREATE TABLE IF NOT EXISTS 'link_contact_group_table' ('id_group' INTEGER NOT NULL, 'id_contact' INTEGER NOT NULL, PRIMARY KEY('id_group','id_contact'),  FOREIGN KEY('id_contact') REFERENCES contact_details_table('id'), FOREIGN KEY('id_group') REFERENCES groups_table('id'))")
            }
        }
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN id_contact INTEGER DEFAULT 0 NOT NULL")
            }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}