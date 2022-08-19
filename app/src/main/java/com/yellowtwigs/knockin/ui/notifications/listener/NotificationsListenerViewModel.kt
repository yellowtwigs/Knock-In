package com.yellowtwigs.knockin.ui.notifications.listener

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.NotificationDB
import com.yellowtwigs.knockin.repositories.contacts.list.ContactsListRepository
import com.yellowtwigs.knockin.repositories.notifications.NotificationsRepository
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsListenerViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val contactsListRepository: ContactsListRepository
) : ViewModel() {

    private val viewStateLiveData = MediatorLiveData<List<NotificationsListenerViewState>>()
    private val contactLiveData = MutableLiveData<ContactNotificationsListenerViewState>()

    private val phoneNumberLiveData = MutableLiveData<String>()
    private val contactNameLiveData = MutableLiveData<String>()
    private val contactMailLiveData = MutableLiveData<String>()

    init {
        val allNotifications = notificationsRepository.getAllNotifications()
        val allContacts = contactsListRepository.getAllContacts()

        viewStateLiveData.apply {
            addSource(allNotifications) { notifications ->
                combine(
                    notifications,
                    allContacts.value,
                    phoneNumberLiveData.value,
                    contactNameLiveData.value
                )
            }
            addSource(allContacts) { contacts ->
                combine(
                    allNotifications.value,
                    contacts,
                    phoneNumberLiveData.value,
                    contactNameLiveData.value
                )
            }
            addSource(phoneNumberLiveData) { phoneNumber ->
                combine(
                    allNotifications.value,
                    allContacts.value,
                    phoneNumber,
                    contactNameLiveData.value
                )
            }
            addSource(contactNameLiveData) { contactName ->
                combine(
                    allNotifications.value,
                    allContacts.value,
                    phoneNumberLiveData.value,
                    contactName
                )
            }
        }
    }

    private fun combine(
        allNotifications: List<NotificationDB>?, allContacts: List<ContactDB>?,
        phoneNumber: String?, contactName: String?
    ) {

        allContacts?.let {
            if (phoneNumber != null && phoneNumber.isNotEmpty() && phoneNumber.isNotBlank()) {
                getContactByPhoneNumber(it, phoneNumber)
            }
            if (contactName != null && contactName.isNotEmpty() && contactName.isNotBlank()) {
                getContactFromName(it, contactName)
            }
        }
    }

    private fun getContactByPhoneNumber(contacts: List<ContactDB>, phoneNumber: String) {
        for (contact in contacts) {
            if (PhoneNumberUtils.compare(contact.listOfPhoneNumbers[0], phoneNumber)) {
                setContactToContactLiveData(contact)
            }
        }
    }

    private fun getContactFromName(contacts: List<ContactDB>, name: String) {
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
                    setContactToContactLiveData(contact)

                } else if (contact.mail_name == name) {

                    setContactToContactLiveData(contact)

                } else if (lastname == contact.lastName && firstname == contact.firstName) {
                    setContactToContactLiveData(contact)
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
                                firstname == firstLetter.toLowerCase() + lastLetter.toLowerCase() ||
                                firstname == firstLetter + lastLetter.toLowerCase()
                            ) {
                                setContactToContactLiveData(contact)
                            }

                        } else {
                            // Ken Suon = Kenzy Suon
                            if (contact.firstName.contains(firstname)) {
                                setContactToContactLiveData(contact)
                            }
                        }

                        // Jfc = Jean-Francois Coudeyre

                    } else {
                        var entireName = name.replace(name[0], ' ')
                        entireName = entireName.replace(name[entireName.length - 1], ' ')

                        if (' ' + contact.firstName + " " + contact.lastName + ' ' == entireName || ' ' + contact.firstName + ' ' == entireName || ' ' + contact.lastName + ' ' == entireName) {
                            setContactToContactLiveData(contact)
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
                    setContactToContactLiveData(contact)
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
                    setContactToContactLiveData(contact)
                }
            }
        }
    }

    private fun setContactToContactLiveData(contact: ContactDB) {
        contact.apply {
            contactLiveData.value = ContactNotificationsListenerViewState(
                id,
                firstName,
                lastName,
                profilePicture,
                profilePicture64,
                listOfPhoneNumbers,
                listOfMails,
                mail_name,
                priority,
                isFavorite == 1,
                messengerId,
                listOfMessagingApps.contains("whatsapp"),
                listOfMessagingApps.contains("telegram"),
                listOfMessagingApps.contains("signal"),
                notificationTone,
                notificationSound,
                isCustomSound,
                vipSchedule,
                hourLimitForNotification,
                audioFileName
            )
        }
    }

    fun setPhoneNumberLiveData(phoneNumber: String) {
        phoneNumberLiveData.value = phoneNumber
    }

    fun setContactNameLiveData(name: String) {
        phoneNumberLiveData.value = name
    }

    fun setContactMailLiveData(mail: String) {
        contactMailLiveData.value = mail
    }

    fun getContactLiveData(): LiveData<ContactNotificationsListenerViewState> {
        return contactLiveData
    }


//    fun saveNotification(sbp: StatusBarParcelable, contactId: Int): NotificationDB? {
//        return if (sbp.statusBarNotificationInfo["android.title"] != null && sbp.statusBarNotificationInfo["android.text"] != null) {
//            NotificationDB(
//                0,
//                sbp.tickerText.toString(),
//                sbp.statusBarNotificationInfo["android.title"]!!.toString(),
//                sbp.statusBarNotificationInfo["android.text"]!!.toString(),
//                sbp.appNotifier!!, false,
//                System.currentTimeMillis(), 0,
//                contactId
//            )
//        } else {
//            null
//        }
//    }
}