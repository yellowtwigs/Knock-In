package com.example.firsttestknocker

import android.os.Build
import android.provider.Settings
import android.support.v7.app.AppCompatActivity

object ContactsPriority : AppCompatActivity() {

    // fonction qui recupere la priorité grâce au nom du contact et la plateforme
    fun getPriorityWithName(name: String, platform: String, listContact: List<Contacts>?): Int {
        var priority = -2
        when (platform) {
            "message" -> {
                priority = getPriority(name,listContact)
            }
            "whatsapp" -> {
                priority = getPriority(name,listContact)
            }
            "gmail" -> {
                priority = getPriorityGmail(name, listContact)
            }
        }
        return priority
    }

    fun getPriorityGmail(mail: String, listContact: List<Contacts>?): Int {
        var priority = -1
        listContact!!.forEach {dbContact ->
            if (dbContact.mail == mail)
                priority = dbContact.contactPriority
        }
        return priority
    }

    // get la priorité grace à la liste
    fun getPriority(name: String, listContact: List<Contacts>?): Int {
        var priority = -1
        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->

                println("contact "+dbContact+ "différent de name"+name)
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

    fun checkPriority2(contactList: List<Contacts>?): Boolean {
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