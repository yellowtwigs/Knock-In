package com.yellowtwigs.knockin.ui.first_launch.start

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.domain.contact.CreateContactUseCase
import com.yellowtwigs.knockin.domain.contact.GetAllAndroidIdsUseCase
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.repositories.groups.manage.ManageGroupRepository
import com.yellowtwigs.knockin.utils.Converter.bitmapToBase64
import com.yellowtwigs.knockin.utils.Converter.unAccent
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val createContactUseCase: CreateContactUseCase,
    private val getAllAndroidIdsUseCase: GetAllAndroidIdsUseCase,
    private val manageGroupRepository: ManageGroupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val listOfTriple = arrayListOf<Triple<String, String, String>>()
    private val ids = arrayListOf<Int>()

    suspend fun syncAllContactsInDatabase(contentResolver: ContentResolver) {
        Log.i("ImportContacts", "ImportContacts 1")
        Log.i(
            "ImportContacts",
            "getAllAndroidIdsUseCase.invoke() : ${getAllAndroidIdsUseCase.invoke()}"
        )
        ids.addAll(getAllAndroidIdsUseCase.invoke())
        Log.i("ImportContacts", "ids : $ids")

        val structuredNameSync = getStructuredNameSync(contentResolver)
        val contactDetails = getContactDetailsSync(contentResolver)
        val contactGroup = getContactGroupSync(contentResolver)

        createListContactsSync(structuredNameSync, contactDetails.toList(), contactGroup)
    }

    private fun getStructuredNameSync(resolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>> {
        val phoneContactsList = arrayListOf<Pair<Int, Triple<String, String, String>>>()
        var idAndName: Pair<Int, Triple<String, String, String>>
        var structName: Triple<String, String, String>
        val phoneContact = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        )
        phoneContact?.apply {
            while (moveToNext()) {
                try {
                    val phoneId =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)).toInt()
                    var firstName =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                    var middleName =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                    var lastName =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    val mimeType =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))
                    val appsInPhone =
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.ACCOUNT_TYPE_AND_DATA_SET))

                    if (firstName == null && middleName == null && lastName == null) {
                    } else {
                        val isNumeric = try {
                            Integer.parseInt(firstName)
                            true
                        } catch (e: NumberFormatException) {
                            false
                        }
                        if (!isNumeric) {
                            if (firstName != null) {
                                if (!firstName.contains("Telegram") && !firstName.contains("WhatsApp") && !firstName.contains(
                                        "com.google"
                                    ) && !firstName.contains("Signal")
                                ) {

                                    if (lastName != null) {
                                        if (appsInPhone != "com.google") {
                                            listOfTriple.add(
                                                Triple(
                                                    firstName, lastName, appsInPhone
                                                )
                                            )
                                        }
                                    } else {
                                        if (appsInPhone != "com.google") {
                                            listOfTriple.add(Triple(firstName, "", appsInPhone))
                                        }
                                    }
                                }
                            } else {
                                if (lastName != null) {
                                    if (appsInPhone != "com.google") {
                                        listOfTriple.add(Triple("", lastName, appsInPhone))
                                    }
                                }
                            }
                        }
                    }

                    if (phoneContactsList.isEmpty() && mimeType == "vnd.android.cursor.item/name") {
                        if (firstName == null) firstName = ""
                        if (middleName == null) middleName = ""
                        if (lastName == null) lastName = ""
                        structName = Triple(firstName, middleName, lastName)
                        idAndName = Pair(phoneId, structName)
                        phoneContactsList.add(idAndName)
                    } else if (!isDuplicate(
                            phoneId, phoneContactsList
                        ) && mimeType == "vnd.android.cursor.item/name"
                    ) {
                        if (firstName == null) firstName = ""
                        if (middleName == null) middleName = ""
                        if (lastName == null) lastName = ""
                        structName = Triple(firstName, middleName, lastName)
                        idAndName = Pair(phoneId, structName)
                        phoneContactsList.add(idAndName)
                    }
                } catch (e: IndexOutOfBoundsException) {
                    Log.i("IndexOutOfBoundsException", "$e")
                }
            }
            close()
        }
        return phoneContactsList
    }

    private fun getContactDetailsSync(resolver: ContentResolver): List<Map<Int, Any>> {
        val listOfDetails = arrayListOf<MutableMap<Int, Any>>()
        var contactDetails: MutableMap<Int, Any>

        val phoneNumberContact = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val emailContact = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC"
        )

        phoneNumberContact?.apply {
            while (moveToNext()) {
                val phoneId =
                    getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))

                val phoneNumber =
                    if (getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) == null) {
                        ""
                    } else {
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }

                var phonePic =
                    getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))


                phonePic = if (phonePic == null || phonePic.contains(
                        "content://com.android.contactList/contactList/", ignoreCase = true
                    )
                ) {
                    ""
                } else {
                    val photo = phoneId?.toLong()?.let { openPhoto(it, resolver) }
                    if (photo != null) {
                        bitmapToBase64(BitmapFactory.decodeStream(photo))
                    } else {
                        ""
                    }
                }

                contactDetails = mutableMapOf(
                    1 to phoneId!!.toInt(), 2 to phoneNumber, 3 to phonePic
                )

                if (listOfDetails.isEmpty() || !listOfDetails.contains(contactDetails)) {
                    listOfDetails.add(contactDetails)
                }
            }
            close()
        }

        emailContact?.apply {
            while (moveToNext()) {
                val phoneId =
                    getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.CONTACT_ID))

                val phoneEmail =
                    if (getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)) == null) {
                        ""
                    } else {
                        getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                    }

                listOfDetails.forEachIndexed { index, map ->
                    if (map[1] == phoneId?.toInt()) {
                        listOfDetails[index][4] = phoneEmail
                    }
                }
            }
            close()
        }

        return listOfDetails
    }

