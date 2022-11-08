package com.yellowtwigs.knockin.model.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.notifications.NotificationAlarmActivity
import com.yellowtwigs.knockin.utils.NotificationsGesture

object NotificationsListenerGesture {

    fun vipNotificationWithLockscreen(
        sbp: StatusBarParcelable,
        sbn: StatusBarNotification,
        service: NotificationsListenerService,
        id: Int
    ) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            val i = Intent(
                service,
                NotificationAlarmActivity::class.java
            )
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra("notification", sbp)
            i.putExtra("ContactId", id)
            service.cancelNotification(sbn.key)
            cancelWhatsappNotification(
                sbn,
                service
            )
            service.startActivity(i)
        } else {
            val i = Intent(
                service,
                NotificationAlarmActivity::class.java
            )
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra("notification", sbp)
            i.putExtra("ContactId", id)
            service.cancelNotification(sbn.key)
            cancelWhatsappNotification(
                sbn,
                service
            )
            service.startActivity(i)
        }
    }

    // UTILS

    fun cancelWhatsappNotification(
        sbn: StatusBarNotification,
        service: NotificationsListenerService
    ) {
        service.activeNotifications.forEach {
            if (it.key.contains("whatsapp") && sbn.key.takeLast(6) == it.key.takeLast(6)) {
                service.cancelNotification(it.key)
            }
        }
    }


    fun positionXIntoScreen(
        popupX: Float,
        deplacementX: Float,
        popupSizeX: Float,
        windowManager: WindowManager
    ): Float {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return when {
            popupX + deplacementX < 0 -> {
                0.0f
            }
            popupX + deplacementX + popupSizeX < metrics.widthPixels -> {
                popupX + deplacementX
            }
            else -> {
                metrics.widthPixels.toFloat() - popupSizeX
            }
        }
    }

    fun positionYIntoScreen(
        popupY: Float,
        deplacementY: Float,
        popupSizeY: Float,
        windowManager: WindowManager
    ): Float {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return when {
            popupY + deplacementY < 0 -> {
                0.0f
            }
            popupY + deplacementY + popupSizeY < metrics.heightPixels -> {
                popupY + deplacementY
            }
            else -> {
                metrics.heightPixels.toFloat() - popupSizeY
            }
        }
    }

    fun messagesNotUseless(sbp: StatusBarParcelable, resources: Resources): Boolean {
        val pregMatchString = resources.getString(R.string.new_messages)
        return !(sbp.statusBarNotificationInfo["android.title"].toString().toLowerCase()
            .contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.text"].toString().toLowerCase()
            .contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.description"].toString().toLowerCase()
            .contains(pregMatchString.toLowerCase())
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Chat heads active")//Passer ces messages dans des strings
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Messenger")
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Bulles de discussion activées"))
    }

    fun addNotificationViewStateToList(
        list: ArrayList<PopupNotificationViewState>,
        contactDB: ContactDB,
        sbp: StatusBarParcelable,
        context: Context
    ) {

        if (contactDB.isCustomSound == 1) {
//            alertCustomNotificationTone(it.notificationTone)
        } else {
//            alertNotificationTone(it.notificationSound)
        }

        val platform = if (sbp.appNotifier?.let {
                NotificationsGesture.convertPackageToString(
                    it,
                    context
                )
            } != "") {
            sbp.appNotifier?.let { NotificationsGesture.convertPackageToString(it, context) }
                .toString()
        } else {
            ""
        }

        list.add(
            PopupNotificationViewState(
                sbp.id,
                sbp.statusBarNotificationInfo["android.title"].toString(),
                sbp.statusBarNotificationInfo["android.text"].toString(),
                platform,
                "${contactDB.firstName} ${contactDB.lastName}",
                contactDB.listOfPhoneNumbers[0],
                contactDB.messengerId,
                contactDB.listOfMails[0]
            )
        )
    }

    fun appNotifiable(sbp: StatusBarParcelable, context: Context): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "Bulles de discussion activées" &&
                NotificationsGesture.convertPackageToString(sbp.appNotifier!!, context) != ""
    }
}