package com.example.knocker.model

import androidx.appcompat.app.AppCompatActivity

object GroupsUsefulFunctions : AppCompatActivity() {

    private var Groups_Useful_functions_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var Groups_Useful_functions_mDbWorkerThread: DbWorkerThread

    fun listToIdString(contactsList: List<Contacts>): String {
        var idString = ""
        contactsList.forEach {
            if (idString == "")
                idString = it.id.toString()
            else
                idString = idString + "," + it.id.toString()
        }
        return idString
    }

    fun stringIdToList(idString: String): List<Contacts> {
        Groups_Useful_functions_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        Groups_Useful_functions_mDbWorkerThread.start()

        Groups_Useful_functions_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        val contactsList = arrayListOf<Contacts>()
        var contact: Contacts?
        if (idString == "") {
            return contactsList
        } else if (idString.contains(",")) {
            val idList = idString.split(',')
            for (i in 0..idList.size-1) {
                contact = Groups_Useful_functions_ContactsDatabase?.contactsDao()?.getContact(idList[i].toInt())
                if (contact != null)
                    contactsList.add(contact)
            }
        } else {
            contact = Groups_Useful_functions_ContactsDatabase?.contactsDao()?.getContact(idString.toInt())
            contactsList.add(contact!!)
        }
        return contactsList
    }
}