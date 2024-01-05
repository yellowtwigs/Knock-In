package com.yellowtwigs.knockin.model.database

import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.yellowtwigs.knockin.model.database.data.ContactDB
import java.text.SimpleDateFormat

import java.util.ArrayList
import java.util.Date
import java.util.HashMap

/**
 * Classe qui correspond a StatusBarNotification(Qui nous permet d'avoir les attributs de la notification reçu) avec seulement les attributs utile pour notre application
 * Et celle-ci nous permet de passer l'objet entre les activités
 * @author Florian Striebel
 */
class StatusBarParcelable : Parcelable {
    var id: Int = 0
    var appNotifier: String? = ""
    var tickerText: String? = ""
    var dateTime: String = ""
    var tailleList: Int = 0
    var contactId: Int = 0
    var key = ArrayList<String>()

    var statusBarNotificationInfo = HashMap<String, Any?>()

    constructor(sbn: StatusBarNotification, contactId: Int) {
        id = sbn.id
        this.contactId = contactId
        tailleList = sbn.notification.extras.keySet().size
        appNotifier = sbn.packageName
        dateTime = SimpleDateFormat("HH:mm").format(Date(sbn.postTime))

        if (sbn.notification.tickerText != null) {
            tickerText = sbn.notification.tickerText.toString()
        } else {
            tickerText = ""
            val ticker = sbn.notification.tickerText
        }
        for (keySbn in sbn.notification.extras.keySet()) {
            key.add(keySbn)
            if (sbn.notification.extras.get(keySbn) != null) {
                statusBarNotificationInfo[keySbn] = sbn.notification.extras.get(keySbn)
            } else {
                statusBarNotificationInfo[keySbn] = ""
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Prend un objet StatusBarParcelable est met à l'interieur de l'objet parcel tous les attributs de l'objet
     * Nous permet de passer l'objet entre les activités
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(contactId)
        dest.writeString(appNotifier)
        dest.writeString(appNotifier)
        dest.writeInt(tailleList)
        dest.writeString(dateTime)
        val entree = statusBarNotificationInfo.entries

        for ((key1, value) in entree) {

            dest.writeString(key1)
            dest.writeString(" $value")

        }
    }

    /**
     * Prend l'objet Parcel pour affecter les bonnes valeurs au StatusBarParcelable
     */
    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        contactId = `in`.readInt()
        appNotifier = `in`.readString()
        tailleList = `in`.readInt()
        tickerText = `in`.readString()
        dateTime = `in`.readString().toString()
        for (i in 0 until tailleList) {
            val keysbn = `in`.readString()
            val value = `in`.readString()
            key.add(keysbn.toString())
            statusBarNotificationInfo[keysbn.toString()] = value
            if (key != null) {
            }
        }
    }

    /**
     * Nous récupérons un nom de contact sans indication superflu
     */
    private fun getContactNameFromString(): String {
        val pregMatchString = ".*\\([0-9]*\\)"
        val NameFromNotif: String = "" + this.statusBarNotificationInfo.get("android.title")
        if (NameFromNotif.matches(pregMatchString.toRegex())) {
            return NameFromNotif.substring(0, TextUtils.lastIndexOf(NameFromNotif, '(')).dropLast(1)
        } else {
            return NameFromNotif
        }
    }

    /**
     * Change le numéro de téléphone par le nom et prénom du contact
     */
    fun changeToContactName(contact: ContactDB) {
        statusBarNotificationInfo["android.title"] = contact.firstName + " " + contact.lastName
    }

    fun castName() {
        statusBarNotificationInfo.put("android.title", getContactNameFromString())
    }

    companion object CREATOR : Parcelable.Creator<StatusBarParcelable> {
        override fun createFromParcel(source: Parcel): StatusBarParcelable {
            return StatusBarParcelable(source)
        }

        override fun newArray(size: Int): Array<StatusBarParcelable?> {
            return arrayOfNulls(size)
        }
    }
}