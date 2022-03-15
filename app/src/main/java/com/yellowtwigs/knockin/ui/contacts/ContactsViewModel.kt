package com.yellowtwigs.knockin.ui.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.repositories.contacts.ContactsRepository
import com.yellowtwigs.knockin.utils.Converter.bitmapToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(private val repository: ContactsRepository) :
    ViewModel() {

    val allContacts = repository.getAllContacts().asLiveData()
    fun getContactAllInfo() = repository.getContactAllInfo().asLiveData()
    fun getContact(id: Int) = repository.getContact(id).asLiveData()
    fun getContactByName(name: String) = repository.getContactByName(name).asLiveData()
    fun getContactForGroup(groupId: Int) = repository.getContactForGroup(groupId).asLiveData()
    fun getContactWithPhoneNumber() = repository.getContactWithPhoneNumber().asLiveData()
    fun getContactWithMail() = repository.getContactWithMail().asLiveData()

    suspend fun insertContact(contact: ContactDB): Long? {
        return repository.insertContact(contact)
    }

    fun insertDetails(contactDetails: List<ContactDetailDB>) = viewModelScope.launch {
        repository.insertDetails(contactDetails)
    }

    fun updateContact(contact: ContactDB) = viewModelScope.launch {
        repository.updateContact(contact)
    }

    fun deleteContact(contact: ContactDB) = viewModelScope.launch {
        repository.deleteContact(contact)
    }

    var listSelectedLiveData = MutableLiveData<List<ContactWithAllInformation>>()
    fun setListSelectedLiveData(contacts: List<ContactWithAllInformation>) {
        listSelectedLiveData.value = contacts
    }

    var contactLiveData = MutableLiveData<ContactWithAllInformation>()
    fun setContactLiveData(contact: ContactWithAllInformation) {
        contactLiveData.value = contact
    }


    fun getStructuredNameSync(resolver: ContentResolver): List<Pair<Int, Triple<String, String, String>>> {
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
        if (phoneContact != null) {
            while (phoneContact.moveToNext()) {
                val phoneId =
                    phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID))
                        .toInt()
                var firstName =
                    phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                Log.i("phoneContact", "$firstName")
                var middleName =
                    phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                var lastName =
                    phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                val mimeType =
                    phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE))

                if (phoneContactsList.isEmpty() && mimeType == "vnd.android.cursor.item/name") {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    structName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phoneId, structName)
                    phoneContactsList.add(idAndName)
                } else if (!isDuplicate(
                        phoneId,
                        phoneContactsList
                    ) && mimeType == "vnd.android.cursor.item/name"
                ) {
                    if (firstName == null)
                        firstName = ""
                    if (middleName == null)
                        middleName = ""
                    if (lastName == null)
                        lastName = ""
                    structName = Triple(firstName, middleName, lastName)
                    idAndName = Pair(phoneId, structName)
                    phoneContactsList.add(idAndName)
                }
            }
        }
        phoneContact?.close()
        return phoneContactsList
    }

    fun getContactMailSync(resolver: ContentResolver): List<Map<Int, Any>> {
        val contactDetails = arrayListOf<Map<Int, Any>>()
        var idAndMail: Map<Int, Any>
        val phoneContact = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC"
        )
        while (phoneContact?.moveToNext() == true) {
            val phoneId =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID))
            var phoneEmail =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
            var phoneTag =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
            if (phoneEmail == null)
                phoneEmail = ""
            if (phoneTag == null) {
                phoneTag = "0"
            }
            idAndMail = mapOf(
                1 to phoneId!!.toInt(),
                2 to phoneEmail,
                3 to assignTagEmail(phoneTag.toInt()),
                4 to "",
                5 to "mail"
            )
            if (contactDetails.isEmpty() || !isDuplicateNumber(idAndMail, contactDetails)) {
                contactDetails.add(idAndMail)
            }
        }
        phoneContact?.close()
        return contactDetails
    }

    fun getPhoneNumberSync(resolver: ContentResolver): List<Map<Int, Any>> {
        val contactPhoneNumber = arrayListOf<Map<Int, Any>>()
        var idAndPhoneNumber: Map<Int, Any>
        val phoneContact = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        while (phoneContact?.moveToNext() == true) {
            val phoneId =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
            var phoneNumber =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            var phonePic =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            var phoneTag =
                phoneContact.getString(phoneContact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
            if (phoneNumber == null)
                phoneNumber = ""
            if (phonePic == null || phonePic.contains(
                    "content://com.android.contactList/contactList/",
                    ignoreCase = true
                )
            ) {
                phonePic = ""
            } else {
                val photo = phoneId?.toLong()?.let { openPhoto(it, resolver) }
                phonePic = if (photo != null) {
                    bitmapToBase64(BitmapFactory.decodeStream(photo))
                } else {
                    ""
                }
            }
            if (phoneTag == null) {
                phoneTag = "0"
            }
            idAndPhoneNumber = mapOf(
                1 to phoneId!!.toInt(),
                2 to phoneNumber,
                3 to assignTagNumber(phoneTag.toInt()),
                4 to phonePic,
                5 to "phone"
            )
            if (contactPhoneNumber.isEmpty() || !isDuplicateNumber(
                    idAndPhoneNumber,
                    contactPhoneNumber
                )
            ) {
                contactPhoneNumber.add(idAndPhoneNumber)
            }
        }
        phoneContact?.close()
        return contactPhoneNumber
    }

    fun getContactWithAndroidId(androidId: Int, lastSync: String): ContactWithAllInformation? {
        var contact: ContactWithAllInformation? = null
        var id = -1
        val allId = sliceLastSync(lastSync)
        allId.forEach {
            if (androidId == it.first)
                id = it.second
        }
        if (id != -1) {
            contact = getContact(id).value
        }
        return contact
    }

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

    private fun assignTagNumber(intTag: Int): String {
        var tag = "other"
        when (intTag) {
            1 -> tag = "home"
            2 -> tag = "mobil"
            3 -> tag = "work"
        }
        return tag
    }

    private fun assignTagEmail(intTag: Int): String {
        var tag = "other"
        when (intTag) {
            1 -> tag = "home"
            2 -> tag = "work"
        }
        return tag
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
            if (id != it.first)
                list.add(Pair(it.first, it.second))
        }
        list.forEach {
            newList += it.first.toString() + ":" + it.second.toString() + "|"
        }
        return newList
    }

    //region ========================================= IS DUPLICATE =========================================

    fun isDuplicate(
        id: Int,
        phoneNumber: List<Pair<Int, Triple<String, String, String?>>>
    ): Boolean {
        phoneNumber.forEach {
            if (it.first == id)
                return true
        }
        return false
    }

    fun isDuplicateContacts(
        allContacts: Pair<Int, Triple<String, String, String>>?,
        lastSync: String?
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
        idAndPhoneNumber: Map<Int, Any>,
        contactPhoneNumber: List<Map<Int, Any>>
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

    //endregion
}