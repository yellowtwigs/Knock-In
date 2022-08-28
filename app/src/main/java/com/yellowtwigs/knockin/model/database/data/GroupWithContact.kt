package com.yellowtwigs.knockin.model.database.data

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.yellowtwigs.knockin.model.database.ContactsDatabase

class GroupWithContact {
    fun getListContact(context: Context): ArrayList<ContactDB> {
        val contactRoom = ContactsDatabase.getDatabase(context)
        val listContact: ArrayList<ContactDB> = arrayListOf()
        for (idContact in ContactIdList!!) {
//            listContact.add(contactRoom!!.contactsDao().getContact(idContact))
        }
        return listContact
    }

    @Embedded
    var groupDB: GroupDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_group", entity = LinkContactGroup::class, projection = ["id_contact"])
    var ContactIdList: List<Int>? = null
}