//    fun getContactWithAndroidId(androidId: Int, lastSync: String): ContactWithAllInformation? {
//        var contact: ContactWithAllInformation? = null
//        var id = -1
//        val allId = sliceLastSync(lastSync)
//        allId.forEach {
//            if (androidId == it.first)
//                id = it.second
//        }
//        if (id != -1) {
//            contact = getContact(id).value
//        }
//        return contact
//    }

    private fun openPhoto(contactId: Long, resolver: ContentResolver): InputStream? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        val cursor = resolver.query(
            photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null
        ) ?: return null
        cursor.apply {
            if (moveToFirst()) {
                val data = getBlob(0)
                if (data != null) {
                    return ByteArrayInputStream(data)
                }
            }
        }
        return null
    }

    fun sliceLastSync(lastSync: String): List<Pair<Int, Int>> {
        val lastSyncList = arrayListOf<Pair<Int, Int>>()
        var allId: Pair<Int, Int>
        val lastSyncSplit = lastSync.split("|")
        lastSyncSplit.forEach {
            if (it != "") {
                val idSplit = it.split(":")
                allId = Pair(idSplit[0].toInt(), idSplit[1].toInt())
                lastSyncList.add(allId)
            }
        }
        return lastSyncList
    }

    fun deleteContactFromLastSync(lastSync: String, id: Int): String {
        val list = mutableListOf<Pair<Int, Int>>()
        val allId = sliceLastSync(lastSync)
        var newList = ""
        allId.forEach {
            if (id != it.first) list.add(Pair(it.first, it.second))
        }
        list.forEach {
            newList += it.first.toString() + ":" + it.second.toString() + "|"
        }
        return newList
    }

    private suspend fun createListContactsSync(
        phoneStructName: List<Pair<Int, Triple<String, String, String>>>?,
        contactDetails: List<Map<Int, Any>>,
        contactGroup: List<Triple<Int, String?, String?>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val contactsInGroup = arrayListOf<String>()
            val groupsName = arrayListOf<String>()
            phoneStructName?.forEachIndexed { _, fullName ->
                contactDetails.forEach { details ->
                    val id = details[1].toString().toInt()

                    Log.i("ImportContacts", "id : $id")
                    Log.i("ImportContacts", "!ids.contains(id) : ${!ids.contains(id)}")

                    if (!ids.contains(id)) {
                        if (fullName.first == details[1]) {
                            ids.add(id)
                            val listOfApps = arrayListOf<String>()

                            for (triple in listOfTriple) {
                                if (fullName.second.third != "") {
                                    if (triple.first != "" && triple.second != "") {
                                        if (fullName.second.third.contains(triple.first) && fullName.second.third.contains(
                                                triple.second
                                            )
                                        ) {
                                            listOfApps.add(triple.third)
                                        }
                                    }
                                }
                                if (fullName.second.first == triple.first && fullName.second.third == triple.second) {
                                    listOfApps.add(triple.third)
                                } else if (fullName.second.third != "" && triple.first != "") {
                                    if (fullName.second.third == triple.first) {
                                        listOfApps.add(triple.third)
                                    }
                                } else if (fullName.second.first == "${triple.first} ${triple.second}" || fullName.second.third == "${triple.first} ${triple.second}") {
                                    listOfApps.add(triple.third)
                                } else if (fullName.second.third != "") {
                                    if (triple.first != "" && triple.second != "") {
                                        if (fullName.second.third.contains(triple.first) && fullName.second.third.contains(
                                                triple.second
                                            )
                                        ) {
                                            listOfApps.add(triple.third)
                                        }
                                    }
                                } else if (fullName.second.third != "D Minvielle" && triple.second == "D Minvielle") {
                                    listOfApps.add(triple.third)
                                }
                            }

                            val secondName = if (fullName.second.second != "") {
                                " ${fullName.second.second} "
                            } else {
                                ""
                            }

                            val fullFullName = if (isStringTotallyEmpty(fullName.second.second)) {
                                if (isStringTotallyEmpty(fullName.second.first)) {
                                    fullName.second.third
                                } else {
                                    if (isStringTotallyEmpty(fullName.second.third)) {
                                        fullName.second.first
                                    } else {
                                        fullName.second.first + " " + fullName.second.third
                                    }
                                }
                            } else {
                                if (isStringTotallyEmpty(fullName.second.first)) {
                                    "${fullName.second.second} " + fullName.second.third
                                } else {
                                    if (isStringTotallyEmpty(fullName.second.third)) {
                                        fullName.second.first + " ${fullName.second.second}"
                                    } else {
                                        fullName.second.first + " ${fullName.second.second} " + fullName.second.third
                                    }
                                }
                            }

                            val listOfPhoneNumbers = mutableListOf<String>()
                            listOfPhoneNumbers.add(details[2].toString())

                            val listOfMails = mutableListOf<String>()

                            if (details[4] != null && details[4].toString()
                                    .isNotBlank() && details[4].toString().isNotEmpty()
                            ) {
                                listOfMails.add(details[4].toString())
                            } else {
                            }

                            Log.i(
                                "FullFullName", "${
                                    fullFullName.uppercase().unAccent().replace("\\s".toRegex(), "")
                                        .toCharArray().toString()
                                }"
                            )

                            createContactUseCase.invoke(
                                ContactDB(
                                    0,
                                    id,
                                    fullFullName.uppercase().unAccent()
                                        .replace("\\s".toRegex(), ""),
                                    fullName.second.first + secondName,
                                    fullName.second.third,
                                    randomDefaultImage(0, context, "Create"),
                                    details[3].toString(),
                                    listOfPhoneNumbers,
                                    listOfMails,
                                    "",
                                    1,
                                    0,
                                    "",
                                    listOfApps,
                                    "",
                                    0,
                                    1,
                                    0,
                                    "",
                                    ""
                                )
                            )


//                            if (fullName.second.second == "") {
//                                val contacts = ContactDB(
//                                    0,
//                                    fullName.second.first,
//                                    fullName.second.third,
//                                    randomDefaultImage(0, context as Context, "Create"),
//                                    details[4].toString(),
//                                    details[0].,
//                                    ,
//                                    0,
//                                    "",
//                                    1,
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
//                                    randomDefaultImage(0, context),
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
//                                if (!isDuplicate(allContacts, contacts)) {
//                                    contacts.id =
//                                        contactsDatabase?.contactsDao()?.insert(contacts)!!.toInt()
//                                    for (details in contactDetails) {
//                                        details.idContact = contacts.id
//                                    }
//                                    contactsDatabase!!.contactsDao().insertDetails(contactDetails)
//                                }
//                            }
                        }
                    }
                }


            }
            // Restaurés depuis l'appareil Huawei - BLN-L21
            // Favorites

            var groupIsDone = false

            contactGroup.forEachIndexed { index, triple ->
                // Index : 1
                // Importés le 28/06/2022

                if (!groupsName.contains(triple.third!!)) {
                    // groupsName add Importés le 28/06/2022
                    groupsName.add(triple.third!!)
                }
                // contactsInGroup add 402
                contactsInGroup.add(triple.second!!)

                if (index + 1 < contactGroup.size) {
                    // Importés le 28/06/2022 != Coworkers
                    groupIsDone = contactGroup[index + 1].third!! != triple.third!!
                }

                if (groupIsDone) {
                    val group = GroupDB(
                        0, triple.third!!, "", -500138, contactsInGroup, 1
                    )
                    manageGroupRepository.insertGroup(group)
                    contactsInGroup.clear()
                }
            }
        }
    }

    private fun isStringTotallyEmpty(value: String): Boolean {
        return value.isEmpty() || value.isBlank() || value == ""
    }

    //region ============================================ GROUP =============================================

    private fun getContactGroupSync(resolver: ContentResolver): List<Triple<Int, String?, String?>> {
        val phoneContact = resolver.query(
            ContactsContract.Groups.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Groups.TITLE + " ASC"
        )
        var allGroupMembers = listOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val groupId =
                phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Groups._ID))
            var groupName =
                phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Groups.TITLE))
            if (groupName == "Starred in Android") {
                groupName = "Favorites"
            }

            if (groupName != "My Contacts") {
                val groupMembers = getMemberOfGroup(resolver, groupId.toString(), groupName)
                if (groupMembers.isNotEmpty() && allGroupMembers.isNotEmpty() && !isDuplicateGroup(
                        allGroupMembers, groupMembers
                    )
                ) {
                    allGroupMembers = allGroupMembers.union(groupMembers).toList()
                } else if (allGroupMembers.isEmpty()) allGroupMembers = groupMembers
            }
        }
        phoneContact?.close()

