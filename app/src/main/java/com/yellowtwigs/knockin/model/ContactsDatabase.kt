package com.yellowtwigs.knockin.model

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import androidx.room.TypeConverters
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.model.dao.*


@Database(
    entities = [ContactDB::class, NotificationDB::class, GroupDB::class, LinkContactGroup::class, VipSbnDB::class],
    version = 20
)
@TypeConverters(Converters::class)
abstract class ContactsDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun GroupsDao(): GroupsDao
    abstract fun VipSbnDao(): VipSbnDao

    companion object {
        private var INSTANCE: ContactsDatabase? = null

        fun getDatabase(context: Context): ContactsDatabase? {
            if (INSTANCE != null) {
                return INSTANCE
            }
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    ContactsDatabase::class.java,
                    "Contact_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_8)
                    .addMigrations(MIGRATION_8_9)
                    .addMigrations(MIGRATION_9_10)
                    .addMigrations(MIGRATION_10_11)
                    .addMigrations(MIGRATION_11_12)
                    .addMigrations(MIGRATION_12_13)
                    .addMigrations(MIGRATION_13_14)
                    .addMigrations(MIGRATION_14_15)
                    .addMigrations(MIGRATION_15_16)
                    .addMigrations(MIGRATION_16_17)
                    .addMigrations(MIGRATION_17_18)
                    .addMigrations(MIGRATION_18_19)
                    .addMigrations(MIGRATION_19_20)
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
                database.execSQL("CREATE TABLE IF NOT EXISTS 'link_contact_group_table' ('id_group' INTEGER NOT NULL, 'id_contact' INTEGER NOT NULL, PRIMARY KEY('id_group','id_contact'), FOREIGN KEY('id_group') REFERENCES groups_table('id'), FOREIGN KEY('id_contact') REFERENCES contact_details_table('id'))")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications_table " + " ADD COLUMN id_contact INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN is_favorite INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE groups_table " + " ADD COLUMN section_color INTEGER DEFAULT -500074 NOT NULL")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN mail_name TEXT DEFAULT '' NOT NULL")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN messenger_id TEXT DEFAULT '' NOT NULL")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN has_whatsapp INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS 'vip_notifications_table' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'notification_id' INTEGER NOT NULL, 'app_notifier' TEXT NOT NULL, 'list_size' INTEGER NOT NULL, 'notification_text' TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS 'vip_sbn_table' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'vip_notification_id' INTEGER NOT NULL, 'sbn_key' TEXT NOT NULL, 'sbn_value' TEXT NOT NULL)")
            }
        }
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN notification_tone TEXT DEFAULT '' NOT NULL")
            }
        }
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN notification_Sound INTEGER DEFAULT 0 NOT NULL")
            }
        }
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN audio_file_name TEXT DEFAULT '' NOT NULL")
            }
        }
        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN has_telegram INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE contacts_table " + " ADD COLUMN has_signal INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }
}