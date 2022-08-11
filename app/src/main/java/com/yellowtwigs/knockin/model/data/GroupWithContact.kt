package com.yellowtwigs.knockin.model.data

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.yellowtwigs.knockin.model.ContactsRoomDatabase

class GroupWithContact {
    fun getListContact(context: Context): ArrayList<ContactWithAllInformation> {
        val contactRoom = ContactsRoomDatabase.getDatabase(context)
        val listContact: ArrayList<ContactWithAllInformation> = arrayListOf()
        for (idContact in ContactIdList!!) {
            listContact.add(contactRoom!!.contactsDao().getContact(idContact))
        }
        return listContact
    }

    @Embedded
    var groupDB: GroupDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_group", entity = LinkContactGroup::class, projection = ["id_contact"])
    var ContactIdList: List<Int>? = null
}