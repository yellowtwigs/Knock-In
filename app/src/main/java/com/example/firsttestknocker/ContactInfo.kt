package com.example.firsttestknocker

import android.support.v7.app.AppCompatActivity

object ContactInfo : AppCompatActivity() {
    fun getInfoWithName(name: String, platform: String, listContact: List<Contacts>?): String {
        var info = "Platform Error"
        when (platform) {
            "message" -> {
                info = getPhoneNumberWithName(name, listContact)
            }
        }
        return info
    }

    fun getPhoneNumberWithName(name: String, listContact: List<Contacts>?): String {
        var info = "Name Error"
        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName+" "+dbContact.lastName == name) {
                    info = dbContact.phoneNumber.dropLast(1)
                }
            }
        } else {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
                    info = dbContact.phoneNumber.dropLast(1)
                }
            }
        }
        return info
    }
}