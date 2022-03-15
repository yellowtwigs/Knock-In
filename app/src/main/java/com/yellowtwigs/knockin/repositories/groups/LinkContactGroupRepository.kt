package com.yellowtwigs.knockin.repositories.groups

import com.yellowtwigs.knockin.model.dao.LinkContactGroupDao
import com.yellowtwigs.knockin.model.data.LinkContactGroup
import javax.inject.Inject

class LinkContactGroupRepository @Inject constructor(private val dao: LinkContactGroupDao) {

    fun getAllLinkContactGroup() = dao.getAllLinkContactGroup()

    suspend fun insert(linkContactGroup: LinkContactGroup) = dao.insert(linkContactGroup)

    suspend fun deleteContactInGroup(idContact: Int, idGroup: Int) =
        dao.deleteContactInGroup(idContact, idGroup)
}