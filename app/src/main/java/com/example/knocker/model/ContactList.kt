package com.example.knocker.model

import android.content.Context
import com.example.knocker.R
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ContactList(var contacts: List<ContactWithAllInformation>,var context:Context){
    constructor(context: Context):this(mutableListOf<ContactWithAllInformation>()
            ,context)

    private lateinit var  mDbWorkerThread: DbWorkerThread
    private var contactsDatabase: ContactsRoomDatabase? = null
    init{
        mDbWorkerThread= DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        contactsDatabase= ContactsRoomDatabase.getDatabase(context)
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb= Callable {
            contactsDatabase!!.
                contactsDao()
                .getContactAllInfo()}
        val result=executorService.submit(callDb)
        println("result knocker"+ result?.get())
        val tmp:List<ContactWithAllInformation>?
                =result.get()
        if(tmp!!.isEmpty()){
            contacts=buildContactList(context)
        }else{
            contacts=tmp
        }
    }
    fun synchronizedList(){

            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { contactsDatabase!!.contactsDao().getContactAllInfo() }
            val result = executorService.submit(callDb)
            println("result knocker" + result.get())
            contacts = result.get()
        //TODO verifiy with Ryan

    }
    fun getInfoWithName(name: String, platform: String): String {
        // on init WorkerThread


        var info = "Platform Error"
        when (platform) {
            "message" -> {
                info = getPhoneNumberWithName(name)
            }
        }
        return info
    }

    fun getPhoneNumberWithName(name: String): String {
        var info = "Name Error"
        if (name.contains(" ")) {
            contacts!!.forEach { dbContact ->
                if (dbContact.contactDB!!.firstName+" "+dbContact.contactDB!!.lastName == name) {
                    info = dbContact.contactDetailList!!.get(0).content.dropLast(1)
                }
            }
        } else {
            contacts!!.forEach { dbContact ->
                if (dbContact.contactDB!!.firstName == name && dbContact.contactDB!!.lastName == "" || dbContact.contactDB!!.firstName == "" && dbContact.contactDB!!.lastName == name) {
                    info = dbContact.contactDetailList!!.get(0).content.dropLast(1)
                }
            }
        }
        return info
    }


    fun sortContactByFirstNameAZ(){
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb= Callable { contactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
        val result=executorService.submit(callDb)
        contacts=result.get()
    }

    private fun getAllContactFilter(filterList: ArrayList<String>): List<ContactWithAllInformation>? {
        val allFilters: MutableList<List<ContactWithAllInformation>> = mutableListOf()
        var filter: List<ContactWithAllInformation>?
        //val allContacts = main_ContactsDatabase?.contactsDao()!!.getContactAllInfo()
        println(filterList)
        if (filterList.contains("sms")) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb= Callable { contactsDatabase?.contactsDao()?.getContactWithPhoneNumber()}
            val result=executorService.submit(callDb)
            filter = result.get()

            if (filter != null && filter.isEmpty() == false) {
                allFilters.add(filter)
            }
        }
        if (filterList.contains("mail")) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb= Callable { contactsDatabase?.contactsDao()?.getContactWithMail()}
            val result=executorService.submit(callDb)
            filter = result.get()
            if (filter != null && filter.isEmpty() == false) {
                allFilters.add(filter)
            }
        }
        if (filterList.isEmpty())
            return null
        var i = 0
        if (allFilters.size > 1) {
            while (i < allFilters.size - 1) {
                allFilters[i+1] = allFilters[i].intersect(allFilters[i + 1]).toList()
                i++
            }
        } else if (allFilters.size == 0) {
            return null
        } else
            return allFilters[0]
        return allFilters[i]
    }


    fun getContactByName(name:String):List<ContactWithAllInformation>{
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb= Callable { contactsDatabase?.contactsDao()?.getContactByName(name)}
        val result=executorService.submit(callDb)
        return result.get()!!
    }

    fun getContactConcernByFilter(filterList: ArrayList<String>,name:String):List<ContactWithAllInformation>{
        val contactFilterList: List<ContactWithAllInformation>? = getAllContactFilter(filterList)
        val contactList=getContactByName(name)
        if (contactFilterList != null) {
           return contactList!!.intersect(contactFilterList).toList()
        }
        return contactList
    }
   //region region Creation FakeContact



    fun loadJSONFromAsset(context: Context):String{
        var json = ""
        try{
            json= context.assets.open("premiers_contacts.json").bufferedReader().use{
                it.readText()
            }
        }catch (e :Exception){
            e.printStackTrace()
            return ""
        }
        return json
    }
    fun buildContactList(context: Context):List<ContactWithAllInformation>{
        var listContacts = mutableListOf<ContactWithAllInformation>()
        var contactString=loadJSONFromAsset(context)
        try {
            val jsArray = JSONArray(contactString)
            for(x in 0..(jsArray.length()-1)){
                listContacts.add(getContactFromJSONObject(jsArray.getJSONObject(x),x))
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return listContacts
    }
    fun getContactFromJSONObject(json: JSONObject, id:Int): ContactWithAllInformation {
        val firstName:String = json.getString("first_name")
        val lastName:String = json.getString("last_name")

        val profilPicture: Int = R.drawable.img_avatar
        val contactPriority :Int= json.getInt("contact_priority")
        val profilPictureStr:String = json.getString("profile_picture_str")

        println("contact :"+ firstName +" "+ lastName)
        val contact = ContactDB(id, firstName, lastName, profilPicture, contactPriority, profilPictureStr)
        val contactInfo = ContactWithAllInformation()
        contactInfo.contactDB= contact
        contactInfo.contactDetailList=getContactDeatailFromJSONObject(json,id)
        return contactInfo
    }
    fun getContactDeatailFromJSONObject(json: JSONObject, idContact:Int): List<ContactDetailDB> {
        val phoneNumber:String = json.getString("phone_number")
        val mail:String=json.getString("mail")
        val contactDetails = ContactDetailDB(null,idContact,phoneNumber+"M","phone", "",0)
        val contactDetails2 = ContactDetailDB(null,idContact,mail+"B","mail", "",1)
        return mutableListOf<ContactDetailDB>(contactDetails,contactDetails2)
    }
    fun getContactId(id:Int): ContactWithAllInformation? {
        for (contactJSON in contacts){
            if(contactJSON.contactDB!!.id==id){
                return contactJSON
            }
        }
        return null
    }

    //endregion


}
