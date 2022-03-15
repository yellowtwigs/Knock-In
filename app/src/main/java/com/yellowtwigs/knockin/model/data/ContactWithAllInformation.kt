package com.yellowtwigs.knockin.model.data

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.room.Embedded
import androidx.room.Relation
import com.yellowtwigs.knockin.model.ContactsDatabase

class ContactWithAllInformation {
    @Embedded
    var contactDB: ContactDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_contact", entity = LinkContactGroup::class)
    var groupList: List<LinkContactGroup>? = null

    @Relation(parentColumn = "id", entityColumn = "id_contact", entity = ContactDetailDB::class)
    var contactDetailList: List<ContactDetailDB>? = null

    fun getContactId(): Int {
        return contactDB!!.id!!
    }

    fun getMessengerID(): String {
        return contactDB!!.messengerId
    }

    /*@Relation(parentColumn = "id",entityColumn = "id_contact",entity = Notification.class)
    public List<Notification> NotificationList;*/
    fun getFirstPhoneNumber(): String {
        val regex = "((\\+33)|0|(\\+33 ))([67])(( [0-9]{2})|([0-9]{2})){4}".toRegex()
        var onlyFix = ""
        for (detail in contactDetailList!!) {
            if (detail.type == "phone" && detail.content.matches(regex)) {
                return detail.content
            } else if (detail.type == "phone" && onlyFix == "") {
                onlyFix = detail.content
            }
        }
        return onlyFix
    }

    fun getSecondPhoneNumber(firstPhoneNumber: String): String {

        for (detail in contactDetailList!!) {
            if (detail.type == "phone" && firstPhoneNumber != detail.content) {
                return detail.content
            }
        }
        return ""
    }

    fun getSecondPhoneTag(firstPhoneNumber: String): String {
        for (detail in contactDetailList!!) {
            if (detail.type == "phone" && firstPhoneNumber != detail.content) {
                return detail.tag
            }
        }
        return ""
    }

    fun getFirstMail(): String {
        for (detail in contactDetailList!!) {
            if (detail.type == "mail") {
                return detail.content
            }
        }
        return ""
    }

    fun getPhoneNumberTag(): String {
        val regex = "((\\+33)|0){1}([67]){1}(( [0-9]{2})|([0-9]{2})){4}".toRegex()
        var onlyFix = ""
        for (detail in contactDetailList!!) {

            if (detail.type == "phone" && detail.content.matches(regex)) {
                return detail.tag
            } else if (detail.type == "phone" && onlyFix.isEmpty()) {
                onlyFix = detail.tag
            }
        }
        return onlyFix
    }

    fun getMailTag(): String {
        for (detail in contactDetailList!!) {
            if (detail.type == "mail") {
                return detail.tag
            }
        }
        return ""
    }

    fun setPriority(contactsDatabase: ContactsDatabase?, priority: Int) {
        contactsDatabase?.contactsDao()?.setPriority(this.getContactId(), priority)
    }

    fun setIsFavorite(contactsDatabase: ContactsDatabase?) {
        contactsDatabase?.contactsDao()?.setIsFavorite(this.getContactId())
    }

    fun setIsNotFavorite(contactsDatabase: ContactsDatabase?) {
        contactsDatabase?.contactsDao()?.setIsNotFavorite(this.getContactId())
    }

    fun setHasWhatsapp(contactsDatabase: ContactsDatabase?) {
        contactsDatabase?.contactsDao()?.setHasWhatsapp(this.getContactId())
    }

    fun setHasNotWhatsapp(contactsDatabase: ContactsDatabase?) {
        contactsDatabase?.contactsDao()?.setHasNotWhatsapp(this.getContactId())
    }

    fun setNotification(contactsDatabase: ContactsDatabase?, alarmTone: Int) {
        contactDB?.id?.let { contactsDatabase?.contactsDao()?.setNotification(it, alarmTone.toString()) }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ContactWithAllInformation) {
            if (other.contactDB == this.contactDB) {
                println("is printing true")
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun getFirstGroup(context: Context): GroupDB? {
        val contactRoom = ContactsDatabase.getDatabase(context)
        return if (groupList != null && groupList?.isNotEmpty() == true) {
            groupList?.get(0)?.idGroup?.let { contactRoom?.GroupsDao()?.getGroupById(it)?.asLiveData()?.value }
        } else {
            null
        }
    }
}
