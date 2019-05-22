package com.example.knocker.model

import com.example.knocker.model.ModelDB.ContactWithAllInformation

import java.util.concurrent.Callable

class DBCallable : Callable<ContactWithAllInformation> {
    @Throws(Exception::class)
    override fun call(): ContactWithAllInformation? {
        val callableObj = Callable { 2 * 3 }
        return null
    }
}
