package com.yellowtwigs.knockin.domain.notifications

import android.util.Log
import com.yellowtwigs.knockin.model.database.dao.ContactsDao
import com.yellowtwigs.knockin.model.database.data.ContactDB
import javax.inject.Inject

class GetContactByName @Inject constructor(private val contactsDao: ContactsDao) {

    operator fun invoke(name: String): ContactDB? {
        val contacts = contactsDao.getAllContactsForNotificationsListener()

        Log.i("GoToWithContact", "contacts : $contacts")

        var contactDB: ContactDB? = null

        if (name.contains(" ")) {
            val array = name.toCharArray().toList()
            val array2 = arrayListOf<Char>()
            var canSpace = false
            var oneTime = true
            array.forEach { char ->
                if (char.isLetter() || char == '-' || canSpace) {
                    array2.add(char)
                    canSpace = oneTime
                    if (char.isWhitespace()) {
                        canSpace = false
                        oneTime = false
                    }
                }
            }

            val name1 = String(array2.toCharArray())
            contacts.forEach { contact ->
                val firstnameList: ArrayList<Char> = arrayListOf()
                var firstname = ""
                val lastnameList: ArrayList<Char> = arrayListOf()
                var lastname = ""

                var cpt = 0

                while (name[cpt] != ' ') {
                    firstnameList.add(name[cpt]) // Kj, " " = 2
                    cpt++
                }
                cpt++

                for (i in 0 until firstnameList.size) {
                    firstname += firstnameList[i]
                }

                while (cpt < name.length) {
                    lastnameList.add(name[cpt]) // Kent, " " = 2
                    cpt++
                }

                for (i in 0 until lastnameList.size) {
                    lastname += lastnameList[i]
                }

                if (contact.firstName + " " + contact.lastName == name ||
                    contact.firstName == name || contact.lastName == name ||
                    " " + contact.firstName + " " + contact.lastName == name ||
                    name1 == contact.firstName + " " + contact.lastName ||
                    contact.firstName == name1 || contact.lastName == name1
                ) {
                    contactDB = contact
                } else if (contact.mail_name == name) {
                    contactDB = contact

                } else if (lastname == contact.lastName && firstname == contact.firstName) {
                    contactDB = contact
                } else {
                    if (lastname == contact.lastName) {
//                        if (name.contains("_") || name.contains(".") || name.contains("-") || name.contains("_")) {
                        if (contact.firstName.contains("-")) {
                            // Kj Kent = Kenny-Jay Kent

                            var cpt2 = 0
                            val firstLetterList: ArrayList<Char> = arrayListOf()
                            val lastLetterList: ArrayList<Char> = arrayListOf()
                            var firstLetter = ""
                            var lastLetter = ""

                            while (contact.firstName[cpt2] != '-') {
                                firstLetterList.add(contact.firstName[cpt2]) // K de Kenny
                                firstLetter = firstLetterList[0].toString()
                                cpt2++
                            }
                            cpt2++

                            while (cpt2 < name.length) {
                                lastLetterList.add(contact.firstName[cpt2]) // J de Jay
                                lastLetter = lastLetterList[0].toString()
                                cpt2++
                            }

                            if (firstname == firstLetter + lastLetter ||
                                firstname == firstLetter.lowercase() + lastLetter.lowercase() ||
                                firstname == firstLetter + lastLetter.lowercase()
                            ) {
                                contactDB = contact
                            }

                        } else {
                            // Ken Suon = Kenzy Suon
                            if (contact.firstName.contains(firstname)) {
                                contactDB = contact
                            }
                        }

                        // Jfc = Jean-Francois Coudeyre

                    } else {
                        var entireName = name.replace(name[0], ' ')
                        entireName = entireName.replace(name[entireName.length - 1], ' ')

                        if (' ' + contact.firstName + " " + contact.lastName + ' ' == entireName || ' ' + contact.firstName + ' ' == entireName || ' ' + contact.lastName + ' ' == entireName) {
                            contactDB = contact
                        }
                    }
                }
            }
        } else {
            val array = name.toCharArray().toList()
            val array2 = arrayListOf<Char>()
            array.forEach { char ->
                if (char.isLetter() || char == '-') {
                    array2.add(char)
                }
            }
            val name1 = String(array2.toCharArray())
            contacts.forEach { contact ->
                if (contact.firstName == name && contact.lastName == "" || contact.firstName == "" && contact.lastName == name ||
                    contact.firstName == name1 && contact.lastName == ""
                ) {
                    contactDB = contact
                }
            }
        }
        contacts.forEach { contact ->
            if (name.isNotEmpty()) {
                var entireName = name.replace(name[0], ' ')
                entireName = entireName.replace(name[entireName.length - 1], ' ')

                if (' ' + contact.firstName + " " + contact.lastName + ' ' == entireName || ' ' +
                    contact.firstName + ' ' == entireName || ' ' + contact.lastName + ' ' == entireName
                ) {
                    contactDB = contact
                }
            }
        }

        return contactDB
    }
}