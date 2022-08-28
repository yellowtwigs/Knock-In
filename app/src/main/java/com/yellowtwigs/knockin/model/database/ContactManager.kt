package com.yellowtwigs.knockin.model.database//package com.yellowtwigs.knockin.model
//
//import android.content.ContentResolver
//import android.content.ContentUris
//import android.content.Context
//import android.content.SharedPreferences
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.provider.ContactsContract
//import android.telephony.PhoneNumberUtils
//import com.yellowtwigs.knockin.R
//import com.yellowtwigs.knockin.model.data.*
//import org.json.JSONObject
//import java.io.ByteArrayInputStream
//import java.io.InputStream
//import java.util.*
//import java.util.concurrent.Callable
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//
//class ContactManager(var contactList: ArrayList<ContactWithAllInformation>, var context: Context) {
//    constructor(context: Context) : this(arrayListOf<ContactWithAllInformation>(), context)
//
//    private var mDbWorkerThread: DbWorkerThread = DbWorkerThread("dbWorkerThread")
//    private var contactsDatabase: ContactsDatabase? = null
//
//    init {
//        mDbWorkerThread.start()
//        contactsDatabase = ContactsDatabase.getDatabase(context)
//        if (contactList.isEmpty()) {
//            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//            val callDb = Callable {
//                contactsDatabase!!.contactsDao().getContactAllInfo()
//            }
//            val result = executorService.submit(callDb)
//            val tmp: ArrayList<ContactWithAllInformation> = arrayListOf()
//            tmp.addAll(result.get())
//            contactList = tmp
////            buildContactListFromJson(context)
//
//        }
//    }
//
//
//    fun sortContactByGroup() {
//        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//        val callDb = Callable { contactsDatabase!!.contactsDao().sortContactByFirstNameAZ() }
//        val result = executorService.submit(callDb)
//        val listChangement: ArrayList<ContactWithAllInformation> = ArrayList()
//        listChangement.addAll(result.get())
//        val listTmp: ArrayList<ContactWithAllInformation> = ArrayList()
//        for (i in listChangement) {
//            if (i.getFirstGroup(context) == null) {
//                listTmp.add(i)
//            }
//        }
//        val listOfContactWithGroup: ArrayList<ContactWithAllInformation> = ArrayList()
//        listOfContactWithGroup.addAll(listChangement)
//        listOfContactWithGroup.removeAll(listTmp)
//        listOfContactWithGroup.sortBy { it.getFirstGroup(context)?.name?.toUpperCase() }//selector(listChangement.get(i))}
//        listChangement.removeAll(listOfContactWithGroup)
//        listChangement.addAll(
//            0,
//            listOfContactWithGroup
//        )
//        contactList = listChangement
//    }
//
//    //endregion
//
//    //region Filter
//    private fun getAllContactFilter(filterList: ArrayList<String>): List<ContactWithAllInformation>? {
//        val allFilters: MutableList<List<ContactWithAllInformation>> = mutableListOf()
//        var filter: List<ContactWithAllInformation>?
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
//        if (allFilters.size > 1) {
//            while (i < allFilters.size - 1) {
//                allFilters[i + 1] = allContactIntersect(allFilters[i], allFilters[i + 1])
//                i++
//            }
//        } else if (allFilters.size == 0) {
//            return null
//        } else
//            return allFilters[0]
//        return allFilters[i]
//    }
//
//    private fun allContactIntersect(
//        firstList: List<ContactWithAllInformation>,
//        secondList: List<ContactWithAllInformation>
//    ): List<ContactWithAllInformation> {
//        val filter = arrayListOf<ContactWithAllInformation>()
//        firstList.forEach { first ->
//            secondList.forEach {
//                if (first.getContactId() == it.getContactId())
//                    filter.add(first)
//            }
//        }
//        return filter
//    }
//
//    fun getContactConcernByFilter(
//        filterList: ArrayList<String>,
//        name: String
//    ): List<ContactWithAllInformation> {
//        val contactFilterList: List<ContactWithAllInformation>? = getAllContactFilter(filterList)
//        val contactList = getContactByName(name)
//        if (contactFilterList != null) {
//            return intersectContactWithAllInformation(contactList, contactFilterList)
//        }
//        return contactList
//    }
//
//    //endregion
//
//    private fun searchInPhoneNumber(
//        name: String,
//        listContact: List<ContactWithAllInformation>
//    ): List<ContactWithAllInformation> {
//        val finalList = arrayListOf<ContactWithAllInformation>()
//        listContact.forEach { contact ->
//            if (contact.contactDetailList != null) {
//                contact.contactDetailList!!.forEach {
//                    if (it.type == "phone" && isSameNumber(name, it.content)) {
//                        finalList.add(contact)
//                    }
//                }
//            }
//        }
//        return finalList
//    }
//
//    private fun isSameNumber(phoneNumberSB: String, phoneNumber: String): Boolean {
//        var epurePhoneNumberSB = phoneNumberSB.replace("\\s".toRegex(), "")
//        var epurePhoneNumber = phoneNumber.replace("\\s".toRegex(), "")
//        if (epurePhoneNumberSB.contains("+33")) {
//            epurePhoneNumberSB = epurePhoneNumberSB.removePrefix("+33")
//            epurePhoneNumberSB = "0$epurePhoneNumberSB"
//        }
//        if (epurePhoneNumber.contains("+33")) {
//            epurePhoneNumber = epurePhoneNumber.removePrefix("+33")
//            epurePhoneNumber = "0$epurePhoneNumber"
//        }
//        if (epurePhoneNumber.contains(epurePhoneNumberSB))
//            return true
//        return false
//    }
//
//    private fun intersectContactWithAllInformation(
//        contactList: List<ContactWithAllInformation>,
//        contactFilterList: List<ContactWithAllInformation>
//    ): List<ContactWithAllInformation> {
//        val listContacts = mutableListOf<ContactWithAllInformation>()
//        contactFilterList.forEach { type ->
//            contactList.forEach {
//                if (type.getContactId() == it.getContactId())
//                    listContacts.add(type)
//            }
//        }
//        return listContacts
//    }
//
//    var defaultTone = R.raw.sms_ring
//    private fun getContactFromJSONObject(json: JSONObject, id: Int): ContactWithAllInformation {
//        val firstName: String = json.getString("first_name")
//        val lastName: String = json.getString("last_name")
//
//        val profilPicture: Int = R.drawable.ic_user_blue
//        val contactPriority: Int = json.getInt("contact_priority")
//        val profilPictureStr: String = json.getString("profile_picture_str")
//        val contact = ContactDB(
//            id,
//            firstName,
//            lastName,
//            "",
//            profilPicture,
//            contactPriority,
//            profilPictureStr,
//            0,
//            "",
//            0,
//            "",
//            defaultTone,
//            0,
//            1,
//            "",
//            "", 0, 0
//        )
//        val contactInfo = ContactWithAllInformation()
//        contactInfo.contactDB = contact
//        contactInfo.contactDetailList = getContactDetailFromJSONObject(json, id)
//        return contactInfo
//    }
//
//    private fun getContactDetailFromJSONObject(
//        json: JSONObject,
//        idContact: Int
//    ): List<ContactDetailDB> {
//        val phoneNumber: String = json.getString("phone_number")
//        val mail: String = json.getString("mail")
//
//        val contactDetails = ContactDetailDB(null, idContact, phoneNumber, "phone", "", 0)
//        val contactDetails2 = ContactDetailDB(null, idContact, mail, "mail", "", 1)
//        return mutableListOf(contactDetails, contactDetails2)
//    }
//
//    //endregion
//
//    //region region ContactSync
//
//    /**
//     * fonction qui permet de vérifier si le numéro de téléphone n'est pas déja enregister.
//     * @param idAndPhoneNumber Map<Int, Any>
//     * @param contactPhoneNumber List<Map<Int, Any>>
//     * @return Boolean
//     */
//    private fun isDuplicate(
//        id: Int,
//        contactPhoneNumber: List<Pair<Int, Triple<String, String, String?>>>
//    ): Boolean {
//        contactPhoneNumber.forEach {
//            if (it.first == id)
//                return true
//        }
//        return false
//    }
//
//    /**
//     * fonction qui permet de récuper les noms entier des contactList du carnet Android.
//     * @param main_contentResolver ContentResolver
//     * @return List<Pair<Int, Triple<String, String, String>>>?
//     */
//    private fun getStructuredNameSync(main_contentResolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>> {
//        val phoneContactsList =
//            arrayListOf<Pair<Int, Triple<String, String, String>>>()
//        var idAndName: Pair<Int, Triple<String, String, String>>
//        var structName: Triple<String, String, String>
//        val phoneContact = main_contentResolver.query(
//            ContactsContract.Data.CONTENT_URI,
//            null,
//            null,
//            null,
//            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
//        )
//        phoneContact?.apply {
//            while (moveToNext()) {
//                val phoneId =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)).toInt()
//                var firstName =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
//                var middleName =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
//                var lastName =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
//                val mimeType =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))
//                val appsInPhone =
//                    getString(getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.ACCOUNT_TYPE_AND_DATA_SET))
//
//                if (firstName == null && middleName == null && lastName == null) {
//                } else {
//                    val isNumeric = try {
//                        Integer.parseInt(firstName)
//                        true
//                    } catch (e: NumberFormatException) {
//                        false
//                    }
//                    if (!isNumeric) {
//                        if (firstName != null) {
//                            if (!firstName.contains("Telegram") && !firstName.contains("WhatsApp") &&
//                                !firstName.contains("com.google") && !firstName.contains("Signal")
//                            ) {
//
//                                if (lastName != null) {
//                                    if (appsInPhone != "com.google") {
//                                        listOfTriple.add(Triple(firstName, lastName, appsInPhone))
//                                    }
//                                } else {
//                                    if (appsInPhone != "com.google") {
//                                        listOfTriple.add(Triple(firstName, "", appsInPhone))
//                                    }
//                                }
//                            }
//                        } else {
//                            if (lastName != null) {
//                                if (appsInPhone != "com.google") {
//                                    listOfTriple.add(Triple("", lastName, appsInPhone))
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if (phoneContactsList.isEmpty() && mimeType == "vnd.android.cursor.item/name") {
//                    if (firstName == null)
//                        firstName = ""
//                    if (middleName == null)
//                        middleName = ""
//                    if (lastName == null)
//                        lastName = ""
//                    structName = Triple(firstName, middleName, lastName)
//                    idAndName = Pair(phoneId, structName)
//                    phoneContactsList.add(idAndName)
//                } else if (!isDuplicate(
//                        phoneId,
//                        phoneContactsList
//                    ) && mimeType == "vnd.android.cursor.item/name"
//                ) {
//                    if (firstName == null)
//                        firstName = ""
//                    if (middleName == null)
//                        middleName = ""
//                    if (lastName == null)
//                        lastName = ""
//                    structName = Triple(firstName, middleName, lastName)
//                    idAndName = Pair(phoneId, structName)
//                    phoneContactsList.add(idAndName)
//                }
//            }
//        }
//        phoneContact?.close()
//        return phoneContactsList
//    }
//
//    /**
//     * fonction qui permet de vérifier si le numéro de téléphone n'est pas déja enregister.
//     * @param idAndPhoneNumber Map<Int, Any>
//     * @param contactPhoneNumber List<Map<Int, Any>>
//     * @return Boolean
//     */
//    private fun isDuplicateNumber(
//        idAndPhoneNumber: Map<Int, Any>,
//        contactPhoneNumber: List<Map<Int, Any>>
//    ): Boolean {
//        contactPhoneNumber.forEach {
//            if (it[1] == idAndPhoneNumber[1] && it[2].toString()
//                    .replace("\\s".toRegex(), "") == idAndPhoneNumber[2].toString()
//                    .replace("\\s".toRegex(), "")
//            ) {
//                return true
//            }
//        }
//        return false
//    }
//
//    /**
//     * fonction qui permet de récuper la photo des contactList du carnet Android.
//     * @param contactId Long
//     * @param main_contentResolver ContentResolver
//     * @return InputStream?
//     */
//    private fun openPhoto(contactId: Long, main_contentResolver: ContentResolver): InputStream? {
//        val contactUri =
//            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
//        val photoUri =
//            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
//        val cursor = main_contentResolver.query(
//            photoUri,
//            arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null
//        ) ?: return null
//        try {
//            if (cursor.moveToFirst()) {
//                val data = cursor.getBlob(0)
//                if (data != null) {
//                    //on return la photo
//                    return ByteArrayInputStream(data)
//                }
//            }
//        } finally {
//            cursor.close()
//        }
//        return null
//    }
//
//    /**
//     * fonction qui permet de récuper les emails des contactList du carnet Android.
//     * @param main_contentResolver ContentResolver
//     * @return List<Map<Int, Any>>
//     */
//    private fun getContactMailSync(main_contentResolver: ContentResolver): List<Map<Int, Any>> {
//        val contactDetails = arrayListOf<Map<Int, Any>>()
//        var idAndMail: Map<Int, Any>
//        val phonecontact = main_contentResolver.query(
//            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
//            null,
//            null,
//            null,
//            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC"
//        )
//        while (phonecontact!!.moveToNext()) {
//            val phoneId =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID))
//            var phoneEmail =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
//            var phoneTag =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
//            if (phoneEmail == null)
//                phoneEmail = ""
//            if (phoneTag == null) {
//                phoneTag = "0"
//            }
//            idAndMail = mapOf(
//                1 to phoneId!!.toInt(),
//                2 to phoneEmail,
//                3 to assignTagEmail(phoneTag.toInt()),
//                4 to "",
//                5 to "mail"
//            )
//            if (contactDetails.isEmpty() || !isDuplicateNumber(idAndMail, contactDetails)) {
//                //ajoute la map à la liste
//                contactDetails.add(idAndMail)
//            }
//        }
//        phonecontact.close()
//        return contactDetails
//    }
//
//    /**
//     * fonction qui permet de récupérer les numéros de téléphone des contactList du carnet Android.
//     * @param main_contentResolver ContentResolver
//     * @return List<Map<Int, Any>>
//     */
//    private fun getPhoneNumberSync(main_contentResolver: ContentResolver): List<Map<Int, Any>> {
//        val contactPhoneNumber = arrayListOf<Map<Int, Any>>()
//        var idAndPhoneNumber: Map<Int, Any>
//        val phonecontact = main_contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            null,
//            null,
//            null,
//            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//        )
//        while (phonecontact?.moveToNext() == true) {
//            val phoneId =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
//            var phoneNumber =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//            var phonePic =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
//            var phoneTag =
//                phonecontact.getString(phonecontact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
//            if (phoneNumber == null)
//                phoneNumber = ""
//            if (phonePic == null || phonePic.contains(
//                    "content://com.android.contactList/contactList/",
//                    ignoreCase = true
//                )
//            ) {
//                phonePic = ""
//            } else {
//                val photo = openPhoto(
//                    phoneId?.toLong()!!,
//                    main_contentResolver
//                )
//                phonePic = if (photo != null) {
//                    bitmapToBase64(
//                        BitmapFactory.decodeStream(photo)
//                    )
//                } else {
//                    ""
//                }
//            }
//            if (phoneTag == null) {
//                phoneTag = "0"
//            }
//            //on stocke toute les infos d'un contact dans idAndPhoneNumber
//            idAndPhoneNumber = mapOf(
//                1 to phoneId!!.toInt(),
//                2 to phoneNumber,
//                3 to assignTagNumber(phoneTag.toInt()),
//                4 to phonePic,
//                5 to "phone"
//            )
//            if (contactPhoneNumber.isEmpty() || !isDuplicateNumber(
//                    idAndPhoneNumber,
//                    contactPhoneNumber
//                )
//            ) {
//                contactPhoneNumber.add(idAndPhoneNumber)
//            }
//        }
//        phonecontact?.close()
//        return contactPhoneNumber
//    }
//
//    /**
//     * fonction qui permet de récuper le tag d'un email du carnet de contact Android.
//     * @param intTag Int
//     * @return String
//     */
//    private fun assignTagEmail(intTag: Int): String {
//        var tag = "other"
//        when (intTag) {
//            1 -> tag = "home"
//            2 -> tag = "work"
//        }
//        return tag
//    }
//
//    /**
//     * fonction qui permet de récuper le tag d'un numero de téléphone du carnet de contact Android.
//     * @param intTag Int
//     * @return String
//     */
//    private fun assignTagNumber(intTag: Int): String {
//        var tag = "other"
//        when (intTag) {
//            1 -> tag = "home"
//            2 -> tag = "mobil"
//            3 -> tag = "work"
//        }
//        return tag
//    }
//
//    private fun isDuplicate(
//        contacts: List<ContactWithAllInformation>?,
//        phoneContactList: ContactDB
//    ): Boolean {
//        contacts?.forEach { contactsInfo ->
//            val contactsDB = contactsInfo.contactDB!!
//            if (contactsDB.firstName == phoneContactList.firstName && contactsDB.lastName == phoneContactList.lastName)
//                return true
//        }
//        return false//TODO
//    }
//
//    /**
//     * fonction qui permet de deserializer les contactList de la dernière sync.
//     * @param lastSync String
//     * @return List<Pair<Int, Int>>
//     */
//    fun sliceLastSync(lastSync: String): List<Pair<Int, Int>> {
//        val lastSyncList = arrayListOf<Pair<Int, Int>>()
//        var AllId: Pair<Int, Int>
//        val lastSyncSplit = lastSync.split("|")
//        lastSyncSplit.forEach {
//            if (it != "") {
//                val idSplit = it.split(":")
//                AllId = Pair(idSplit[0].toInt(), idSplit[1].toInt())
//                lastSyncList.add(AllId)
//            }
//        }
//        return lastSyncList
//    }
//
//    private fun createListContactsSync(
//        phoneStructName: List<Pair<Int, Triple<String, String, String>>>?,
//        contactNumberAndPic: List<Map<Int, Any>>,
//        contactGroup: List<Triple<Int, String?, String?>>,
//        contactManager: ContactManager
//    ) {
//        val phoneContactsList = arrayListOf<ContactDB>()
//        val lastId = arrayListOf<Int>()
//        val applicationContext = this.context
//        val sharedPreferences =
//            applicationContext.getSharedPreferences("save_last_sync", Context.MODE_PRIVATE)
//        val edit: SharedPreferences.Editor = sharedPreferences.edit()
//        var ContactLinksGroup: Pair<LinkContactGroup, GroupDB>
//        val listLinkAndGroup = arrayListOf<Pair<LinkContactGroup, GroupDB>>()
//        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//        var lastSyncId = ""
//        var lastSync = ""
//        val callDb = Callable {
//            val allcontacts = contactsDatabase?.contactsDao()?.sortContactByFirstNameAZ()
//            var modifiedContact = 0
//            phoneStructName?.forEachIndexed { _, fullName ->
//                val set = mutableSetOf<String>()
//                contactNumberAndPic.forEach { numberPic ->
//                    val id = numberPic[1].toString().toInt()
//                    if (!lastId.contains(id)) {
//                        val contactDetails = getDetailsById(id, contactNumberAndPic)
//                        val contactGroups = getGroupsAndLinks(id, contactGroup)
//                        if (fullName.first == numberPic[1]) {
//                            lastId.add(id)
//                            val listOfApps = arrayListOf<String>()
//
//                            for (triple in listOfTriple) {
//                                if (fullName.second.third != "") {
//                                    if (triple.first != "" && triple.second != "") {
//                                        if (fullName.second.third.contains(triple.first) &&
//                                            fullName.second.third.contains(triple.second)
//                                        ) {
//                                            listOfApps.add(triple.third)
//                                        }
//                                    }
//                                }
//                                if (fullName.second.first == triple.first && fullName.second.third == triple.second
//                                ) {
//                                    listOfApps.add(triple.third)
//                                } else if (fullName.second.third != "" && triple.first != "") {
//                                    if (fullName.second.third == triple.first) {
//                                        listOfApps.add(triple.third)
//                                    }
//                                } else if (fullName.second.first == "${triple.first} ${triple.second}" ||
//                                    fullName.second.third == "${triple.first} ${triple.second}"
//                                ) {
//                                    listOfApps.add(triple.third)
//                                } else if (fullName.second.third != "") {
//                                    if (triple.first != "" && triple.second != "") {
//                                        if (fullName.second.third.contains(triple.first) &&
//                                            fullName.second.third.contains(triple.second)
//                                        ) {
//                                            listOfApps.add(triple.third)
//                                        }
//                                    }
//                                } else if (fullName.second.third != "D Minvielle" && triple.second == "D Minvielle") {
//                                    listOfApps.add(triple.third)
//                                }
//                            }
//
//                            val hasTelegram = if (listOfApps.contains("org.telegram.messenger")) {
//                                1
//                            } else {
//                                0
//                            }
//
//                            val hasWhatsapp = if (listOfApps.contains("com.whatsapp")) {
//                                1
//                            } else {
//                                0
//                            }
//
//                            val hasSignal = if (listOfApps.contains("org.thoughtcrime.securesms")) {
//                                1
//                            } else {
//                                0
//                            }
//
//                            if (fullName.second.second == "") {
//                                val contacts = ContactDB(
//                                    null,
//                                    fullName.second.first,
//                                    fullName.second.third,
//                                    "",
//                                    randomDefaultImage(0, "Create"),
//                                    1,
//                                    numberPic[4].toString(),
//                                    0,
//                                    "",
//                                    hasWhatsapp,
//                                    "",
//                                    defaultTone,
//                                    0,
//                                    1,
//                                    "",
//                                    "",
//                                    hasTelegram,
//                                    hasSignal
//                                )
//                                lastSync = sharedPreferences.getString("last_sync_2", "")!!
//                                if (!isDuplicateContacts(fullName, lastSync)) {
//                                    contacts.id =
//                                        contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
//                                    lastSyncId += fullName.first.toString() + ":" + contacts.id.toString() + "|"
//                                    for (details in contactDetails) {
//                                        details.idContact = contacts.id
//                                    }
//                                    for (groups in contactGroups) {
//                                        val links = LinkContactGroup(0, contacts.id!!.toInt())
//                                        ContactLinksGroup = Pair(links, groups)
//                                        listLinkAndGroup.add(ContactLinksGroup)
//                                    }
//                                    saveGroupsAndLinks(listLinkAndGroup)
//                                    listLinkAndGroup.clear()
//                                    contactsDatabase!!.contactsDao().insertDetails(contactDetails)
//                                } else {
//                                    var positionInSet = 3
//                                    val contact = getContactWithAndroidId(fullName.first, lastSync)
//                                    if (contact != null) {
//                                        set.add("0" + contact.contactDB!!.id)
//                                        set.add("1" + fullName.second.first)
//                                        if (fullName.second.second == "")
//                                            set += "2" + fullName.second.third
//                                        else
//                                            set += "2" + fullName.second.second + " " + fullName.second.third
//                                        for (details in contactDetails) {
//                                            val alldetail =
//                                                details.type + ":" + details.content + ":" + details.tag
//                                            set += positionInSet.toString() + alldetail
//                                            positionInSet++
//                                        }
//                                        if (!isSameContact(
//                                                contact,
//                                                fullName.second,
//                                                contactDetails
//                                            )
//                                        ) {
//                                            modifiedContact++
//                                            edit.putStringSet(modifiedContact.toString(), set)
//                                            edit.apply()
//                                        }
//                                    } else {
//                                        lastSync =
//                                            deleteContactFromLastSync(lastSync, fullName.first)
//                                        edit.putString("last_sync_2", lastSync)
//                                        edit.apply()
//                                        contacts.id =
//                                            contactsDatabase?.contactsDao()?.insert(contacts)!!
//                                                .toInt()
//                                        lastSyncId += fullName.first.toString() + ":" + contacts.id.toString() + "|"
//                                        for (details in contactDetails) {
//                                            details.idContact = contacts.id
//                                        }
//                                        for (groups in contactGroups) {
//                                            val links = LinkContactGroup(0, contacts.id!!.toInt())
//                                            ContactLinksGroup = Pair(links, groups)
//                                            listLinkAndGroup.add(ContactLinksGroup)
//                                        }
//                                        saveGroupsAndLinks(listLinkAndGroup)
//                                        listLinkAndGroup.clear()
//                                        contactsDatabase?.contactsDao()
//                                            ?.insertDetails(contactDetails)
//                                    }
//                                }
//                                phoneContactsList.add(contacts)
//                            } else if (fullName.second.second != "") {
//                                val contacts = ContactDB(
//                                    null,
//                                    fullName.second.first,
//                                    fullName.second.second + " " + fullName.second.third,
//                                    "",
//                                    randomDefaultImage(0, "Create"),
//                                    1,
//                                    numberPic[4].toString(),
//                                    0,
//                                    "",
//                                    hasWhatsapp,
//                                    "",
//                                    defaultTone,
//                                    0,
//                                    1,
//                                    "",
//                                    "",
//                                    hasTelegram,
//                                    hasSignal
//                                )
//                                phoneContactsList.add(contacts)
//                                if (!isDuplicate(allcontacts, contacts)) {
//                                    contacts.id =
//                                        contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
//                                    for (details in contactDetails) {
//                                        details.idContact = contacts.id
//                                    }
//                                    contactsDatabase!!.contactsDao().insertDetails(contactDetails)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (lastSyncId != "") {
//                if (lastSync != "")
//                    lastSyncId = lastSync + lastSyncId
//                edit.putString("last_sync_2", lastSyncId)
//                edit.apply()
//            }
//            contactsDatabase?.contactsDao()?.getContactAllInfo()
//        }
//        val syncContact = executorService.submit(callDb).get()
//        contactManager.contactList.addAll(syncContact!!)
//    }
//
//    private fun deleteContactFromLastSync(lastSync: String, id: Int): String {
//        val list = mutableListOf<Pair<Int, Int>>()
//        val allId = sliceLastSync(lastSync)
//        var newList = ""
//        allId.forEach {
//            if (id != it.first)
//                list.add(Pair(it.first, it.second))
//        }
//        list.forEach {
//            newList += it.first.toString() + ":" + it.second.toString() + "|"
//        }
//        return newList
//    }
//
//    fun setToContactList(contactset: List<String>): Pair<ContactDB, List<ContactDetailDB>> {
//        val allContacts: Pair<ContactDB, List<ContactDetailDB>>
//        val detailList = arrayListOf<ContactDetailDB>()
//        for (i in 3 until contactset.size) {
//            val contactSetSplite = contactset.elementAt(i).split(":")
//            detailList.add(
//                ContactDetailDB(
//                    null,
//                    null,
//                    contactSetSplite[1],
//                    contactSetSplite[0].drop(1),
//                    contactSetSplite[2],
//                    i - 2
//                )
//            )
//        }
//        allContacts = Pair(
//            ContactDB(
//                contactset.elementAt(0).drop(1).toInt(),
//                contactset.elementAt(1).drop(1),
//                contactset.elementAt(2).drop(1),
//                "",
//                0,
//                0,
//                "",
//                0,
//                "",
//                0,
//                "",
//                defaultTone,
//                0,
//                1,
//                "",
//                "",
//                0,
//                0
//            ), detailList
//        )
//        return allContacts
//    }
//
//    private fun isSameContact(
//        KnockinContact: ContactWithAllInformation?,
//        fullname: Triple<String, String, String>,
//        contactDetail: List<ContactDetailDB>
//    ): Boolean {
//        var isSame = true
//        if (fullname.second != "") {
//            if (KnockinContact!!.contactDB!!.firstName != fullname.first || KnockinContact.contactDB!!.lastName != fullname.second + " " + fullname.third) {
//                return false
//            }
//        } else {
//            if (KnockinContact!!.contactDB!!.firstName != fullname.first || KnockinContact.contactDB!!.lastName != fullname.third) {
//                return false
//            }
//        }
//        if (KnockinContact.contactDetailList!!.size != contactDetail.size) {
//            return false
//        }
//        var alreadyCheck: Int
//        KnockinContact.contactDetailList!!.forEach { Knockin ->
//            alreadyCheck = 0
//            contactDetail.forEach {
//                if (alreadyCheck == 0 && (Knockin.type != it.type || Knockin.content != it.content || Knockin.tag != it.tag)) {
//                    isSame = false
//                } else {
//                    alreadyCheck = 1
//                    isSame = true
//                }
//            }
//            if (!isSame)
//                return false
//        }
//        return true
//    }
//
//    private fun isDuplicateContacts(
//        contacts: Pair<Int, Triple<String, String, String>>?,
//        lastSync: String?
//    ): Boolean {
//        if (lastSync != null) {
//            val allId = sliceLastSync(lastSync)
//            allId.forEach { Id ->
//                if (contacts!!.first == Id.first) {
//                    return true
//                }
//            }
//        }
//        return false
//    }
//
//    private fun saveGroupsAndLinks(listLinkAndGroup: List<Pair<LinkContactGroup, GroupDB>>) {
//        listLinkAndGroup.forEach {
//            val dbGroup = contactsDatabase?.GroupsDao()!!.getGroupWithName(it.second.name)
//            var groupId = 0
//            groupId = if (dbGroup == null) {
//                contactsDatabase?.GroupsDao()!!.insert(it.second)!!.toInt()
//            } else {
//                dbGroup.id!!.toInt()
//            }
//            if (groupId != 0) {
//                contactsDatabase?.LinkContactGroupDao()!!
//                    .insert(LinkContactGroup(groupId, it.first.idContact))
//            }
//        }
//    }
//
//    /**
//     * fonction qui permet de récupérer toutes les infos des contactList du carnet Android.
//     * @param main_contentResolver ContentResolver
//     */
//    fun getAllContactsInfoSync(main_contentResolver: ContentResolver) {
//        val phoneStructName = getStructuredNameSync(main_contentResolver)
//
//
//
//        val contactNumberAndPic = getPhoneNumberSync(main_contentResolver)
//        val contactDetail = contactNumberAndPic.union(contactMail)
//        val contactMail = getContactMailSync(main_contentResolver)
//        val contactGroup = getContactGroupSync(main_contentResolver)
//        contactList.clear()
//        createListContactsSync(phoneStructName, contactDetail.toList(), contactGroup, this)
//    }
//
//    //endregion
//
//    fun getContactWithName(name: String, platform: String): ContactWithAllInformation? {
//        when (platform) {
//            "message" -> {
//                return getContact(name)
//            }
//            "Message" -> {
//                return getContact(name)
//            }
//            "WhatsApp" -> {
//                return getContact(name)
//            }
//            "whatsApp" -> {
//                return getContact(name)
//            }
//            "gmail" -> {
//                return getContact(name)
//            }
//            "Gmail" -> {
//                return getContact(name)
//            }
//            "Outlook" -> {
//                return getContact(name)
//            }
//            "outlook" -> {
//                return getContact(name)
//            }
//            "Signal" -> {
//                return getContact(name)
//            }
//            "signal" -> {
//                return getContact(name)
//            }
//            "Messenger" -> {
//                return getContact(name)
//            }
//            "messenger" -> {
//                return getContact(name)
//            }
//            "Telegram" -> {
//                return getContact(name)
//            }
//            "telegram" -> {
//                return getContact(name)
//            }
//        }
//        return null
//    }
//
//    fun getContact(name: String): ContactWithAllInformation? {
//        if (name.contains(" ")) {
//            val array = name.toCharArray().toList()
//            val array2 = arrayListOf<Char>()
//            var canSpace = false
//            var oneTime = true
//            array.forEach { char ->
//                if (char.isLetter() || char == '-' || canSpace) {
//                    array2.add(char)
//                    canSpace = oneTime
//                    if (char.isWhitespace()) {
//                        canSpace = false
//                        oneTime = false
//                    }
//                }
//            }
//
//            val name1 = String(array2.toCharArray())
//            this.contactList.forEach { dbContact ->
//                val contactInfo = dbContact.contactDB!!
//
//                val firstnameList: ArrayList<Char> = arrayListOf()
//                var firstname = ""
//                val lastnameList: ArrayList<Char> = arrayListOf()
//                var lastname = ""
//
//                var cpt = 0
//
//                while (name[cpt] != ' ') {
//                    firstnameList.add(name[cpt]) // Kj, " " = 2
//                    cpt++
//                }
//                cpt++
//
//                for (i in 0 until firstnameList.size) {
//                    firstname += firstnameList[i]
//                }
//
//                while (cpt < name.length) {
//                    lastnameList.add(name[cpt]) // Kent, " " = 2
//                    cpt++
//                }
//
//                for (i in 0 until lastnameList.size) {
//                    lastname += lastnameList[i]
//                }
//
//                if (contactInfo.firstName + " " + contactInfo.lastName == name ||
//                    contactInfo.firstName == name || contactInfo.lastName == name ||
//                    " " + contactInfo.firstName + " " + contactInfo.lastName == name ||
//                    name1 == contactInfo.firstName + " " + contactInfo.lastName ||
//                    contactInfo.firstName == name1 || contactInfo.lastName == name1
//                ) {
//
//                    return dbContact
//
//                } else if (contactInfo.mail_name == name) {
//
//                    return dbContact
//
//                } else if (lastname == contactInfo.lastName && firstname == contactInfo.firstName) {
//                    return dbContact
//                } else {
//                    if (lastname == contactInfo.lastName) {
////                        if (name.contains("_") || name.contains(".") || name.contains("-") || name.contains("_")) {
//                        if (contactInfo.firstName.contains("-")) {
//                            // Kj Kent = Kenny-Jay Kent
//
//                            var cpt2 = 0
//                            val firstLetterList: ArrayList<Char> = arrayListOf()
//                            val lastLetterList: ArrayList<Char> = arrayListOf()
//                            var firstLetter = ""
//                            var lastLetter = ""
//
//                            while (contactInfo.firstName[cpt2] != '-') {
//                                firstLetterList.add(contactInfo.firstName[cpt2]) // K de Kenny
//                                firstLetter = firstLetterList[0].toString()
//                                cpt2++
//                            }
//                            cpt2++
//
//                            while (cpt2 < name.length) {
//                                lastLetterList.add(contactInfo.firstName[cpt2]) // J de Jay
//                                lastLetter = lastLetterList[0].toString()
//                                cpt2++
//                            }
//
//                            if (firstname == firstLetter + lastLetter ||
//                                firstname == firstLetter.toLowerCase() + lastLetter.toLowerCase() ||
//                                firstname == firstLetter + lastLetter.toLowerCase()
//                            ) {
//                                dbContact.contactDB!!.mail_name = name
//                                return dbContact
//                            }
//
//                        } else {
//                            // Ken Suon = Kenzy Suon
//                            if (contactInfo.firstName.contains(firstname)) {
//                                return dbContact
//                            }
//                        }
//
//                        // Jfc = Jean-Francois Coudeyre
//
//                    } else {
//                        var entireName = name.replace(name[0], ' ')
//                        entireName = entireName.replace(name[entireName.length - 1], ' ')
//
//                        if (' ' + contactInfo.firstName + " " + contactInfo.lastName + ' ' == entireName || ' ' + contactInfo.firstName + ' ' == entireName || ' ' + contactInfo.lastName + ' ' == entireName) {
//                            return dbContact
//                        }
//                    }
//                }
//            }
//        } else {
//            val array = name.toCharArray().toList()
//            val array2 = arrayListOf<Char>()
//            array.forEach { char ->
//                if (char.isLetter() || char == '-') {
//                    array2.add(char)
//                }
//            }
//            val name1 = String(array2.toCharArray())
//            contactList.forEach { dbContact ->
//                val contactInfo = dbContact.contactDB!!
//
//                if (contactInfo.firstName == name && contactInfo.lastName == "" || contactInfo.firstName == "" && contactInfo.lastName == name ||
//                    contactInfo.firstName == name1 && contactInfo.lastName == ""
//                ) {
//                    return dbContact
//                }
//            }
//        }
//        contactList.forEach { dbContact ->
//            val contactInfo = dbContact.contactDB!!
//
//            if (name.isNotEmpty()) {
//                var entireName = name.replace(name[0], ' ')
//                entireName = entireName.replace(name[entireName.length - 1], ' ')
//
//                if (' ' + contactInfo.firstName + " " + contactInfo.lastName + ' ' == entireName || ' ' + contactInfo.firstName + ' ' == entireName || ' ' + contactInfo.lastName + ' ' == entireName) {
//                    return dbContact
//                }
//            }
//        }
//
//        return null
//    }
//
//    fun getContactFromMail(name: String): ContactWithAllInformation? {
//        if (name.contains(" ")) {
//            val array = name.toCharArray().toList()
//            val array2 = arrayListOf<Char>()
//            var canSpace = false
//            var oneTime = true
//            array.forEach { char ->
//                if (char.isLetter() || char == '-' || canSpace) {
//                    array2.add(char)
//                    canSpace = oneTime
//                    if (char.isWhitespace()) {
//                        canSpace = false
//                        oneTime = false
//                    }
//                }
//            }
//
//            val name1 = String(array2.toCharArray())
//            this.contactList.forEach { dbContact ->
//                val contactInfo = dbContact.contactDB!!
//
//                val firstnameList: ArrayList<Char> = arrayListOf()
//                var firstname = ""
//                val lastnameList: ArrayList<Char> = arrayListOf()
//                var lastname = ""
//
//                var cpt = 0
//
//                while (name[cpt] != ' ') {
//                    firstnameList.add(name[cpt]) // Kj, " " = 2
//                    cpt++
//                }
//                cpt++
//
//                for (i in 0 until firstnameList.size) {
//                    firstname += firstnameList[i]
//                }
//
//                while (cpt < name.length) {
//                    lastnameList.add(name[cpt]) // Kent, " " = 2
//                    cpt++
//                }
//
//                for (i in 0 until lastnameList.size) {
//                    lastname += lastnameList[i]
//                }
//
//                if (dbContact.contactDB?.mail_name == name ||
//                    dbContact.contactDB?.mail_name == name1
//                ) {
//                    return dbContact
//                }
//            }
//        } else {
//            val array = name.toCharArray().toList()
//            val array2 = arrayListOf<Char>()
//            array.forEach { char ->
//                if (char.isLetter() || char == '-') {
//                    array2.add(char)
//                }
//            }
//            val name1 = String(array2.toCharArray())
//            contactList.forEach { dbContact ->
//                val contactInfo = dbContact.contactDB!!
//
//                if (contactInfo.mail_name == name || contactInfo.mail_name == name1) {
//                    return dbContact
//                }
//            }
//        }
//        return null
//    }
//
//
//    fun getContactFromNumber(phoneNumber: String): ContactWithAllInformation? {
//        for (contact in this.contactList) {
//            if (PhoneNumberUtils.compare(contact.getFirstPhoneNumber(), phoneNumber)) {
//                return contact
//            }
//        }
//        return null
//    }
//
//    fun setToContactInListPriority(priority: Int) {
//        for (contact in this.contactList) {
//            val callDb = Callable {
//                contact.setPriority(contactsDatabase, priority)
//            }
//            val executorService: ExecutorService = Executors.newFixedThreadPool(1)
//            val result = executorService.submit(callDb)
//            result.get()
//        }
//    }
//}