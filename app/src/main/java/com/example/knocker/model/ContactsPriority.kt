package com.example.knocker.model

import androidx.appcompat.app.AppCompatActivity
import com.example.knocker.model.ModelDB.ContactDB

object ContactsPriority : AppCompatActivity() {

    // fonction qui recupere la priorité grâce au nom du contact et la plateforme
    fun getPriorityWithName(name: String, platform: String, listContact: List<ContactDB>?): Int {
        var priority = -2
        when (platform) {
            "message" -> {
                priority = getPriority(name, listContact)
            }
            "WhatsApp" -> {
                priority = getPriority(name, listContact)
            }
            "gmail" -> {
                priority = getPriority(name, listContact)
            }
        }
        return priority
    }

    // get la priorité grace à la liste
    fun getPriority(name: String, listContact: List<ContactDB>?): Int {
        var priority = -1
        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->

//                println("contact "+dbContact+ "différent de name"+name)
                if (dbContact.firstName+" "+dbContact.lastName == name) {
                    priority = dbContact.contactPriority
                }
            }
        } else {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
                    priority = dbContact.contactPriority
                }
            }
        }
        return priority
    }

    fun checkPriority2(contactList: List<ContactDB>?): Boolean {
        if (contactList != null) {
            for (contact in contactList){
               if(contact.contactPriority==2){
                   return true
               }
            }
        }
        return false
    }

}