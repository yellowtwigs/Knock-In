package com.example.notificationlistener

import android.os.Parcel
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.util.Log

import java.util.ArrayList
import java.util.HashMap

class StatusBarParcelable : Parcelable {
    var id: Int = 0
        private set
    var appNotifier: String? = null
        private set
    var tailleList: Int = 0
        private set
    val key = ArrayList<String>()

    val statusBarNotificationInfo = HashMap<String, Any>()

    constructor(sbn: StatusBarNotification) {
        id = sbn.id
        tailleList = sbn.notification.extras.keySet().size
        appNotifier = sbn.packageName
        for (keySbn in sbn.notification.extras.keySet()) {
            key.add(keySbn)
            if(sbn.notification.extras.get(keySbn)!= null) {
                statusBarNotificationInfo[keySbn] = sbn.notification.extras.get(keySbn)
            }else{
                statusBarNotificationInfo[keySbn]= ""
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

        Log.i(TAG, "posted by:$appNotifier taille list$tailleList")
        for (i in 0 until tailleList) {
            val keysbn = `in`.readString()
            val value = `in`.readString()
            key.add(keysbn)
            statusBarNotificationInfo[keysbn] = value
            if (key != null) {
                Log.i(TAG, keysbn + "=" + this.statusBarNotificationInfo[keysbn])
            }
        }
    }

    companion object CREATOR: Parcelable.Creator<StatusBarParcelable> {
        override fun createFromParcel(source: Parcel): StatusBarParcelable {
            return StatusBarParcelable(source)
        }

        override fun newArray(size: Int): Array<StatusBarParcelable?> {
            return arrayOfNulls(size)
        }

    }
        var TAG = StatusBarParcelable::class.java.simpleName


    }
