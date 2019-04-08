package com.example.firsttestknocker

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [Contacts::class], version = 1)
abstract class ContactsRoomDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
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
                        "Contact_database"
                ).build()
                return INSTANCE
            }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}