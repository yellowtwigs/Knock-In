package com.example.knocker.model.ModelDB

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
    fun getPhoneNumber():String?{
       for(detail in contactDetailList!!){
           if(detail.type.equals("phone")){

               return detail.content.dropLast(1)
           }
       }
        return null
    }

}
