package com.yellowtwigs.knockin.repositories.contacts.edit

interface EditContactRepository {

    suspend fun updateContactPriority1To0()
    suspend fun updateContactPriority0To1()
}