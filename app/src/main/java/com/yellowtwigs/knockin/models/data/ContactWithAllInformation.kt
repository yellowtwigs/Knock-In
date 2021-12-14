package com.yellowtwigs.knockin.models.data

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.yellowtwigs.knockin.model.ContactsRoomDatabase

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
        val regex = "((\\+33)|0|(\\+33 )){1}([67]){1}(( [0-9]{2})|([0-9]{2})){4}".toRegex()
        var onlyFix = ""
        for (detail in contactDetailList!!) {
            println(detail.content + "matches with regex ?" + detail.content.matches(regex))
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

    fun setPriority2(contactsDatabase: ContactsRoomDatabase?) {
        val test = contactsDatabase!!.contactsDao().setPriority2(this.getContactId())
        println("test getContactID $test")
    }

    fun setIsFavorite(contactsDatabase: ContactsRoomDatabase?) {
        val test = contactsDatabase!!.contactsDao().setIsFavorite(this.getContactId())
        println("test getContactID $test")
    }

    fun setIsNotFavorite(contactsDatabase: ContactsRoomDatabase?) {
        val test = contactsDatabase!!.contactsDao().setIsNotFavorite(this.getContactId())
        println("test getContactID $test")
    }

    fun setHasWhatsapp(contactsDatabase: ContactsRoomDatabase?) {
        val test = contactsDatabase!!.contactsDao().setHasWhatsapp(this.getContactId())
        println("test getContactID $test")
    }

    fun setHasNotWhatsapp(contactsDatabase: ContactsRoomDatabase?) {
        val test = contactsDatabase!!.contactsDao().setHasNotWhatsapp(this.getContactId())
        println("test getContactID $test")
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
        val contactRoom = ContactsRoomDatabase.getDatabase(context)
        if (groupList != null && groupList!!.isNotEmpty()) {
            return contactRoom!!.GroupsDao().getGroup(groupList!![0].idGroup)
        } else {
            return null
        }
    }
}
