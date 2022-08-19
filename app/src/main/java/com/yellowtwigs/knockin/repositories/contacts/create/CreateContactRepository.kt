package com.yellowtwigs.knockin.repositories.contacts.create

import com.yellowtwigs.knockin.model.data.ContactDB

interface CreateContactRepository {

    suspend fun insertContact(contact: ContactDB): Long
}