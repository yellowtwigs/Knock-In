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
import android.view.View
import android.widget.GridView
import com.example.knocker.R
import com.example.knocker.controller.ContactAdapter
import java.io.ByteArrayInputStream
import java.io.InputStream


object ContactSync : AppCompatActivity() {
    
    fun getPhoneNumber(main_contentResolver: ContentResolver): List<Triple<Int,String?,String?>>? {
        val contactPhoneNumber = arrayListOf<Triple<Int,String?,String?>>()
        var idAndPhoneNumber: Triple<Int,String?,String?>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            var phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            var phonePic = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (phoneNumber == null)
                phoneNumber = ""
            if (phonePic == null || phonePic.contains("content://com.android.contacts/contacts/", ignoreCase = true)){
                phonePic = ""
            } else {
                println("phone pic"+phonePic)
                println("openPhoto "+phoneId!!.toLong()+" main content resolver "+main_contentResolver) // huwei content://com.android.contacts/contacts/1600/photo
                phonePic = bitmapToBase64(BitmapFactory.decodeStream(openPhoto(phoneId.toLong(), main_contentResolver)))
            }
            idAndPhoneNumber = Triple(phoneId!!.toInt(), phoneNumber, phonePic)
            if (contactPhoneNumber.isEmpty()) {
                //println("1er = "+idAndPhoneNumber)
                contactPhoneNumber.add(idAndPhoneNumber)
            } else if (!isDuplicateNumber(idAndPhoneNumber, contactPhoneNumber)){
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



    fun getAllContacsInfo(main_contentResolver: ContentResolver,gridView: GridView?) {
        val phoneStructName = getStructuredName(main_contentResolver)
        val contactNumberAndPic = getPhoneNumber(main_contentResolver)
        val phoneContactsList = createListContacts(phoneStructName,contactNumberAndPic,gridView)
    }

    private fun isDuplicateNumber(idAndPhoneNumber: Triple<Int,String?,String?>, contactPhoneNumber:List<Triple<Int,String?,String?>>): Boolean{
        //println("/////////////////////////////  "+ contactPhoneNumber.size)
        contactPhoneNumber.forEach {
           // println("ID = "+it.first+" NUMBER = "+it.second)
            if (it.first == idAndPhoneNumber.first || it.second == idAndPhoneNumber.second)
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
    }//TODO:redondant

    fun createListContacts(phoneStructName: List<Pair<Int, Triple<String, String, String>>>?, contactNumberAndPic: List<Triple<Int,String?,String?>>?,gridView: GridView?) {
        val phoneContactsList = arrayListOf<Contacts>()
        var main_ContactsDatabase: ContactsRoomDatabase? = null
        lateinit var main_mDbWorkerThread: DbWorkerThread
        main_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        main_mDbWorkerThread.start()
        main_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        val addAllContacts = Runnable {
            val allcontacts = main_ContactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
            phoneStructName!!.forEach { fullName ->
                contactNumberAndPic!!.forEach { numberPic ->
                    val contactDetails=listOf(ContactDetails(null,null,numberPic.second!! + "P","phone",0))
                    p
                    if (fullName.first == numberPic.first) {
                        if (fullName.second.second == "") {
                            // val contact = Contacts(null, fullName.second.first, fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            val contacts = Contacts(null, fullName.second.first, fullName.second.third, R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            if(!ContactSync.isDuplicate(allcontacts,contacts)){

                                contacts.id=main_ContactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                for(details in contactDetails){
                                    details.idContact=contacts.id
                                }
                                main_ContactsDatabase?.contactsDao()?.insertDetails(contactDetails)
                                //main_ContactsDatabase?.contactsDao()?.insertDetailsForContact(contacts,contactDetails)
                             //   main_ContactsDatabase?.contactsDao()?.insertContactDetailSync(numberPic.second!! + "P","phone")
                                println("contact"+contacts)
                                println("liste de contact"+main_ContactsDatabase?.contactsDao()?.getAllContacts())
                            }
                            phoneContactsList.add(contacts)
                        } else if (fullName.second.second != "") {
                            //val contact = Contacts(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, numberPic.second!! + "P", "", R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            val contacts = Contacts(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, R.drawable.ryan, R.drawable.aquarius, 1, numberPic.third!!)
                            phoneContactsList.add(contacts)
                            if(!ContactSync.isDuplicate(allcontacts,contacts)){
                                contacts.id=main_ContactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                for(details in contactDetails){
                                    details.idContact=contacts.id
                                }
                                main_ContactsDatabase?.contactsDao()?.insertDetails(contactDetails)
                            }
                        }else{
                        }
                    }
                }
                println("liste de détail"+main_ContactsDatabase?.contactDetailsDao()?.getAllDetails())
                //val syncContact = main_ContactsDatabase?.contactsDao()?.getAllContacts()
                //val sharedPreferences = applicationContext.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                //val len = sharedPreferences.getInt("gridview", 4)
                //val contactAdapter = ContactAdapter(this, syncContact, len)
                //gridView!!.adapter = contactAdapter
                //view!!.adapter = contactAdapter

            }
        }
        runOnUiThread(addAllContacts)
    }
  /*  fun addContactsToView(phoneContactsList:List<Contacts>,,view: GridView) {
         var main_ContactsDatabase: ContactsRoomDatabase? = null
         lateinit var main_mDbWorkerThread: DbWorkerThread
        val addAllContacts = Runnable {
            var isDuplicate: Boolean
            val allcontacts = main_ContactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
            //val priority = ContactsPriority.getPriorityWithName("Ryan Granet", "sms", allcontacts)
            //println("priorité === "+priority)
            phoneContactsList?.forEach { phoneContactList ->
                isDuplicate = false
                allcontacts?.forEach { contactsDB ->
                    //println("LOOOOOOOOOOOOP "+ contactsDB)
                    if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                        isDuplicate = true
                    println("STATE = " + isDuplicate) //////////
                }//TODO
                if (isDuplicate == false) {
                    val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    val len = sharedPreferences.getInt("gridview", 4)
                    println("PLSSSSSSSSSSSSSS " + phoneContactList)
                    main_ContactsDatabase?.contactsDao()?.insert(phoneContactList)



                }
            }
        }
        runOnUiThread(addAllContacts)
    }*/
    private fun isDuplicate(contacts:List<Contacts>?,phoneContactList:Contacts):Boolean{
        contacts?.forEach { contactsDB ->
            //println("LOOOOOOOOOOOOP "+ contactsDB)
            if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                return true
        }
        return false//TODO
    }
}