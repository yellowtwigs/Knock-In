package com.yellowtwigs.knockin.repositories.contacts.create

import com.yellowtwigs.knockin.model.database.data.ContactDB

interface CreateContactRepository {

    suspend fun insertContact(contact: ContactDB): Long
}