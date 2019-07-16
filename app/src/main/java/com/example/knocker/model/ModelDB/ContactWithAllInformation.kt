package com.example.knocker.model.ModelDB

import androidx.room.Embedded
import androidx.room.Relation
import com.example.knocker.model.ContactsRoomDatabase

class ContactWithAllInformation {
    @Embedded
    var contactDB: ContactDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_contact", entity = LinkContactGroup::class)
    var groupList: List<LinkContactGroup>? = null

    @Relation(parentColumn = "id", entityColumn = "id_contact", entity = ContactDetailDB::class)
    var contactDetailList: List<ContactDetailDB>? = null

    fun getContactId():Int{
        return contactDB!!.id!!
    }
    /*@Relation(parentColumn = "id",entityColumn = "id_contact",entity = Notification.class)
    public List<Notification> NotificationList;*/
    fun getPhoneNumber():String{
       for(detail in contactDetailList!!){
           if(detail.type.equals("phone")){

               return detail.content
           }
       }
        return ""
    }

    fun getFirstMail():String{
        for (detail in contactDetailList!!){
            if(detail.type.equals("mail")){
                return detail.content
            }
        }
        return ""
    }

    fun getPhoneNumberTag():String {
        for(detail in contactDetailList!!){
            if(detail.type.equals("phone")){
                return detail.tag
            }
        }
        return ""
    }

    fun getMailTag():String {
        for(detail in contactDetailList!!){
            if(detail.type.equals("mail")){
                return detail.tag
            }
        }
        return ""
    }

    fun setPriority2(contactsDatabase: ContactsRoomDatabase?) {
        val test=contactsDatabase!!.contactsDao().setPriority2(this.getContactId())
        println("test getContactID "+test)
    }

    override fun equals(other: Any?): Boolean {
        if(other is ContactWithAllInformation){
            if(other.contactDB!!.equals(this.contactDB)){
                println("is printing true")
                return true
            }else{
                return false
            }
        }else{
            return false
        }
        return super.equals(other)
    }


}
