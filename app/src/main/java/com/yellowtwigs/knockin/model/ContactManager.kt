package com.yellowtwigs.knockin.model

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Base64
import androidx.lifecycle.asLiveData
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * La Classe qui contient toute les fonctions qui touche à la synchronisation des contactList, les filtre et searchbar
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactManager(var contactList: ArrayList<ContactWithAllInformation>, var context: Context) {
    constructor(context: Context) : this(arrayListOf<ContactWithAllInformation>(), context)

    private var mDbWorkerThread: DbWorkerThread = DbWorkerThread("dbWorkerThread")
    private var contactsDatabase: ContactsDatabase? = null

    init {
        mDbWorkerThread.start()
        contactsDatabase = ContactsDatabase.getDatabase(context)
        if (contactList.isEmpty()) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable {
                contactsDatabase?.contactsDao()?.getContactAllInfo()?.asLiveData()?.value
            }
            val result = executorService.submit(callDb)
            val tmp: ArrayList<ContactWithAllInformation> = arrayListOf()
            result.get()?.let { tmp.addAll(it) }
            contactList = if (tmp.isEmpty()) {
                buildContactListFromJson(context)
            } else {
                tmp
            }
        }
    }

    //region Filter
//    private fun getAllContactFilter(filterList: ArrayList<String>): List<ContactWithAllInformation>? {
//        val allFilters: MutableList<List<ContactWithAllInformation>> = mutableListOf()
//        var filter: List<ContactWithAllInformation>?
//        //check si la list contient sms,mail ou rien
//        if (filterList.isEmpty())
//            return null
//        if (filterList.contains("sms")) {
//            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//            val callDb = Callable { contactsDatabase?.contactsDao()?.getContactWithPhoneNumber() }
//            val result = executorService.submit(callDb)
//            filter = result.get()
//            if (filter != null && filter.isEmpty() == false) {
//                allFilters.add(filter)
//            }
//        }
//        if (filterList.contains("mail")) {
//            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//            val callDb = Callable { contactsDatabase?.contactsDao()?.getContactWithMail() }
//            val result = executorService.submit(callDb)
//            filter = result.get()
//            if (filter != null && filter.isNotEmpty()) {
//                allFilters.add(filter)
//            }
//        }
//
//        var i = 0
//        //contient plus de 1 filtre rentre dans le if, 0 filtre dans le else if , 1 filtre dans le else
//        if (allFilters.size > 1) {
//            while (i < allFilters.size - 1) {
//                allFilters[i + 1] = allContactIntersect(allFilters[i], allFilters[i + 1])
//                //allFilters[i + 1] = allFilters[i].intersect(allFilters[i + 1]).toList()
//                i++
//            }
//        } else if (allFilters.size == 0) {
//            return null
//        } else
//            return allFilters[0]
//        return allFilters[i]
//    }

    private fun allContactIntersect(
        firstList: List<ContactWithAllInformation>,
        secondList: List<ContactWithAllInformation>
    ): List<ContactWithAllInformation> {
        val filter = arrayListOf<ContactWithAllInformation>()
        firstList.forEach { first ->
            secondList.forEach {
                if (first.getContactId() == it.getContactId())
                    filter.add(first)
            }
        }
        return filter
    }

