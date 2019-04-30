package com.example.firsttestknocker

import android.support.v7.app.AppCompatActivity

object ContactInfo : AppCompatActivity() {

    private var main_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var main_mDbWorkerThread: DbWorkerThread

    fun getInfoWithName(name: String, platform: String): String {
        // on init WorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()

        //on get la base de données
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)

        val listContact = main_ContactsDatabase?.contactsDao()?.getAllContacts()

        var info = "Platform Error"
        when (platform) {
            "message" -> {
                info = getPhoneNumberWithName(name, listContact)
            }
        }
        return info
    }

    fun getPhoneNumberWithName(name: String, listContact: List<Contacts>?): String {
        var info = "Name Error"
        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName+" "+dbContact.lastName == name) {
                    info = dbContact.phoneNumber.dropLast(1)
                }
            }
        } else {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
                    info = dbContact.phoneNumber.dropLast(1)
                }
            }
        }
        return info
    }
}