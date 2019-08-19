package com.example.knocker.model

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Base64
import com.example.knocker.R
import com.example.knocker.model.ModelDB.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.ArrayList
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
    private var contactsDatabase: ContactsRoomDatabase? = null

    init {
        mDbWorkerThread.start()
        contactsDatabase = ContactsRoomDatabase.getDatabase(context)
        if (contactList.isEmpty()) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable {
                contactsDatabase!!.contactsDao()
                        .getContactAllInfo()
            }
            val result = executorService.submit(callDb)
            val tmp: ArrayList<ContactWithAllInformation> = arrayListOf()
            tmp.addAll(result.get())
            if (tmp.isEmpty()) {
                contactList = buildContactListFromJson(context)
            } else {
                contactList = tmp
            }
        }
    }

    /**
     * Renvoie le contact dont on a passé en paramètre l'id
     * @param id Int
     * @return [ContactWithAllInformation]
     */
    fun getContactById(id: Int): ContactWithAllInformation? {
        for (contact in this.contactList) {
            if (contact.contactDB!!.id == id) {
                return contact
            }
        }
        return null
    }

//region SortContact Toutes les méthodes nous permetttant de trier les contacts
    fun sortContactByFirstNameAZ() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
        val result = executorService.submit(callDb)
        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
        listChangement.addAll(result.get())
        listChangement.retainAll(contactList)
        contactList = listChangement
    }

    fun sortContactByLastname() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByLastNameAZ() }
        val result = executorService.submit(callDb)
        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
        listChangement.addAll(result.get())
        listChangement.retainAll(contactList)
        contactList = listChangement
    }

    fun sortContactByPriority() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByPriority20() }
        val result = executorService.submit(callDb)
        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
        listChangement.addAll(result.get())
        listChangement.retainAll(contactList)
        contactList = listChangement
    }

    fun sortContactByFavorite() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByFavorite() }
        val result = executorService.submit(callDb)
        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
        listChangement.addAll(result.get())
        listChangement.retainAll(contactList)
        contactList = listChangement
    }

    fun sortContactByGroup() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
        val result = executorService.submit(callDb)
        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
        listChangement.addAll(result.get())
        val listTmp: ArrayList<ContactWithAllInformation> = ArrayList()
        //listTmp.addAll(listChangement)
        for (i in listChangement) {
            if (i.getFirstGroup(context) == null) {
                listTmp.add(i)
            }
        }
        val listOfContactWithGroup: ArrayList<ContactWithAllInformation> = ArrayList()
        listOfContactWithGroup.addAll(listChangement)
        listOfContactWithGroup.removeAll(listTmp)
        listOfContactWithGroup.sortBy { it.getFirstGroup(context)?.name?.toUpperCase() }//selector(listChangement.get(i))}
        listChangement.removeAll(listOfContactWithGroup)
        listChangement.addAll(0, listOfContactWithGroup)//ajouté à l'index 0 la liste de contact avec groupe
        contactList = listChangement
    }


