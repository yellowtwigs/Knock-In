package com.example.firsttestknocker

import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.ContentUris
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.io.InputStream


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

    fun getPhoneNumber(main_contentResolver: ContentResolver): List<Triple<Int,String,String?>>? {
        val contactPhoneNumber = arrayListOf<Triple<Int,String,String?>>()
        var idAndPhoneNumber: Triple<Int,String,String?>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            val phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            var phonePic = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (phonePic != null) {
                phonePic = bitmapToBase64(BitmapFactory.decodeStream(openPhoto(phoneId!!.toLong(),main_contentResolver)))
            }
            idAndPhoneNumber = Triple(phoneId!!.toInt(), phoneNumber!!, phonePic)
            if (contactPhoneNumber.isEmpty()) {
                contactPhoneNumber.add(idAndPhoneNumber)
            } else if (!isDuplicate(idAndPhoneNumber,contactPhoneNumber)){
                contactPhoneNumber.add(idAndPhoneNumber)
            }
        }
        phonecontact?.close()
        return contactPhoneNumber
    }

    fun openPhoto(contactId: Long, main_contentResolver: ContentResolver): InputStream? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        val cursor = main_contentResolver.query(photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null) ?: return null
        try {
            if (cursor.moveToFirst()) {
                val data = cursor.getBlob(0)
                if (data != null) {
                    return ByteArrayInputStream(data)
                }
            }
        } finally {
            cursor.close()
        }
        return null
    }

    fun getAllContacsInfo(main_contentResolver: ContentResolver): List<Contacts>? {
        val phoneContactsList = null
        val contactNumberAndPic: List<Triple<Int,String,String?>>?
        contactNumberAndPic = getPhoneNumber(main_contentResolver)
        //get Structured name CONTACT
        //merge 3 list into one list<Contact> CONTACTS
        return phoneContactsList
    }

    private fun isDuplicate(idAndPhoneNumber: Triple<Int,String,String?>, contactPhoneNumber:List<Triple<Int,String,String?>>): Boolean{
        //println("/////////////////////////////  "+ contactPhoneNumber.size)
        contactPhoneNumber.forEach {
           // println("ID = "+it.first+" NUMBER = "+it.second)
            if (it.first == idAndPhoneNumber.first)
                return true
        }
        return false
    }

    private fun isDuplicate(contact: String, contactsList:List<Contacts>): Boolean {
        contactsList.forEach {
            if (it.lastName == "" && it.firstName == contact || it.firstName + " " + it.lastName == contact)
                return true
        }
        return false
    }

    fun bitmapToBase64(bitmap: Bitmap) : String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}