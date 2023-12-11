package com.yellowtwigs.knockin.ui.first_launch.start

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
import com.yellowtwigs.knockin.utils.NotificationsGesture
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
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
        ids.addAll(getAllAndroidIdsUseCase.invoke())

        val structuredNameSync = getStructuredNameSync(contentResolver)
        val contactDetails = getContactDetailsSync(contentResolver)
        val contactGroup = getContactGroupSync(contentResolver)

        createListContactsSync(contentResolver, structuredNameSync, contactDetails.toList(), contactGroup)
    }

    private fun getStructuredNameSync(resolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>> {
        val phoneContactsList = arrayListOf<Pair<Int, Triple<String, String, String>>>()
        var idAndName: Pair<Int, Triple<String, String, String>>
        var structName: Triple<String, String, String>
        val phoneContact = resolver.query(
            ContactsContract.Data.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        )
        phoneContact?.apply {
            while (moveToNext()) {
                try {
                    val phoneId = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)).toInt()
                    var firstName = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                    var middleName = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                    var lastName = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    val mimeType = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))
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
                                if (!firstName.contains("Telegram") && !firstName.contains(NotificationsGesture.WHATSAPP_APP_NAME) && !firstName.contains(
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
            var lastId = ""
            var savedPhoneNumberWithType = ""
            var savedPhoneNumber = ""
            var savedPhoneType = ""
            var savedPhoto = ""

            while (moveToNext()) {
                val phoneId = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val phoneType = if (getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)) == null) {
                    ""
                } else {
                    getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                }
                val phoneNumber = if (getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) == null) {
                    ""
                } else {
                    getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }

//                try {
//                    setSendToVoicemailFlag(phoneId, resolver)
//                } catch (e: Exception) {
//                    Log.i("PhoneCall", "e : $e")
//                }

                var phonePic = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                phonePic = if (phonePic == null || phonePic.contains("content://com.android.contactList/contactList/", ignoreCase = true)) {
                    ""
                } else {
                    val photo = phoneId?.toLong()?.let { openPhoto(it, resolver) }
                    if (photo != null) {
                        bitmapToBase64(BitmapFactory.decodeStream(photo))
                    } else {
                        ""
                    }
                }

                if (lastId == "") {
                    lastId = phoneId
                    savedPhoneNumberWithType = "$phoneType:$phoneNumber"
                    savedPhoneNumber = phoneNumber
                    savedPhoneType = phoneType
                    savedPhoto = phonePic
                } else {
                    if (lastId == phoneId) {
                        if (savedPhoneNumber == phoneNumber && savedPhoneType == phoneType) {

                        } else if (phoneNumber.contains("-") || savedPhoneNumber.contains("-")) {
                            if (phoneNumber.replace("-", "") == savedPhoneNumber || savedPhoneNumber.replace(
                                    "-", ""
                                ) == phoneNumber
                            ) {

                            } else {
                                if (savedPhoneType == "2") {
                                } else if (phoneType == "2") {
                                    savedPhoneNumberWithType = "$phoneType:$phoneNumber"
                                    savedPhoneNumber = phoneNumber
                                    savedPhoneType = phoneType
                                    savedPhoto = phonePic
                                } else {
                                }
                            }
                        } else if (phoneNumber.contains(" ") || savedPhoneNumber.contains(" ")) {
                            if (phoneNumber.replace(" ", "") == savedPhoneNumber || savedPhoneNumber.replace(
                                    " ", ""
                                ) == phoneNumber
                            ) {

                            } else {
                                if (savedPhoneType == "2") {
                                } else if (phoneType == "2") {
                                    savedPhoneNumberWithType = "$phoneType:$phoneNumber"
                                    savedPhoneNumber = phoneNumber
                                    savedPhoneType = phoneType
                                    savedPhoto = phonePic
                                } else {
                                }
                            }
                        } else {
                            if (savedPhoneType == "2") {
                            } else if (phoneType == "2") {
                                savedPhoneNumberWithType = "$phoneType:$phoneNumber"
                                savedPhoneNumber = phoneNumber
                                savedPhoneType = phoneType
                                savedPhoto = phonePic
                            } else {
                            }
                        }
                    } else {
                        contactDetails = mutableMapOf(
                            1 to lastId.toInt(), 2 to savedPhoneNumberWithType, 3 to savedPhoto
                        )
                        if (listOfDetails.isEmpty() || !listOfDetails.contains(contactDetails)) {
                            listOfDetails.add(contactDetails)
                        }

                        lastId = phoneId
                        savedPhoneNumberWithType = "$phoneType:$phoneNumber"
                        savedPhoneNumber = phoneNumber
                        savedPhoneType = phoneType
                        savedPhoto = phonePic
                    }
                }

                if (isLast) {
                    contactDetails = mutableMapOf(
                        1 to phoneId.toInt(), 2 to "$phoneType:$phoneNumber", 3 to phonePic
                    )
                    if (listOfDetails.isEmpty() || !listOfDetails.contains(contactDetails)) {
                        listOfDetails.add(contactDetails)
                    }
                }
            }
            close()
        }

        emailContact?.apply {
            while (moveToNext()) {
                val phoneId = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.CONTACT_ID))

                val phoneEmail = if (getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)) == null) {
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

    private fun openPhoto(contactId: Long, resolver: ContentResolver): InputStream? {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
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

    private fun sliceLastSync(lastSync: String): List<Pair<Int, Int>> {
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
        contentResolver: ContentResolver,
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

                            val listOfPhoneNumbers = arrayListOf<String>()
                            listOfPhoneNumbers.add(details[2].toString())

                            val listOfMails = mutableListOf<String>()

                            if (details[4] != null && details[4].toString().isNotBlank() && details[4].toString().isNotEmpty()) {
                                listOfMails.add(details[4].toString())
                            } else {
                            }


                            Log.i("GetIsStarred", "${fullFullName.uppercase().unAccent().replace("\\s".toRegex(), "")}")
                            val isStarred = if (isContactStarred(contentResolver, id)) 2 else 1

                            createContactUseCase.invoke(
                                ContactDB(
                                    0,
                                    id,
                                    fullFullName.uppercase().unAccent().replace("\\s".toRegex(), ""),
                                    fullName.second.first + secondName,
                                    fullName.second.third,
                                    randomDefaultImage(0, context, "Create"),
                                    details[3].toString(),
                                    listOfPhoneNumbers,
                                    listOfMails,
                                    "",
                                    isStarred,
                                    0,
                                    "",
                                    listOfApps,
                                    "",
                                    -1,
                                    0,
                                    0,
                                    "",
                                    ""
                                )
                            )
                        }
                    }
                }
            }
            var groupIsDone = false
            var groupIsAlreadyInDB = false
            var groupTriple = ""

            contactGroup.forEachIndexed { index, triple ->
                if (!groupsName.contains(triple.third!!)) {
                    groupTriple = triple.third.toString()
                    groupsName.add(triple.third!!)
                }
                contactsInGroup.add(triple.second!!)

                if (index + 1 < contactGroup.size) {
                    groupIsDone = contactGroup[index + 1].third!! != triple.third!!
                }

                if (groupIsDone || groupTriple != triple.third || index == contactGroup.lastIndex) {
                    if (manageGroupRepository.getAllGroups().isEmpty()) {
                        val group = GroupDB(0, triple.third!!, "", -500138, contactsInGroup, 1)
                        manageGroupRepository.insertGroup(group)
                    } else {
                        manageGroupRepository.getAllGroups().forEach { groupDb ->
                            if (groupDb.name == triple.third!! && groupDb.listOfContactsData == contactsInGroup) {
                                groupIsAlreadyInDB = true
                            }
                        }
                        if (!groupIsAlreadyInDB) {
                            val group = GroupDB(0, triple.third!!, "", -500138, contactsInGroup, 1)
                            manageGroupRepository.insertGroup(group)
                        }
                    }

                    contactsInGroup.clear()
                }
            }
        }
    }

    private fun isContactStarred(contentResolver: ContentResolver, contactId: Int): Boolean {
        val projection = arrayOf(ContactsContract.Contacts.STARRED)
        val selection = "${ContactsContract.Contacts._ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                if (columnIndex >= 0) {
                    val starred = it.getInt(columnIndex)
                    Log.i("GetIsStarred", "$starred")
                    return starred == 1
                }
            }
        }

        return false
    }

    private fun isStringTotallyEmpty(value: String): Boolean {
        return value.isEmpty() || value.isBlank() || value == ""
    }

    //region ============================================ GROUP =============================================

    private fun getContactGroupSync(resolver: ContentResolver): List<Triple<Int, String?, String?>> {
        val phoneContact = resolver.query(
            ContactsContract.Groups.CONTENT_URI, null, null, null, ContactsContract.Groups.TITLE + " ASC"
        )
        var allGroupMembers = listOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val groupId = phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Groups._ID))
            var groupName = phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Groups.TITLE))
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

        return allGroupMembers
    }

    private fun getMemberOfGroup(
        resolver: ContentResolver, groupId: String, groupName: String?
    ): List<Triple<Int, String?, String?>> {
        val where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId
        val phoneContact = resolver.query(
            ContactsContract.Data.CONTENT_URI, null, where, null, ContactsContract.Data.DISPLAY_NAME + " ASC"
        )
        var member: Triple<Int, String?, String?>
        val groupMembers = arrayListOf<Triple<Int, String?, String?>>()
        while (phoneContact?.moveToNext() == true) {
            val contactId = phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
            val contactName = phoneContact.getString(phoneContact.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME))

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
            if (it[1] == idAndPhoneNumber[1] && it[2].toString().replace("\\s".toRegex(), "") == idAndPhoneNumber[2].toString()
                    .replace("\\s".toRegex(), "")
            ) {
                return true
            }
        }
        return false
    }

    private fun isDuplicateGroup(
        member: List<Triple<Int, String?, String?>>, groupMembers: List<Triple<Int, String?, String?>>
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