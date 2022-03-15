package com.yellowtwigs.knockin.model.data

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.room.Embedded
import androidx.room.Relation
import com.yellowtwigs.knockin.model.ContactsDatabase

class GroupWithContact {
    fun getListContact(context: Context): ArrayList<ContactWithAllInformation> {
        val contactRoom = ContactsDatabase.getDatabase(context)
        val listContact: ArrayList<ContactWithAllInformation> = arrayListOf()
        for (idContact in contactIdList!!) {
            contactRoom?.contactsDao()?.getContact(idContact)?.asLiveData()?.value?.let {
                listContact.add(
                    it
                )
            }
        }
        return listContact
    }

    @Embedded
    var groupDB: GroupDB? = null

    @Relation(parentColumn = "id", entityColumn = "id_group", entity = LinkContactGroup::class, projection = ["id_contact"])
    var contactIdList: List<Int>? = null
}