//    fun getContactConcernByFilter(
//        filterList: ArrayList<String>,
//        name: String
//    ): List<ContactWithAllInformation> {
//        //get tout les contact en appliquant les filtres
//        val contactFilterList: List<ContactWithAllInformation>? = getAllContactFilter(filterList)
//        //get tout les contact en appliquant la searchbar
//        val contactList = getContactByName(name) //TODO inverser avec ligne 189
//        if (contactFilterList != null) {
//            //get uniquement les contact en commun dans les 2 list
//            return intersectContactWithAllInformation(contactList, contactFilterList)
//        }
//        return contactList
//    }

    //endregion

    private fun searchInPhoneNumber(
        name: String,
        listContact: List<ContactWithAllInformation>
    ): List<ContactWithAllInformation> {
        val finalList = arrayListOf<ContactWithAllInformation>()
        listContact.forEach { contact ->
            if (contact.contactDetailList != null) {
                contact.contactDetailList!!.forEach {
                    if (it.type == "phone" && isSameNumber(name, it.content)) {
                        finalList.add(contact)
                    }
                }
            }
        }
        return finalList
    }

    private fun isSameNumber(phoneNumberSB: String, phoneNumber: String): Boolean {
        var epurePhoneNumberSB = phoneNumberSB.replace("\\s".toRegex(), "")
        var epurePhoneNumber = phoneNumber.replace("\\s".toRegex(), "")
        if (epurePhoneNumberSB.contains("+33")) {
            epurePhoneNumberSB = epurePhoneNumberSB.removePrefix("+33")
            epurePhoneNumberSB = "0$epurePhoneNumberSB"
        }
        if (epurePhoneNumber.contains("+33")) {
            epurePhoneNumber = epurePhoneNumber.removePrefix("+33")
            epurePhoneNumber = "0$epurePhoneNumber"
        }
        if (epurePhoneNumber.contains(epurePhoneNumberSB))
            return true
        return false
    }

    private fun intersectContactWithAllInformation(
        contactList: List<ContactWithAllInformation>,
        contactFilterList: List<ContactWithAllInformation>
    ): List<ContactWithAllInformation> {
        val listContacts = mutableListOf<ContactWithAllInformation>()
        contactFilterList.forEach { type ->
            contactList.forEach {
                if (type.getContactId() == it.getContactId())
                    listContacts.add(type)
            }
        }
        return listContacts
    }

    //region region Creation FakeContact

    private fun loadJSONFromAsset(context: Context): String {
        var json = ""
        try {
            json = context.assets.open("premiers_contacts.json").bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return json
    }

    private fun buildContactListFromJson(context: Context): ArrayList<ContactWithAllInformation> {
        val listContacts = arrayListOf<ContactWithAllInformation>()
        val contactString = loadJSONFromAsset(context)
        try {
            val jsArray = JSONArray(contactString)
            for (x in 0 until jsArray.length()) {
                listContacts.add(getContactFromJSONObject(jsArray.getJSONObject(x), x + 1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return listContacts
    }

    private fun getContactFromJSONObject(json: JSONObject, id: Int): ContactWithAllInformation {
        val firstName: String = json.getString("first_name")
        val lastName: String = json.getString("last_name")

        val contactPriority: Int = json.getInt("contact_priority")
        val profilPictureStr: String = json.getString("profile_picture_str")
        val contact = ContactDB(
            id,
            firstName,
            lastName,
            "",
            R.drawable.ic_user_blue,
            contactPriority,
            profilPictureStr,
            0,
            "",
            0,
            "",
            0,
            1,
            ""
        )
        val contactInfo = ContactWithAllInformation()
        contactInfo.contactDB = contact
        contactInfo.contactDetailList = getContactDetailFromJSONObject(json, id)
        return contactInfo
    }

    private fun getContactDetailFromJSONObject(
        json: JSONObject,
        idContact: Int
    ): List<ContactDetailDB> {
        val phoneNumber: String = json.getString("phone_number")
        val mail: String = json.getString("mail")

        val contactDetails = ContactDetailDB(null, idContact, phoneNumber, "phone", "", 0)
        val contactDetails2 = ContactDetailDB(null, idContact, mail, "mail", "", 1)
        return mutableListOf(contactDetails, contactDetails2)
    }

    //endregion

    //region region ContactSync



    private fun isDuplicate(
        contacts: List<ContactWithAllInformation>, phoneContactList: ContactDB
    ): Boolean {
        contacts.forEach { contactsInfo ->
            val contactsDB = contactsInfo.contactDB
            if (contactsDB?.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                return true
        }
        return false
    }

    /**
     * fonction qui permet de deserializer les contactList de la dernière sync.
     * @param lastSync String
     * @return List<Pair<Int, Int>>
     */
    fun sliceLastSync(lastSync: String): List<Pair<Int, Int>> {
        val lastSyncList = arrayListOf<Pair<Int, Int>>()
        var AllId: Pair<Int, Int>
        val lastSyncSplit = lastSync.split("|")
        lastSyncSplit.forEach {
            if (it != "") {
                val idSplit = it.split(":")
                AllId = Pair(idSplit[0].toInt(), idSplit[1].toInt())
                lastSyncList.add(AllId)
            }
        }
        return lastSyncList
    }

    fun setToContactList(contactSet: List<String>): Pair<ContactDB, List<ContactDetailDB>> {
        val allContacts: Pair<ContactDB, List<ContactDetailDB>>
        val detailList = arrayListOf<ContactDetailDB>()
        for (i in 3 until contactSet.size) {
            val contactSetSplite = contactSet.elementAt(i).split(":")
            detailList.add(
                ContactDetailDB(
                    null,
                    null,
                    contactSetSplite[1],
                    contactSetSplite[0].drop(1),
                    contactSetSplite[2],
                    i - 2
                )
            )
        }
        allContacts = Pair(
            ContactDB(
                contactSet.elementAt(0).drop(1).toInt(),
                contactSet.elementAt(1).drop(1),
                contactSet.elementAt(2).drop(1),
                "",
                0,
                0,
                "",
                0,
                "",
                0,
                "",
                0,
                1,
                ""
            ), detailList
        )
        return allContacts
    }




    //endregion

    fun getContactWithName(name: String, platform: String): ContactWithAllInformation? {
        when (platform) {
            "message" -> {
                return getContact(name)
            }
            "Message" -> {
                return getContact(name)
            }
            "WhatsApp" -> {
                return getContact(name)
            }
            "whatsApp" -> {
                return getContact(name)
            }
            "gmail" -> {
                return getContact(name)
            }
            "Gmail" -> {
                return getContact(name)
            }
            "Outlook" -> {
                return getContact(name)
            }
            "outlook" -> {
                return getContact(name)
            }
            "Messenger" -> {
                return getContact(name)
            }
            "messenger" -> {
                return getContact(name)
            }
        }
        return null
    }

    // get la priorité grace à la liste
    fun getContact(name: String): ContactWithAllInformation? {
        if (name.contains(" ")) {
            val array = name.toCharArray().toList()
            val array2 = arrayListOf<Char>()
            var canSpace = false
            var oneTime = true
            array.forEach { char ->
                if (char.isLetter() || char == '-' || canSpace) {
                    array2.add(char)
                    canSpace = oneTime
                    if (char.isWhitespace()) {
                        canSpace = false
                        oneTime = false
                    }
                }
            }

            val name1 = String(array2.toCharArray())
            this.contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!

                val firstnameList: ArrayList<Char> = arrayListOf()
                var firstname = ""
                val lastnameList: ArrayList<Char> = arrayListOf()
                var lastname = ""

                var cpt = 0

                while (name[cpt] != ' ') {
                    firstnameList.add(name[cpt]) // Kj, " " = 2
                    cpt++
                }
                cpt++

                for (i in 0 until firstnameList.size) {
                    firstname += firstnameList[i]
                }

                while (cpt < name.length) {
                    lastnameList.add(name[cpt]) // Kent, " " = 2
                    cpt++
                }

                for (i in 0 until lastnameList.size) {
                    lastname += lastnameList[i]
                }

                if (contactInfo.firstName + " " + contactInfo.lastName == name ||
                    contactInfo.firstName == name || contactInfo.lastName == name ||
                    " " + contactInfo.firstName + " " + contactInfo.lastName == name ||
                    name1 == contactInfo.firstName + " " + contactInfo.lastName ||
                    contactInfo.firstName == name1 || contactInfo.lastName == name1
                ) {

                    return dbContact

                } else if (contactInfo.mail_name == name) {

                    return dbContact

                } else if (lastname == contactInfo.lastName && firstname == contactInfo.firstName) {
                    return dbContact
                } else {
                    if (lastname == contactInfo.lastName) {
//                        if (name.contains("_") || name.contains(".") || name.contains("-") || name.contains("_")) {
                        if (contactInfo.firstName.contains("-")) {
                            // Kj Kent = Kenny-Jay Kent

                            var cpt2 = 0
                            val firstLetterList: ArrayList<Char> = arrayListOf()
                            val lastLetterList: ArrayList<Char> = arrayListOf()
                            var firstLetter = ""
                            var lastLetter = ""

                            while (contactInfo.firstName[cpt2] != '-') {
                                firstLetterList.add(contactInfo.firstName[cpt2]) // K de Kenny
                                firstLetter = firstLetterList[0].toString()
                                cpt2++
                            }
                            cpt2++

                            while (cpt2 < name.length) {
                                lastLetterList.add(contactInfo.firstName[cpt2]) // J de Jay
                                lastLetter = lastLetterList[0].toString()
                                cpt2++
                            }

                            if (firstname == firstLetter + lastLetter ||
                                firstname == firstLetter.toLowerCase() + lastLetter.toLowerCase() ||
                                firstname == firstLetter + lastLetter.toLowerCase()
                            ) {
                                dbContact.contactDB!!.mail_name = name
                                return dbContact
                            }

                        } else {
                            // Ken Suon = Kenzy Suon
                            if (contactInfo.firstName.contains(firstname)) {
                                return dbContact
                            }
                        }

                        // Jfc = Jean-Francois Coudeyre

                    } else {
                        var entireName = name.replace(name[0], ' ')
                        entireName = entireName.replace(name[entireName.length - 1], ' ')

                        if (' ' + contactInfo.firstName + " " + contactInfo.lastName + ' ' == entireName || ' ' + contactInfo.firstName + ' ' == entireName || ' ' + contactInfo.lastName + ' ' == entireName) {
                            return dbContact
                        }
                    }
                }
            }
        } else {
            val array = name.toCharArray().toList()
            val array2 = arrayListOf<Char>()
            array.forEach { char ->
                if (char.isLetter() || char == '-') {
                    array2.add(char)
                }
            }
            val name1 = String(array2.toCharArray())
            contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!

                if (contactInfo.firstName == name && contactInfo.lastName == "" || contactInfo.firstName == "" && contactInfo.lastName == name ||
                    contactInfo.firstName == name1 && contactInfo.lastName == ""
                ) {
                    return dbContact
                } else {
                    if (name.isNotEmpty()) {
                        var entireName = name.replace(name[0], ' ')
                        entireName = entireName.replace(name[entireName.length - 1], ' ')

                        if (' ' + contactInfo.firstName + " " + contactInfo.lastName + ' ' == entireName || ' ' + contactInfo.firstName + ' ' == entireName || ' ' + contactInfo.lastName + ' ' == entireName) {
                            return dbContact
                        }
                    }
                }
            }
        }
        return null
    }

    fun getContactId(name: String): Int {
        if (name.contains(" ")) {
            this.contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!
                if (contactInfo.firstName + " " + contactInfo.lastName == name) {
                    return contactInfo.id!!
                }
            }
        } else {
            this.contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!

                if (contactInfo.firstName == name && contactInfo.lastName == "" || contactInfo.firstName == "" && contactInfo.lastName == name) {
                    return contactInfo.id!!
                }
            }
        }
        return -1
    }

    fun getContactFromNumber(phoneNumber: String): ContactWithAllInformation? {
        for (contact in this.contactList) {
            if (PhoneNumberUtils.compare(contact.getFirstPhoneNumber(), phoneNumber)) {
                return contact
            }
        }
        return null
    }

    fun setToContactInListPriority(priority: Int) {
        for (contact in this.contactList) {
            val callDb = Callable {
                contact.setPriority(contactsDatabase, priority)
            }
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val result = executorService.submit(callDb)
            result.get()
        }
    }
}