//endregion
//region Filter
    private fun getAllContactFilter(filterList: ArrayList<String>): List<ContactWithAllInformation>? {
        val allFilters: MutableList<List<ContactWithAllInformation>> = mutableListOf()
        var filter: List<ContactWithAllInformation>?
        //check si la list contient sms,mail ou rien
        if (filterList.isEmpty())
            return null
        if (filterList.contains("sms")) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { contactsDatabase?.contactsDao()?.getContactWithPhoneNumber() }
            val result = executorService.submit(callDb)
            filter = result.get()
            if (filter != null && filter.isEmpty() == false) {
                allFilters.add(filter)
            }
        }
        if (filterList.contains("mail")) {
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val callDb = Callable { contactsDatabase?.contactsDao()?.getContactWithMail() }
            val result = executorService.submit(callDb)
            filter = result.get()
            if (filter != null && filter.isNotEmpty()) {
                allFilters.add(filter)
            }
        }

        var i = 0
        //contient plus de 1 filtre rentre dans le if, 0 filtre dans le else if , 1 filtre dans le else
        if (allFilters.size > 1) {
            while (i < allFilters.size - 1) {
                allFilters[i + 1] = allContactIntersect(allFilters[i], allFilters[i + 1])
                //allFilters[i + 1] = allFilters[i].intersect(allFilters[i + 1]).toList()
                i++
            }
        } else if (allFilters.size == 0) {
            return null
        } else
            return allFilters[0]
        return allFilters[i]
    }

    private fun allContactIntersect(firstList: List<ContactWithAllInformation>, secondList: List<ContactWithAllInformation>): List<ContactWithAllInformation> {
        val filter = arrayListOf<ContactWithAllInformation>()
        firstList.forEach { first ->
            secondList.forEach {
                if (first.getContactId() == it.getContactId())
                    filter.add(first)
            }
        }
        return filter
    }

    fun getContactConcernByFilter(filterList: ArrayList<String>, name: String): List<ContactWithAllInformation> {
        //get tout les contact en appliquant les filtres
        val contactFilterList: List<ContactWithAllInformation>? = getAllContactFilter(filterList)
        //get tout les contact en appliquant la searchbar
        val contactList = getContactByName(name) //TODO inverser avec ligne 189
        if (contactFilterList != null) {
            //get uniquement les contact en commun dans les 2 list
            return intersectContactWithAllInformation(contactList, contactFilterList)
        }
        return contactList
    }
    //endregion
    private fun getContactByName(name: String): List<ContactWithAllInformation> {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        var callDb = Callable { contactsDatabase?.contactsDao()?.getContactByName(name) }
        var result = executorService.submit(callDb)
        var listContact = result.get()!!
        if (listContact.isEmpty()) {
            callDb = Callable { contactsDatabase?.contactsDao()?.getContactAllInfo() }
            result = executorService.submit(callDb)
            listContact = result.get()!!
            listContact = searchInPhoneNumber(name, listContact)
        }
        return listContact
    }

    private fun searchInPhoneNumber(name: String, listContact: List<ContactWithAllInformation>): List<ContactWithAllInformation> {
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

    private fun intersectContactWithAllInformation(contactList: List<ContactWithAllInformation>, contactFilterList: List<ContactWithAllInformation>): List<ContactWithAllInformation> {
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

        val profilPicture: Int = R.drawable.img_avatar
        val contactPriority: Int = json.getInt("contact_priority")
        val profilPictureStr: String = json.getString("profile_picture_str")
        val contact = ContactDB(id, firstName, lastName, profilPicture, contactPriority, profilPictureStr, 0)
        val contactInfo = ContactWithAllInformation()
        contactInfo.contactDB = contact
        contactInfo.contactDetailList = getContactDetailFromJSONObject(json, id)
        return contactInfo
    }

    private fun getContactDetailFromJSONObject(json: JSONObject, idContact: Int): List<ContactDetailDB> {
        val phoneNumber: String = json.getString("phone_number")
        val mail: String = json.getString("mail")
//        val favorite: Boolean = json.getBoolean("mail")

        val contactDetails = ContactDetailDB(null, idContact, phoneNumber, "phone", "", 0)
        val contactDetails2 = ContactDetailDB(null, idContact, mail, "mail", "", 1)
        return mutableListOf(contactDetails, contactDetails2)
    }

    //endregion

    //region region ContactSync

    /**
     * fonction qui permet de vérifier si le numéro de téléphone n'est pas déja enregister.
     * @param idAndPhoneNumber Map<Int, Any>
     * @param contactPhoneNumber List<Map<Int, Any>>
     * @return Boolean
     */
    private fun isDuplicate(id: Int, contactPhoneNumber: List<Pair<Int, Triple<String, String, String?>>>): Boolean {
        contactPhoneNumber.forEach {
            if (it.first == id)
                return true
        }
        return false
    }

    /**
     * fonction qui permet de récuper les noms entier des contactList du carnet Android.
     * @param main_contentResolver ContentResolver
     * @return List<Pair<Int, Triple<String, String, String>>>?
     */
    private fun getStructuredNameSync(main_contentResolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>>? {
        val phoneContactsList = arrayListOf<Pair<Int, Triple<String, String, String>>>()
        var idAndName: Pair<Int, Triple<String, String, String>>
        var StructName: Triple<String, String, String>
        //requete pour récuperer tout les nom/prénom complet contactList dans le carnet android
        val phonecontact = main_contentResolver.query(ContactsContract.Data.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
        if (phonecontact != null) {
            while (phonecontact.moveToNext()) {
                //récupère l'id d'un contact
                val phone_id = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)).toInt()
                //récupère le prénom d'un contact
                var firstName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                //récupère le middleName d'un contact
                var middleName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                //récupère le nom de famille d'un contact
                var lastName = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                //récupère le mimeType de la requete (permet de savoir si le curseur est sur item/name)
                val mimeType = phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))
                //on regarde si on à pas le contact en double
                if (phoneContactsList.isEmpty() && mimeType == "vnd.android.cursor.item/name") {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    StructName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phone_id, StructName)
                    //ajoute le contact à la liste de contact
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
                    //ajoute le contact à la liste de contact
                    phoneContactsList.add(idAndName)
                }
            }
        }
        phonecontact?.close()
        return phoneContactsList
    }

    /**
     * fonction qui permet de convertir un bitmap en base64.
     * @param bitmap Bitmap
     * @return String
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()

        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    /**
     * fonction qui permet de vérifier si le numéro de téléphone n'est pas déja enregister.
     * @param idAndPhoneNumber Map<Int, Any>
     * @param contactPhoneNumber List<Map<Int, Any>>
     * @return Boolean
     */
    private fun isDuplicateNumber(idAndPhoneNumber: Map<Int, Any>, contactPhoneNumber: List<Map<Int, Any>>): Boolean {
        contactPhoneNumber.forEach {
            if (it[1] == idAndPhoneNumber[1] && it[2].toString().replace("\\s".toRegex(), "") == idAndPhoneNumber[2].toString().replace("\\s".toRegex(), "")) {
                println("IT = " + it[2] + " IDANDPHONE = " + idAndPhoneNumber[2])
                return true
            }
        }
        return false
    }

    /**
     * fonction qui permet de récuper la photo des contactList du carnet Android.
     * @param contactId Long
     * @param main_contentResolver ContentResolver
     * @return InputStream?
     */
    private fun openPhoto(contactId: Long, main_contentResolver: ContentResolver): InputStream? {
        //on recupert l'uri du contact grace à l'id du contact
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        //on recupert l'uri de la photo grâce à l'uri du contact
        val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        //on met le curseur sur la photo
        val cursor = main_contentResolver.query(photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null) ?: return null
        try {
            if (cursor.moveToFirst()) {
                val data = cursor.getBlob(0)
                if (data != null) {
                    //on return la photo
                    return ByteArrayInputStream(data)
                }
            }
        } finally {
            cursor.close()
        }
        return null
    }

    /**
     * fonction qui permet de récuper les emails des contactList du carnet Android.
     * @param main_contentResolver ContentResolver
     * @return List<Map<Int, Any>>
     */
    private fun getContactMailSync(main_contentResolver: ContentResolver): List<Map<Int, Any>> {
        val contactDetails = arrayListOf<Map<Int, Any>>()
        var idAndMail: Map<Int, Any>
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            //recupert l'id du contact
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID))
            //recupert l'email du contact
            var phoneEmail = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
            //recupert le tag de l'email
            var phoneTag = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
            if (phoneEmail == null)
                phoneEmail = ""
            if (phoneTag == null) {
                phoneTag = "0"
            }
            //crée une map avec l'email, l'id du contact, son tag et le type du detail (mail)
            idAndMail = mapOf(1 to phoneId!!.toInt(), 2 to phoneEmail, 3 to assignTagEmail(phoneTag.toInt()), 4 to "", 5 to "mail")
            if (contactDetails.isEmpty() || !isDuplicateNumber(idAndMail, contactDetails)) {
                //ajoute la map à la liste
                contactDetails.add(idAndMail)
            }
        }
        phonecontact?.close()
        return contactDetails
    }

    /**
     * fonction qui permet de récupérer les numéros de téléphone des contactList du carnet Android.
     * @param main_contentResolver ContentResolver
     * @return List<Map<Int, Any>>
     */
    private fun getPhoneNumberSync(main_contentResolver: ContentResolver): List<Map<Int, Any>> {
        val contactPhoneNumber = arrayListOf<Map<Int, Any>>()
        var idAndPhoneNumber: Map<Int, Any>
        //requete pour récuperer les numéros de téléphone des contactList
        val phonecontact = main_contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phonecontact.moveToNext()) {
            //récupère l'id d'un contact
            val phoneId = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            //récupère le numéro de téléphone d'un contact
            var phoneNumber = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            //récupère l'id de la photo de profile d'un contact
            var phonePic = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            //récupère le tag du numéro de téléphone
            var phoneTag = phonecontact?.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
            if (phoneNumber == null)
                phoneNumber = ""
            //on check si le contact possède une photo
            if (phonePic == null || phonePic.contains("content://com.android.contactList/contactList/", ignoreCase = true)) {
                phonePic = ""
            } else {
                //on recupert la photo et on la converti en base64
                phonePic = bitmapToBase64(BitmapFactory.decodeStream(openPhoto(phoneId!!.toLong(), main_contentResolver)))
            }
            if (phoneTag == null) {
                phoneTag = "0"
            }
            //on stocke toute les infos d'un contact dans idAndPhoneNumber
            idAndPhoneNumber = mapOf(1 to phoneId!!.toInt(), 2 to phoneNumber, 3 to assignTagNumber(phoneTag.toInt()), 4 to phonePic, 5 to "phone")
            if (contactPhoneNumber.isEmpty() || !isDuplicateNumber(idAndPhoneNumber, contactPhoneNumber)) {
                //on ajoute le contact dans la liste des contactList
                contactPhoneNumber.add(idAndPhoneNumber)
            }
        }
        phonecontact?.close()
        return contactPhoneNumber
    }

    /**
     * fonction qui permet de récuper le tag d'un email du carnet de contact Android.
     * @param intTag Int
     * @return String
     */
    private fun assignTagEmail(intTag: Int): String {
        var tag = "other"
        when (intTag) {
            1 -> tag = "home"
            2 -> tag = "work"
        }
        return tag
    }

    /**
     * fonction qui permet de récuper le tag d'un numero de téléphone du carnet de contact Android.
     * @param intTag Int
     * @return String
     */
    private fun assignTagNumber(intTag: Int): String {
        var tag = "other"
        when (intTag) {
            1 -> tag = "home"
            2 -> tag = "mobil"
            3 -> tag = "work"
        }
        return tag
    }

    /**
     * fonction qui permet de donner un avatar de façon random à un contact ou de recupérer son avatar à l'aide d'un nombre .
     * @param avatarId Int
     * @param createOrGet String
     * @return Int
     */
    fun randomDefaultImage(avatarId: Int, createOrGet: String): Int {
        if (createOrGet == "Create") {
            return kotlin.random.Random.nextInt(0, 7)
        } else if (createOrGet == "Get") {
            when (avatarId) {
                0 -> return R.drawable.ic_user_purple
                1 -> return R.drawable.ic_user_blue
                2 -> return R.drawable.ic_user_knocker
                3 -> return R.drawable.ic_user_green
                4 -> return R.drawable.ic_user_om
                5 -> return R.drawable.ic_user_orange
                6 -> return R.drawable.ic_user_pink
                else -> return R.drawable.ic_user_blue
            }
        }
        return -1
    }

    private fun isDuplicate(contacts: List<ContactWithAllInformation>?, phoneContactList: ContactDB): Boolean {
        contacts?.forEach { contactsInfo ->
            val contactsDB = contactsInfo.contactDB!!
            if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
                return true
        }
        return false//TODO
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

    /**
     * fonction qui permet de deserializer les contactList de la dernière sync.
     * @param id Int
     * @param contactNumberAndPic List<Map<Int, Any>>
     * @return List<ContactDetailDB>
     */
    private fun getDetailsById(id: Int, contactNumberAndPic: List<Map<Int, Any>>): List<ContactDetailDB> {
        val contactDetails = arrayListOf<ContactDetailDB>()
        var fieldPosition = 0
        contactNumberAndPic.forEach {
            if (it[1] == id) {
                contactDetails.add(ContactDetailDB(null, null, it[2].toString(), it[5].toString(), it[3].toString(), fieldPosition))
                fieldPosition++
            }
        }
        return contactDetails
    }

    private fun getGroupsAndLinks(id: Int, contactGroup: List<Triple<Int, String?, String?>>): List<GroupDB> {
        val contactGroups = arrayListOf<GroupDB>()
        var linkAndGroup: GroupDB
        contactGroup.forEach {
            if (it.first == id) {
                linkAndGroup = GroupDB(null, it.third!!, "")
                contactGroups.add(linkAndGroup)
            }
        }
        return contactGroups
    }

    private fun createListContactsSync(phoneStructName: List<Pair<Int, Triple<String, String, String>>>?, contactNumberAndPic: List<Map<Int, Any>>, contactGroup: List<Triple<Int, String?, String?>>, gestionnaireContacts: ContactManager) {
        val phoneContactsList = arrayListOf<ContactDB>()
        val lastId = arrayListOf<Int>()
        val applicationContext = this.context
        val sharedPreferences = applicationContext.getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        var ContactLinksGroup: Pair<LinkContactGroup, GroupDB>
        val listLinkAndGroup = arrayListOf<Pair<LinkContactGroup, GroupDB>>()
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        var lastSyncId = ""
        var lastSync = ""
        val callDb = Callable {
            //on récupère tout les contactList de la database
            val allcontacts = contactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
            var modifiedContact = 0
            //Pour chaque contact on va recuperer tout les numéro et email associé
            phoneStructName!!.forEach { fullName ->
                val set = mutableSetOf<String>()
                contactNumberAndPic.forEach { numberPic ->
                    val id = numberPic[1].toString().toInt()
                    //on regarde si l'id est dans lastId pour évité de créer un contact doublon dans la database
                    if (!lastId.contains(id)) {
                        //on récupere tout les details d'un contact grâce à son id android
                        val contactDetails = getDetailsById(id, contactNumberAndPic)
                        //on récupere tout les groupe du contact grâce à son id android
                        val contactGroups = getGroupsAndLinks(id, contactGroup)
                        // on regarde si l'id du fullName est le même que celui de numberPic
                        if (fullName.first == numberPic[1]) {
                            lastId.add(id)
                            //on regarde si le contact possède un middle name
                            if (fullName.second.second == "") {
                                //on créé un objet ContactDB que l'on remplis avec les info récolté avant
                                val contacts = ContactDB(null, fullName.second.first, fullName.second.third, randomDefaultImage(0, "Create"), 1, numberPic[4].toString(), 0)
                                //on recupere la liste des contactList récuperer lors de la derniere synchro sous format idAndroid:id
                                lastSync = sharedPreferences.getString("last_sync_2", "")!!
                                //on regarde si on a pas deja enregistré le contact lors de la dernière synchro
                                if (!isDuplicateContacts(fullName, lastSync)) {
                                    //on enregistre dans la database le contact et on recupere son id Knocker
                                    contacts.id = contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                    //on serialise le nouveau contact ( idAndroid:
                                    lastSyncId += fullName.first.toString() + ":" + contacts.id.toString() + "|"
                                    for (details in contactDetails) {
                                        //pour chaque details du contact on lui donne l'id du contact
                                        details.idContact = contacts.id
                                    }
                                    for (groups in contactGroups) {
                                        //pour chaque groupe du contact on lui donne l'id du contact pour crée un link
                                        val links = LinkContactGroup(0, contacts.id!!.toInt())
                                        ContactLinksGroup = Pair(links, groups)
                                        listLinkAndGroup.add(ContactLinksGroup)
                                    }
                                    //on sauvegarde dans la database les groupes et les linkcontactGroupe
                                    saveGroupsAndLinks(listLinkAndGroup)
                                    listLinkAndGroup.clear()
                                    //on sauvegarde dans la database les details du contact
                                    contactsDatabase!!.contactsDao().insertDetails(contactDetails)
                                } else {
                                    var positionInSet = 3
                                    //on commence à serialiser le contact
                                    println("CONTACT ALREADY EXIST = " + fullName.second + " " + fullName.first)
                                    val contact = getContactWithAndroidId(fullName.first, lastSync)
                                    if (contact != null) {
                                        set.add("0" + contact.contactDB!!.id)
                                        set.add("1" + fullName.second.first)
                                        if (fullName.second.second == "")
                                            set += "2" + fullName.second.third
                                        else
                                            set += "2" + fullName.second.second + " " + fullName.second.third
                                        for (details in contactDetails) {
                                            val alldetail = details.type + ":" + details.content + ":" + details.tag
                                            set += positionInSet.toString() + alldetail
                                            positionInSet++
                                        }
                                        // on regarde si le contact à été modifier depuis la dernière synchro, si oui on sauvegarde la serialisation dans une shared Pref
                                        if (!isSameContact(contact, fullName.second, contactDetails)) {
                                            modifiedContact++
                                            edit.putStringSet(modifiedContact.toString(), set)
                                            edit.apply()
                                        }
                                    } else {
                                        lastSync = deleteContactFromLastSync(lastSync, fullName.first)
                                        edit.putString("last_sync_2", lastSync)
                                        edit.apply()
                                       // createListContactsSync(phoneStructName, contactNumberAndPic, contactGroup, gestionnaireContacts)
                                        contacts.id = contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                        //on serialise le nouveau contact ( idAndroid:
                                        lastSyncId += fullName.first.toString() + ":" + contacts.id.toString() + "|"
                                        for (details in contactDetails) {
                                            //pour chaque details du contact on lui donne l'id du contact
                                            details.idContact = contacts.id
                                        }
                                        for (groups in contactGroups) {
                                            //pour chaque groupe du contact on lui donne l'id du contact pour crée un link
                                            val links = LinkContactGroup(0, contacts.id!!.toInt())
                                            ContactLinksGroup = Pair(links, groups)
                                            listLinkAndGroup.add(ContactLinksGroup)
                                        }
                                        //on sauvegarde dans la database les groupes et les linkcontactGroupe
                                        saveGroupsAndLinks(listLinkAndGroup)
                                        listLinkAndGroup.clear()
                                        //on sauvegarde dans la database les details du contact
                                        contactsDatabase!!.contactsDao().insertDetails(contactDetails)
                                    }
                                }
                                phoneContactsList.add(contacts)
                            } else if (fullName.second.second != "") {
                                val contacts = ContactDB(null, fullName.second.first, fullName.second.second + " " + fullName.second.third, randomDefaultImage(0, "Create"), 1, numberPic[4].toString(), 0)
                                phoneContactsList.add(contacts)
                                if (!isDuplicate(allcontacts, contacts)) {
                                    contacts.id = contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
                                    for (details in contactDetails) {
                                        details.idContact = contacts.id
                                    }
                                    contactsDatabase!!.contactsDao().insertDetails(contactDetails)
                                }
                            }
                        }
                    }
                }
            }
            if (lastSyncId != "") {
                if (lastSync != "")
                    lastSyncId = lastSync + lastSyncId
                edit.putString("last_sync_2", lastSyncId)
                edit.apply()
            }
            contactsDatabase?.contactsDao()?.getContactAllInfo()
        }
        val syncContact = executorService.submit(callDb).get()
        gestionnaireContacts.contactList.addAll(syncContact!!)
    }

    private fun deleteContactFromLastSync(lastSync: String, id: Int): String {
        val list = mutableListOf<Pair<Int, Int>>()
        val allId = sliceLastSync(lastSync)
        var newList = ""
        allId.forEach {
            if (id != it.first)
                list.add(Pair(it.first, it.second))
        }
        list.forEach {
            newList += it.first.toString() + ":" + it.second.toString() + "|"
        }
        return newList
    }

    fun setToContactList(contactset: List<String>): Pair<ContactDB, List<ContactDetailDB>> {
        val allContacts: Pair<ContactDB, List<ContactDetailDB>>
        val detailList = arrayListOf<ContactDetailDB>()
        for (i in 3..contactset.size - 1) {
            val contactSetSplite = contactset.elementAt(i).split(":")
            detailList.add(ContactDetailDB(null, null, contactSetSplite[1], contactSetSplite[0].drop(1), contactSetSplite[2], i - 2))
        }
        allContacts = Pair(ContactDB(contactset.elementAt(0).drop(1).toInt(), contactset.elementAt(1).drop(1), contactset.elementAt(2).drop(1), 0, 0, "", 0), detailList)
        return allContacts
    }

    private fun isSameContact(knockerContact: ContactWithAllInformation?, fullname: Triple<String, String, String>, contactDetail: List<ContactDetailDB>): Boolean {
        var isSame = true
        if (fullname.second != "") {
            if (knockerContact!!.contactDB!!.firstName != fullname.first || knockerContact.contactDB!!.lastName != fullname.second + " " + fullname.third) {
                return false
            }
        } else {
            if (knockerContact!!.contactDB!!.firstName != fullname.first || knockerContact.contactDB!!.lastName != fullname.third) {
                return false
            }
        }
        if (knockerContact.contactDetailList!!.size != contactDetail.size) {
            return false
        }
        var alreadyCheck: Int
        knockerContact.contactDetailList!!.forEach { knocker ->
            alreadyCheck = 0
            contactDetail.forEach {
                if (alreadyCheck == 0 && (knocker.type != it.type || knocker.content != it.content || knocker.tag != it.tag)) {
                    isSame = false
                } else {
                    alreadyCheck = 1
                    isSame = true
                }
            }
            if (!isSame)
                return false
        }
        return true
    }

    private fun getContactWithAndroidId(androidId: Int, lastSync: String): ContactWithAllInformation? {
        var contact: ContactWithAllInformation? = null
        var knockerId = -1
        val allId = sliceLastSync(lastSync)
        allId.forEach {
            if (androidId == it.first)
                knockerId = it.second
        }
        if (knockerId != -1) {
            contact = contactsDatabase?.contactsDao()?.getContact(knockerId)
        }
        return contact
    }

    private fun isDuplicateContacts(allcontacts: Pair<Int, Triple<String, String, String>>?, lastSync: String?): Boolean {
        if (lastSync != null /*|| lastSync != ""*/) {
            val allId = sliceLastSync(lastSync!!)
            allId.forEach { Id ->
                if (allcontacts!!.first == Id.first) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * fonction qui permet de récuperer les numéro de téléphone des contactList du carnet Android.
     * @param main_contentResolver ContentResolver
     * @return List<Triple<Int, String?, String?>>
     */
    private fun getContactGroupSync(main_contentResolver: ContentResolver): List<Triple<Int, String?, String?>> {
        val phoneContact = main_contentResolver.query(ContactsContract.Groups.CONTENT_URI, null, null, null, ContactsContract.Groups.TITLE + " ASC")
        var allGroupMembers = listOf<Triple<Int, String?, String?>>()
        println(phoneContact.columnNames)
        while (phoneContact.moveToNext()) {
            //récupère l'id du groupe
            val groupId = phoneContact?.getString(phoneContact.getColumnIndex(ContactsContract.Groups._ID))

            //récupère le nom du groupe
            var groupName = phoneContact?.getString(phoneContact.getColumnIndex(ContactsContract.Groups.TITLE))

            //récupère les membres du groupe
            if (groupName == "Starred in Android") {
                groupName = "Favorites"
            }
            val groupMembers = getMemberOfGroup(main_contentResolver, groupId.toString(), groupName)
            if (groupMembers.isNotEmpty() && allGroupMembers.isNotEmpty() && !isDuplicateGroup(allGroupMembers, groupMembers)) {
                //ajoute un membre au groupe
                allGroupMembers = allGroupMembers.union(groupMembers).toList()
            } else if (allGroupMembers.isEmpty())
                allGroupMembers = groupMembers
        }
        phoneContact?.close()
        return allGroupMembers
    }

    private fun getMemberOfGroup(main_contentResolver: ContentResolver, groupId: String, groupeName: String?): List<Triple<Int, String?, String?>> {
        val where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId
        val phoneContact = main_contentResolver.query(ContactsContract.Data.CONTENT_URI, null, where, null, ContactsContract.Data.DISPLAY_NAME + " ASC")
        var member: Triple<Int, String?, String?>
        val groupMembers = arrayListOf<Triple<Int, String?, String?>>()
        while (phoneContact.moveToNext()) {
            //récupère l'id du contact
            val contactId = phoneContact?.getString(phoneContact.getColumnIndex(ContactsContract.Data.CONTACT_ID))
            //récupère le nom du contact
            val contactName = phoneContact?.getString(phoneContact.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            member = Triple(contactId!!.toInt(), contactName, groupeName)
            if (!groupMembers.contains(member)) {
                //ajoute le contact à la liste des membre du groupe
                groupMembers.add(member)
            }
        }
        phoneContact?.close()
        return groupMembers
    }

    private fun isDuplicateGroup(member: List<Triple<Int, String?, String?>>, groupMembers: List<Triple<Int, String?, String?>>): Boolean {
        groupMembers.forEach {
            groupMembers.forEachIndexed { index, it ->
                if (it.third == member[0].third)
                    return true
            }
        }
        return false
    }


    private fun saveGroupsAndLinks(listLinkAndGroup: List<Pair<LinkContactGroup, GroupDB>>) {
        listLinkAndGroup.forEach {
            val dbGroup = contactsDatabase?.GroupsDao()!!.getGroupWithName(it.second.name)
            var groupId = 0
            if (dbGroup == null) {
                groupId = contactsDatabase?.GroupsDao()!!.insert(it.second)!!.toInt()
            } else {
                groupId = dbGroup.id!!.toInt()
            }
            if (groupId != 0) {
                contactsDatabase?.LinkContactGroupDao()!!.insert(LinkContactGroup(groupId, it.first.idContact))
            }
        }
    }

    /**
     * fonction qui permet de récuper tout les info des contactList du carnet Android.
     * @param main_contentResolver ContentResolver
     */
    fun getAllContacsInfoSync(main_contentResolver: ContentResolver) {
        //récupère le prénom et nom complet de tout les contactList
        val phoneStructName = getStructuredNameSync(main_contentResolver)
        //récupère tout les numéros de téléphone et l'image de profil de chaque contact
        val contactNumberAndPic = getPhoneNumberSync(main_contentResolver)
        //récupère tout les mails de chaque contact
        val contactMail = getContactMailSync(main_contentResolver)
        //récupère tout les groupes de chaque contact
        val contactGroup = getContactGroupSync(main_contentResolver)
        //fusionne dans contactDetail la list contactNumberAndPic et contactMail
        val contactDetail = contactNumberAndPic.union(contactMail)
        //clear la liste des contactList de la classe ContactManager(ContactWithAllInformation)
        contactList.clear()
        //fonction qui va stocker dans la database tout les element récuperé plus haut
        createListContactsSync(phoneStructName, contactDetail.toList(), contactGroup, this)
    }
//endregion

    fun getContactWithName(name: String, platform: String): ContactWithAllInformation? {
        println("test platform $platform test name $name")
        when (platform) {
            "message" -> {
                return getContact(name)
            }
            "WhatsApp" -> {
                return getContact(name)
            }
            "gmail" -> {
                return getContact(name)
            }
            "Messenger" -> {
                return getContact(name)
            }
        }
        return null
    }

    // get la priorité grace à la liste
    fun getContact(name: String): ContactWithAllInformation? {
        if (name.contains(" ")) {
            this.contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!

                if (contactInfo.firstName + " " + contactInfo.lastName == name || contactInfo.firstName == name || contactInfo.lastName == name) {
                    return dbContact
                }
            }
        } else {
            this.contactList.forEach { dbContact ->
                val contactInfo = dbContact.contactDB!!
                if (contactInfo.firstName == name && contactInfo.lastName == "" || contactInfo.firstName == "" && contactInfo.lastName == name) {
                    return dbContact
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

                println("contact " + dbContact.contactDB.toString() + " name of contact =" + name)
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

    fun setToContactInListPriority2() {
        for (contact in this.contactList) {
            val callDb = Callable {
                //contactsDatabase!!.contactsDao().setPriority2(contact.getContactId())
                contact.setPriority2(contactsDatabase)
            }
            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
            val result = executorService.submit(callDb)
            result.get()
        }
    }
}