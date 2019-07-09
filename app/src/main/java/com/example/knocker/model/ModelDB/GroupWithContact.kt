package com.example.knocker.model.ModelDB

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.example.knocker.model.ContactsRoomDatabase

class GroupWithContact {
    fun getListContact(context: Context):ArrayList<ContactWithAllInformation>{
        val contactRoom=ContactsRoomDatabase.getDatabase(context)
        val listContact:ArrayList<ContactWithAllInformation> = arrayListOf<ContactWithAllInformation>()
        for(link in linkContactList!!) {
            listContact.add(contactRoom!!.contactsDao().getContact(link.idContact))
        }
        return listContact
    }

    @Embedded
    var groupDB: GroupDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_contact", entity = LinkContactGroup::class,projection = arrayOf("id_group"))
    var linkContactList: List<LinkContactGroup>? = null

    //var contactList:List<ContactDB>?= null

}