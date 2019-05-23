package com.example.knocker.model

import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.widget.GridView
import com.example.knocker.R
import com.example.knocker.controller.ContactAdapter
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import java.io.ByteArrayInputStream
import java.io.InputStream


object ContactSync : AppCompatActivity() {

    fun getPhoneNumber(main_contentResolver: ContentResolver): List<Triple<Int, String?, String?>>? {
        val contactPhoneNumber = arrayListOf<Triple<Int, String?, String?>>()
        var idAndPhoneNumber: Triple<Int, String?, String?>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            var phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            var phonePic = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (phoneNumber == null)
                phoneNumber = ""
            if (phonePic == null || phonePic.contains("content://com.android.contacts/contacts/", ignoreCase = true)) {
                phonePic = ""
            } else {
                println("phone pic" + phonePic)
                println("openPhoto " + phoneId!!.toLong() + " main content resolver " + main_contentResolver) // huwei content://com.android.contacts/contacts/1600/photo
                phonePic = bitmapToBase64(BitmapFactory.decodeStream(openPhoto(phoneId.toLong(), main_contentResolver)))
            }
            idAndPhoneNumber = Triple(phoneId!!.toInt(), phoneNumber, phonePic)
            if (contactPhoneNumber.isEmpty()) {
                //println("1er = "+idAndPhoneNumber)
                contactPhoneNumber.add(idAndPhoneNumber)
            } else if (!isDuplicateNumber(idAndPhoneNumber, contactPhoneNumber)) {
                //println("AND = "+idAndPhoneNumber)
                contactPhoneNumber.add(idAndPhoneNumber)
            }
        }
        phonecontact?.close()
        println("number ?= "+ contactPhoneNumber)
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

