package com.example.firsttestknocker

import android.support.v7.app.AppCompatActivity

object ContactsPriority : AppCompatActivity() {

    // fonction qui recupere la priorité grâce au nom du contact et la plateforme
    fun getPriorityWithName(name: String, platform: String, listContact: List<Contacts>?): Int {
        var priority = 1
        when (platform) {
            "sms" -> {
                // jean, jean michel, jean michel pelletier
                priority = getPriority(name,listContact)
            }
        }
        return priority
    }

    // get la priorité grace à la liste
    fun getPriority(name: String, listContact: List<Contacts>?): Int {
        var priority = -1
        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName+" "+dbContact.lastName == name) { //contain or == |jean michel pellier && michel pellier !=
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
}