//        Log.i("getAllGroups", "$allGroupMembers")

        return allGroupMembers
    }

    private fun getMemberOfGroup(
        resolver: ContentResolver, groupId: String, groupName: String?
    ): List<Triple<Int, String?, String?>> {
        val where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId
        val phoneContact = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            where,
            null,
            ContactsContract.Data.DISPLAY_NAME + " ASC"
        )
        var member: Triple<Int, String?, String?>
        val groupMembers = arrayListOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val contactId =
                phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
            val contactName =
                phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME))

            member = Triple(contactId!!.toInt(), contactName, groupName)
            if (!groupMembers.contains(member)) {
                groupMembers.add(member)
            }
        }
        phoneContact?.close()
        return groupMembers
    }

    //endregion

    //region ========================================= IS DUPLICATE =========================================

    private fun isDuplicate(
        id: Int, phoneNumber: List<Pair<Int, Triple<String, String, String?>>>
    ): Boolean {
        phoneNumber.forEach {
            if (it.first == id) return true
        }
        return false
    }

    private fun isDuplicateContacts(
        allContacts: Pair<Int, Triple<String, String, String>>?, lastSync: String?
    ): Boolean {
        if (lastSync != null) {
            val allId = sliceLastSync(lastSync)
            allId.forEach { Id ->
                if (allContacts?.first == Id.first) {
                    return true
                }
            }
        }
        return false
    }

    private fun isDuplicateNumber(
        idAndPhoneNumber: Map<Int, Any>, contactPhoneNumber: List<Map<Int, Any>>
    ): Boolean {
        contactPhoneNumber.forEach {
            if (it[1] == idAndPhoneNumber[1] && it[2].toString()
                    .replace("\\s".toRegex(), "") == idAndPhoneNumber[2].toString()
                    .replace("\\s".toRegex(), "")
            ) {
                return true
            }
        }
        return false
    }

    private fun isDuplicateGroup(
        member: List<Triple<Int, String?, String?>>,
        groupMembers: List<Triple<Int, String?, String?>>
    ): Boolean {
        groupMembers.forEach { _ ->
            groupMembers.forEachIndexed { index, it ->
                if (it.third == member[0].third) return true
            }
        }
        return false
    }

    //endregion
}