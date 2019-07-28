package com.example.knocker.model

import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.example.knocker.model.ModelDB.ContactWithAllInformation

import java.util.ArrayList
import java.util.HashMap

class StatusBarParcelable : Parcelable {
    private var id: Int = 0
    var appNotifier: String? = ""
    var tickerText: String? = ""
    private var tailleList: Int = 0
    val key = ArrayList<String>()


    val statusBarNotificationInfo = HashMap<String, Any>()

    constructor(sbn: StatusBarNotification) {
        id = sbn.id
        tailleList = sbn.notification.extras.keySet().size
        appNotifier = sbn.packageName

        if (sbn.notification.tickerText != null) {
            tickerText = sbn.notification.tickerText.toString()
        } else {
            tickerText = ""
            val ticker = sbn.notification.tickerText
            Log.i(TAG, " $ticker est null")
        }
        for (keySbn in sbn.notification.extras.keySet()) {
            key.add(keySbn)
            if (sbn.notification.extras.get(keySbn) != null) {
                statusBarNotificationInfo[keySbn] = sbn.notification.extras.get(keySbn)
            } else {
                statusBarNotificationInfo[keySbn] = ""
            }
        }
        //Log.i(TAG, "ID:" + sbn.getId());
        //Log.i(TAG, "Posted by:" + sbn.getPackageName());


    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(appNotifier)
        dest.writeInt(tailleList)
        dest.writeString(tickerText)
        val entree = statusBarNotificationInfo.entries

        for ((key1, value) in entree) {

            dest.writeString(key1)
            dest.writeString(" $value")

        }
        dest.writeString(appNotifier)
        //dest.writeList(key);
        //dest.writeMap(statusBarNotificationInfo);
    }

    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        appNotifier = `in`.readString()
        tailleList = `in`.readInt()
        tickerText = `in`.readString()
        Log.i(TAG, "posted by:$appNotifier taille list $tailleList and write by $tickerText")
        for (i in 0 until tailleList) {
            val keysbn = `in`.readString()
            val value = `in`.readString()
            key.add(keysbn)
            statusBarNotificationInfo[keysbn] = value
            if (key != null) {
            }
        }
    }

    private fun getContactNameFromString(): String {
        val pregMatchString = ".*\\([0-9]*\\)"
        val NameFromNotif: String = "" + this.statusBarNotificationInfo.get("android.title")
        if (NameFromNotif.matches(pregMatchString.toRegex())) {
            return NameFromNotif.substring(0, TextUtils.lastIndexOf(NameFromNotif, '(')).dropLast(1)
        } else {
            println("pregmatch fail" + NameFromNotif)
            return NameFromNotif
        }
    }

    fun changeToContactName(contact: ContactWithAllInformation) {
        statusBarNotificationInfo.put("android.title", contact.contactDB!!.firstName + " " + contact.contactDB!!.lastName)
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

    var TAG = StatusBarParcelable::class.java.simpleName

}
