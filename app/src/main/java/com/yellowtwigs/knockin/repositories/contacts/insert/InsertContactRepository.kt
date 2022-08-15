package com.yellowtwigs.knockin.repositories.contacts.insert

import com.yellowtwigs.knockin.model.data.ContactDB

interface InsertContactRepository {

    suspend fun insertContact(contact: ContactDB): Long
}