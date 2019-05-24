package com.example.knocker.model.ModelDB

import androidx.room.Embedded
import androidx.room.Relation

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


}
