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

    fun getPhoneNumber(main_contentResolver: ContentResolver): List<Triple<Int,String?,String?>>? {
        val contactPhoneNumber = arrayListOf<Triple<Int,String?,String?>>()
        var idAndPhoneNumber: Triple<Int,String?,String?>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            var phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            var phonePic = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (phoneNumber == null)
                phoneNumber = ""
            if (phonePic != null) {
                println("phone pic"+phonePic)
                println("openPhoto "+phoneId!!.toLong()+" main content resolver "+main_contentResolver)
                phonePic = bitmapToBase64(BitmapFactory.decodeStream(openPhoto(phoneId!!.toLong(),main_contentResolver)))
            } else {
                phonePic = ""
            }
            idAndPhoneNumber = Triple(phoneId!!.toInt(), phoneNumber, phonePic)
            if (contactPhoneNumber.isEmpty()) {
                //println("1er = "+idAndPhoneNumber)
                contactPhoneNumber.add(idAndPhoneNumber)
            } else if (!isDuplicate(idAndPhoneNumber,contactPhoneNumber)){
                //println("AND = "+idAndPhoneNumber)
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

    fun getStructuredName(main_contentResolver: ContentResolver):  List<Pair<Int, Triple<String, String, String>>>? {
        val phoneContactsList = arrayListOf<Pair<Int, Triple<String, String, String>>>()
        var idAndName: Pair<Int, Triple<String, String, String>>
        var StructName: Triple<String, String, String>
        val phonecontact = main_contentResolver.query(ContactsContract.Data.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
        if (phonecontact != null) {
            while (phonecontact.moveToNext()) {
                val phone_id = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)).toInt()
                var firstName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                var middleName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                var lastName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                val mimeType = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))
                if (phoneContactsList.isEmpty() && mimeType == "vnd.android.cursor.item/name") {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    StructName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phone_id,StructName)
                    phoneContactsList.add(idAndName)
                } else if (!isDuplicate(phone_id, phoneContactsList) && mimeType == "vnd.android.cursor.item/name") {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    StructName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phone_id,StructName)
                    phoneContactsList.add(idAndName)
                }
            }
        }
        phonecontact?.close()
        return phoneContactsList
    }

    fun createListContacts(phoneStructName: List<Pair<Int, Triple<String, String, String>>>?, contactNumberAndPic: List<Triple<Int,String?,String?>>?): List<Contacts> {
        val phoneContactsList = arrayListOf<Contacts>()
        //var contact: Contacts
        phoneStructName!!.forEach { fullName ->
            contactNumberAndPic!!.forEach { numberPic ->
                if (fullName.first == numberPic.first) {
                    if (fullName.second.second == "") {
                        println("FN = "+fullName.second.first+" lastName = "+fullName.second.third+" phoneNumber = "+numberPic.second!! + "P")
                        val contact = Contacts(null, fullName.second.first, fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                        phoneContactsList.add(contact)
                    } else if (fullName.second.second != "") {
                        val contact = Contacts(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                        phoneContactsList.add(contact)
                    }
                }
            }
        }
        return phoneContactsList
    }

    fun getAllContacsInfo(main_contentResolver: ContentResolver): List<Contacts>? {
        val phoneStructName = getStructuredName(main_contentResolver)
        val contactNumberAndPic = getPhoneNumber(main_contentResolver)
        val phoneContactsList = createListContacts(phoneStructName, contactNumberAndPic)
        return phoneContactsList
    }

    private fun isDuplicate(idAndPhoneNumber: Triple<Int,String?,String?>, contactPhoneNumber:List<Triple<Int,String?,String?>>): Boolean{
        //println("/////////////////////////////  "+ contactPhoneNumber.size)
        contactPhoneNumber.forEach {
           // println("ID = "+it.first+" NUMBER = "+it.second)
            if (it.first == idAndPhoneNumber.first)
                return true
        }
        return false
    }

    private fun isDuplicate(id: Int, contactPhoneNumber:List<Pair<Int,Triple<String, String, String?>>>): Boolean{
//        println("/////////////////////////////  "+ contactPhoneNumber.size)
//        println("TEST -> "+id+"--------->"+ contactPhoneNumber)
        contactPhoneNumber.forEach {
            if (it.first == id)
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