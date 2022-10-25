package com.yellowtwigs.knockin.model.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
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

object NotificationsListenerGesture {


    fun vipNotificationWithLockscreen(
        sbp: StatusBarParcelable,
        sbn: StatusBarNotification,
        service: NotificationsListenerService
    ) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            val i = Intent(
                service,
                NotificationAlarmActivity::class.java
            )
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra("notification", sbp)
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
}