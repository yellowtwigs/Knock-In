package com.example.firsttestknocker

import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.content.*

object ContactSync : AppCompatActivity() {
    fun getAllContact(main_contentResolver: ContentResolver): List<Contacts>? {
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        val phoneContactsList = arrayListOf<Contacts>()
        while (phonecontact.moveToNext()) {
            val fullName = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (phoneContactsList.isEmpty()) {
                var lastName = ""
                if (fullName!!.contains(' '))
                    lastName = fullName.substringAfter(' ')
                val contactData = Contacts(null, fullName.substringBefore(' '), lastName, phoneNumber!!+"P", "", R.drawable.ryan, R.drawable.aquarius, 0, "")
                phoneContactsList.add(contactData)
            } else if (!isDuplicate(fullName!!, phoneContactsList)) {
                var lastName = ""
                if (fullName.contains(' '))
                    lastName = fullName.substringAfter(' ')
                val contactData = Contacts(null, fullName.substringBefore(' '), lastName, phoneNumber!!+"P", "", R.drawable.ryan, R.drawable.aquarius, 0, "")
                phoneContactsList.add(contactData)
            }
        }
        phonecontact?.close()
        return phoneContactsList
    }
    
    //compare le contact données avec tous ceux de la Database
    private fun isDuplicate(contact: String, contactsList: List<Contacts>): Boolean {
        contactsList.forEach {
            if (it.lastName == "" && it.firstName == contact || it.firstName + " " + it.lastName == contact)
                return true
        }
        return false
    }
}