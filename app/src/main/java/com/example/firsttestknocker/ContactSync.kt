package com.example.firsttestknocker

import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.content.*

object ContactSync : AppCompatActivity() {
    fun getAllContact(main_contentResolver: ContentResolver): List<Contacts>? {
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        val phoneContactsList = arrayListOf<Contacts>()
        while (phonecontact.moveToNext()) {
            val phone_id = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            val fullName = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            println("\n//////////////\n"+fullName+"\n"+phoneNumber)
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

    fun getPhoneNumber(main_contentResolver: ContentResolver): List<Pair<Int,String>>? {
        val contactPhoneNumber = arrayListOf<Pair<Int,String>>()
        var idAndPhoneNumber: Pair<Int,String>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            val phone_id = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            val phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            idAndPhoneNumber = Pair(phone_id!!.toInt(), phoneNumber!!)
            if (contactPhoneNumber.isEmpty()) {
                contactPhoneNumber.add(idAndPhoneNumber)
            } else if (!isDuplicate(idAndPhoneNumber,contactPhoneNumber)){
                contactPhoneNumber.add(idAndPhoneNumber)
            }
        }
        phonecontact?.close()
        return contactPhoneNumber
    }

    fun getPicture(main_contentResolver: ContentResolver): List<Pair<Int,String>>? {
        val contactPicture = arrayListOf<Pair<Int,String>>()
        //var idAndPicture
        return contactPicture
    }

    fun getAllContacsInfo(main_contentResolver: ContentResolver): List<Contacts>? {
        val phoneContactsList = null
        val contactPhoneNumber: List<Pair<Int,String>>?
        val contactPicture: List<Pair<Int,String>>?
        contactPhoneNumber = getPhoneNumber(main_contentResolver)
        contactPicture = getPicture(main_contentResolver)
        //get la photo PAIR
        //get Structured name CONTACT
        //merge 3 list into one list<Contact> CONTACTS
        return phoneContactsList
    }

    private fun isDuplicate(idAndPhoneNumber: Pair<Int,String>, contactPhoneNumber:List<Pair<Int,String>>): Boolean{
        println("/////////////////////////////  "+ contactPhoneNumber.size)
        contactPhoneNumber.forEach {
            println("ID = "+it.first+" NUMBER = "+it.second)
            if (it.first == idAndPhoneNumber.first)
                return true
        }
        return false
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