    fun getStructuredName(main_contentResolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>>? {
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
                    idAndName = Pair(phone_id, StructName)
                    phoneContactsList.add(idAndName)
                } else if (!isDuplicate(phone_id, phoneContactsList) && mimeType == "vnd.android.cursor.item/name") {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    StructName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phone_id, StructName)
                    phoneContactsList.add(idAndName)
                }
            }
        }
        phonecontact?.close()
        println("ok ?= "+ phoneContactsList)
        return phoneContactsList
    }



    fun getAllContacsInfo(main_contentResolver: ContentResolver, gridView: GridView?, applicationContext: Context) {
        val phoneStructName = getStructuredName(main_contentResolver)
        val contactNumberAndPic = getPhoneNumber(main_contentResolver)
        val phoneContactsList = createListContacts(phoneStructName,contactNumberAndPic,gridView,applicationContext)
    }

    private fun isDuplicateNumber(idAndPhoneNumber: Triple<Int, String?, String?>, contactPhoneNumber: List<Triple<Int, String?, String?>>): Boolean {
        contactPhoneNumber.forEach {
            if (it.first == idAndPhoneNumber.first || it.second == idAndPhoneNumber.second)
                return true
        }
        return false
    }

    private fun isDuplicate(id: Int, contactPhoneNumber: List<Pair<Int, Triple<String, String, String?>>>): Boolean {
        contactPhoneNumber.forEach {
            if (it.first == id)
                return true
        }
        return false
    }

    private fun isDuplicate(contact: String, contactsList:List<ContactDB>): Boolean {
        contactsList.forEach {
            if (it.lastName == "" && it.firstName == contact || it.firstName + " " + it.lastName == contact)
                return true
        }
        return false
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }//TODO:redondant

    fun createListContacts(phoneStructName: List<Pair<Int, Triple<String, String, String>>>?, contactNumberAndPic: List<Triple<Int, String?, String?>>?, gridView: GridView?, applicationContext: Context) {
        var NewSyncStr = ""
        val phoneContactsList = arrayListOf<ContactDB>()
        var main_ContactsDatabase: ContactsRoomDatabase? = null
        lateinit var main_mDbWorkerThread: DbWorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        val addAllContacts = Runnable {
            val allcontacts = main_ContactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
            phoneStructName!!.forEach { fullName ->
                contactNumberAndPic!!.forEach { numberPic ->
                    val contactDetails = listOf(ContactDetailDB(null, null, numberPic.second!! + "M", "phone", 0), ContactDetailDB(null, null, "B", "phone", 0))
                    if (fullName.first == numberPic.first) {
                        if (fullName.second.second == "") {
                            // val contact = ContactDB(null, fullName.second.first, fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            val contacts = ContactDB(null, fullName.second.first, fullName.second.third, R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            if (!ContactSync.isDuplicate(allcontacts, contacts)) {

                                contacts.id = main_ContactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                for (details in contactDetails) {
                                    details.idContact = contacts.id
                                }
                                main_ContactsDatabase?.contactsDao()?.insertDetails(contactDetails)
                                //main_ContactsDatabase?.contactsDao()?.insertDetailsForContact(contacts,contactDetails)
                                //   main_ContactsDatabase?.contactsDao()?.insertContactDetailSync(numberPic.second!! + "P","phone")
                            }
                            phoneContactsList.add(contacts)
                        } else if (fullName.second.second != "") {
                            //val contact = ContactDB(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            val contacts = ContactDB(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            phoneContactsList.add(contacts)
                            if (!ContactSync.isDuplicate(allcontacts, contacts)) {
                                contacts.id = main_ContactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                for (details in contactDetails) {
                                    details.idContact = contacts.id
                                }
                                main_ContactsDatabase?.contactsDao()?.insertDetails(contactDetails)
                            }
                        } else {
                        }
                    }
                }
            }

            val syncContact= main_ContactsDatabase?.contactsDao()?.getContactAllInfo()
            val lastSyncList = getLastSync(applicationContext)
            if (lastSyncList.isEmpty()) {
                storeLastSync(phoneContactsList, applicationContext,lastSyncList, true)
            } else {
                deleteDeletedContactFromPhone(lastSyncList, phoneContactsList)
                storeLastSync(phoneContactsList, applicationContext,lastSyncList, false)
            }
            val sharedPreferences = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val len = sharedPreferences.getInt("gridview", 4)
            val contactAdapter = ContactAdapter(applicationContext, syncContact, len)
            gridView!!.adapter = contactAdapter
        }
        runOnUiThread(addAllContacts)
    }

    fun storeLastSync(contactsList: List<ContactDB>, applicationContext:Context, lastSync: List<Pair<Int,String>>,isFirstTime: Boolean) {
        val sharedPreferences = applicationContext.getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        var name = ""
        if (isFirstTime == true) {
            contactsList.forEach {
                name += it.id.toString() + ":" + it.firstName + " " + it.lastName + "|"
            }
            edit.putString("last_sync", name)
            edit.apply()
        }else{
            contactsList.forEach {
                if (it.id==null) {
                    var i = 0
                    while (i != (lastSync.size)-1 && it.firstName + " " + it.lastName != lastSync[i].second){
                        i++
                    }
                    if (i != (lastSync.size) && it.firstName + " " + it.lastName == lastSync[i].second){
                        name += lastSync[i].first.toString() + ":" + it.firstName + " " + it.lastName + "|"
                    }
                }else{
                    name += it.id.toString() + ":" + it.firstName + " " + it.lastName + "|"
                }
            }
            edit.putString("last_sync", name)
            edit.apply()
        }
    }

    fun getLastSync(applicationContext: Context): List<Pair<Int,String>> {
        val lastSyncList = arrayListOf<Pair<Int,String>>()
        var idAndName: Pair<Int,String>
        val sharedPreferences = applicationContext.getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
        val lastSync = sharedPreferences.getString("last_sync","")
        if (lastSync != null && lastSync != "" && lastSync.contains("|")) {
            val lastSyncSplit = lastSync.split("|")
            lastSyncSplit.forEach {
                if (it != "") {
                    val idAndNameSplit = it.split(":")
                    idAndName = Pair(idAndNameSplit[0].toInt(), idAndNameSplit[1])
                    lastSyncList.add(idAndName)
                }
            }
        } else if (lastSync != null && lastSync != "") {
            val idAndNameSplit = lastSync.split(":")
            idAndName = Pair(idAndNameSplit[0].toInt(), idAndNameSplit[1])
            lastSyncList.add(idAndName)
        }
        return lastSyncList
    }

    fun deleteDeletedContactFromPhone(lastSync: List<Pair<Int,String>>, newSync: List<ContactDB>) {
        var main_ContactsDatabase: ContactsRoomDatabase? = null
        var id: Int? = null
        lateinit var main_mDbWorkerThread: DbWorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        var isDelete = true
        lastSync.forEach { old ->
            isDelete = true
            newSync.forEach {
                if (it.firstName+" "+it.lastName == old.second)
                    isDelete = false
            }
            if (isDelete == true) {
                println("NEW = "+old.second+"   id = "+old.first)
                main_ContactsDatabase?.contactsDao()?.deleteContactById(old.first)
            }
        }
    }

    private fun isDuplicate(contacts:List<ContactWithAllInformation>?, phoneContactList: ContactDB):Boolean{
        contacts?.forEach { contactsInfo ->
            val contactsDB=contactsInfo.contactDB!!
            if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                return true
        }
        return false//TODO
    }


    private fun randomDefaultImage(): Int {

        val nextValues = kotlin.random.Random.nextInt(0, 4)

        if (nextValues == 0) {
            return R.drawable.ic_man_user_blue
        } else if (nextValues == 1) {
            return R.drawable.ic_man_user_green
        } else if (nextValues == 2) {
            return R.drawable.ic_man_user_purple
        } else if (nextValues == 3) {
            return R.drawable.ic_man_user_pink
        } else if (nextValues == 4) {
            return R.drawable.ic_man_user_brown
        }

        return 0
    }
}