package com.yellowtwigs.knockin.model.service

import android.content.*
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.notifications.NotificationsListenerUseCases
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.utils.ContactGesture.isPhoneNumber
import com.yellowtwigs.knockin.utils.ContactGesture.isValidEmail
import com.yellowtwigs.knockin.utils.Converter.convertTimeToEndTime
import com.yellowtwigs.knockin.utils.Converter.convertTimeToHour
import com.yellowtwigs.knockin.utils.Converter.convertTimeToMinutes
import com.yellowtwigs.knockin.utils.Converter.convertTimeToStartTime
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class NotificationsListenerService : NotificationListenerService() {

    private var oldPosX: Float = 0.0f
    private var oldPosY: Float = 0.0f
    private var popupView: View? = null
    private var windowManager: WindowManager? = null

    private var notificationPopupRecyclerView: RecyclerView? = null

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var durationPreferences: SharedPreferences

    @Inject
    lateinit var notificationsListenerUseCases: NotificationsListenerUseCases

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        requestRebind(ComponentName(this, NotificationListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        durationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)

        val sbp = StatusBarParcelable(sbn)

        if (sharedPreferences.getBoolean("serviceNotif", true)) {
            sbp.castName()
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()
//            val app = convertPackageToString(sbp.appNotifier!!, this)

            Log.i("getNotifications", "sbp.appNotifier : ${sbp.appNotifier?.toString()}")

            if (sbp.appNotifier?.let { convertPackageToString(it, this) } != "") {
                if (message.contains("call") || message.contains("Incoming") || message.contains("Incoming Call") ||
                    message.contains("Appel entrant") || message.contains("appel entrant")
                ) {
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationsListenerUseCases.apply {
                            val contact = when {
                                isPhoneNumber(name) -> {
                                    getContactByPhoneNumber.invoke(name)
                                }
                                isValidEmail(name) -> {
                                    getContactByMail.invoke(name)
                                }
                                else -> {
                                    getContactByName.invoke(name)
                                }
                            }

                            val notification = if (contact != null) {
                                NotificationDB(
                                    0,
                                    sbp.tickerText.toString(),
                                    sbp.statusBarNotificationInfo["android.title"].toString(),
                                    sbp.statusBarNotificationInfo["android.text"].toString(),
                                    sbp.appNotifier!!,
                                    System.currentTimeMillis(),
                                    0,
                                    contact.id,
                                    contact.priority
                                )
                            } else {
                                NotificationDB(
                                    0,
                                    sbp.tickerText.toString(),
                                    sbp.statusBarNotificationInfo["android.title"].toString(),
                                    sbp.statusBarNotificationInfo["android.text"].toString(),
                                    sbp.appNotifier!!,
                                    System.currentTimeMillis(),
                                    0,
                                    0,
                                    0
                                )
                            }

//                                    && notificationNotDouble(notification)
                            if (sbp.appNotifier != "com.samsung.android.incallui") {
                                contact?.let {
                                    saveNotification.invoke(notification)
                                }
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                                    if (contact != null) {
                                        displayNotificationWithContact(sbp, sbn, contact)
                                    } else {
//                                        displayNotificationWithoutContact(sbp, sbn)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    notificationsListenerUseCases.apply {
                        saveNotification.invoke(
                            NotificationDB(
                                0,
                                sbp.tickerText.toString(),
                                sbp.statusBarNotificationInfo["android.title"].toString(),
                                sbp.statusBarNotificationInfo["android.text"].toString(),
                                sbp.appNotifier!!,
                                System.currentTimeMillis(),
                                0,
                                0,
                                0
                            )
                        )
                    }
                }
            }
        }
    }

    private fun displayNotificationWithContact(
        sbp: StatusBarParcelable,
        sbn: StatusBarNotification,
        contact: ContactDB
    ) {
        contact.apply {
            when (priority) {
                2 -> {
                    val date = DateFormat.getTimeInstance().calendar.time
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val hours = cal.get(Calendar.HOUR_OF_DAY)
                    val minutes = cal.get(Calendar.MINUTE)
                    val today = cal.get(Calendar.DAY_OF_WEEK)

                    val vipScheduleValueSharedPreferences =
                        getSharedPreferences(
                            "VipScheduleValue",
                            Context.MODE_PRIVATE
                        )

                    val startTime = convertTimeToStartTime(hourLimitForNotification)
                    val hourStart = convertTimeToHour(startTime)
                    val minutesStart = convertTimeToMinutes(startTime)
                    val endTime = convertTimeToEndTime(hourLimitForNotification)
                    val hourEnd = convertTimeToHour(endTime)
                    val minutesEnd = convertTimeToMinutes(endTime)

//                    when (vipSchedule) {
//                        1 -> vipNotificationsDeployment(sbp, sbn, contact)
//                        2 -> {
//                            if (today in 1..4 || today == 7) {
//                                if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
//                                    if (hourStart.toInt() == hours) {
//                                        if (minutes >= minutesStart.toInt()) {
//                                            if (hourEnd.toInt() == hours) {
//                                                if (minutes <= minutesEnd.toInt()) {
//                                                    vipNotificationsDeployment(
//                                                        sbp,
//                                                        sbn,
//                                                        contact
//                                                    )
//                                                }
//                                            } else {
//                                                vipNotificationsDeployment(
//                                                    sbp,
//                                                    sbn,
//                                                    contact
//                                                )
//                                            }
//                                        }
//                                    } else if (hourEnd.toInt() == hours) {
//                                        if (minutes <= minutesEnd.toInt()) {
//                                            vipNotificationsDeployment(
//                                                sbp,
//                                                sbn,
//                                                contact
//                                            )
//                                        }
//                                    } else {
//                                        vipNotificationsDeployment(
//                                            sbp,
//                                            sbn,
//                                            contact
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                        3 -> {
//                            if (today == 5 || today == 6) {
//                                vipNotificationsDeployment(sbp, sbn, contact)
//                            }
//                        }
//                        4 -> {
//                            if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
//                                if (hourStart.toInt() == hours) {
//                                    if (minutes >= minutesStart.toInt()) {
//                                        if (hourEnd.toInt() == hours) {
//                                            if (minutes <= minutesEnd.toInt()) {
//                                                vipNotificationsDeployment(sbp, sbn, contact)
//                                            }
//                                        } else {
//                                            vipNotificationsDeployment(sbp, sbn, contact)
//                                        }
//                                    }
//                                } else if (hourEnd.toInt() == hours) {
//                                    if (minutes <= minutesEnd.toInt()) {
//                                        vipNotificationsDeployment(sbp, sbn, contact)
//                                    }
//                                } else {
//                                    vipNotificationsDeployment(sbp, sbn, contact)
//                                }
//                            }
//                        }
//                        else -> {
//                            if (vipScheduleValueSharedPreferences.getBoolean(
//                                    "VipScheduleValue",
//                                    false
//                                )
//                            ) {
//                                if (today == 5 || today == 6) {
//                                    val notificationsHour =
//                                        getSharedPreferences(
//                                            "TeleworkingReminder",
//                                            Context.MODE_PRIVATE
//                                        ).getString(
//                                            "TeleworkingReminder",
//                                            ""
//                                        )
//
//                                    if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
//                                        if (hourStart.toInt() == hours) {
//                                            if (minutes >= minutesStart.toInt()) {
//                                                if (hourEnd.toInt() == hours) {
//                                                    if (minutes <= minutesEnd.toInt()) {
//                                                        vipNotificationsDeployment(sbp, sbn, contact)
//                                                    }
//                                                } else {
//                                                    vipNotificationsDeployment(sbp, sbn, contact)
//                                                }
//                                            }
//                                        } else if (hourEnd.toInt() == hours) {
//                                            if (minutes <= minutesEnd.toInt()) {
//                                                vipNotificationsDeployment(sbp, sbn, contact)
//                                            }
//                                        } else {
//                                            vipNotificationsDeployment(sbp, sbn, contact)
//                                        }
//                                    }
//                                }
//                            } else {
//                                vipNotificationsDeployment(sbp, sbn, contact)
//                            }
//                        }
//                    }
                }
                1 -> {
                }
                0 -> {
                    this@NotificationsListenerService.cancelAllNotifications()
                    cancelWhatsappNotification(sbn)
                }
            }
        }
    }

//    private fun displayNotificationWithoutContact(
//        sbp: StatusBarParcelable,
//        sbn: StatusBarNotification
//    ) {
//        if (sbn.key.contains("whatsapp")) {
//            cancelWhatsappNotification(sbn)
//        } else {
//            if (sbn.packageName == OUTLOOK_PACKAGE || sbn.packageName == GMAIL_PACKAGE) {
//
//            } else {
//                if (sbn.packageName == MESSAGE_PACKAGE || sbn.packageName == MESSAGE_SAMSUNG_PACKAGE || sbn.packageName == XIAOMI_MESSAGE_PACKAGE || sbn.packageName == WHATSAPP_SERVICE) {
//                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//                        var i = 0
//                        val vipNotif = VipNotificationsDB(
//                            null,
//                            sbp.id,
//                            sbp.appNotifier!!,
//                            sbp.tailleList,
//                            sbp.tickerText!!
//                        )
//                        val notifId =
//                            database!!.VipNotificationsDao()
//                                .insert(vipNotif)
//                        while (i < sbp.key.size) {
//                            if (sbp.key[i] == "android.title" || sbp.key[i] == "android.text" || sbp.key[i] == "android.largeIcon") {
//                                val VipSbn = VipSbnDB(
//                                    null,
//                                    notifId!!.toInt(),
//                                    sbp.key[i],
//                                    sbp.statusBarNotificationInfo[sbp.key[i]].toString()
//                                )
//                                database!!.VipSbnDao()
//                                    .insert(VipSbn)
//                            }
//                            i++
//                        }
//                    }
//                    val screenListener: KeyguardManager =
//                        this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//                    if (screenListener.isKeyguardLocked) {
//                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//                            this.cancelNotification(sbn.key)
//                            cancelWhatsappNotif(sbn)
//                            displayLayoutWithSharedPreferences(
//                                sbp,
//                                null
//                            )
//                        } else {
//                            val i = Intent(
//                                this@NotificationsListener,
//                                NotificationAlarmActivity::class.java
//                            )
//                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            i.putExtra("notification", sbp)
//                            this.cancelNotification(sbn.key)
//                            cancelWhatsappNotif(sbn)
//                            startActivity(i)
//                        }
//                    } else {
//                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//                            this.cancelNotification(sbn.key)
//                            cancelWhatsappNotif(sbn)
//                        }
//                        displayLayoutWithSharedPreferences(
//                            sbp,
//                            null
//                        )
//                    }
//                }
//            }
//        }
//
//    }

    private fun cancelWhatsappNotification(sbn: StatusBarNotification) {
        this.activeNotifications.forEach {
            if (it.key.contains("whatsapp") && sbn.key.takeLast(6) == it.key.takeLast(6)) {
                this.cancelNotification(it.key)
            }
        }
    }

//    private fun notificationNotDouble(notification: NotificationDB): Boolean {
//        val lastInsertId = database!!.notificationsDao().lastInsert()
//        val lastInsert = database!!.notificationsDao()
//            .getNotification(lastInsertId)
//        val listLastInsert = database!!.notificationsDao()
//            .lastInsertByTime(System.currentTimeMillis())
//        for (lastNotif in listLastInsert) {
//            if (lastNotif != null && lastNotif.platform == notification.platform && lastNotif.description == notification.description) {
//                return false
//            }
//        }
//        if (lastInsert != null && lastInsert.platform == notification.platform && lastInsert.title == notification.title && lastInsert.description == notification.description && notification.timestamp - lastInsert.timestamp < 1000) {
//            return false
//        }
//        return true
//    }

    /**
     * Nous permet de reconnaitre un message inutile
     *  @param sbp StatusBarParcelable       La notification reçu
     *  @return Boolean
     */
    private fun messagesNotUseless(sbp: StatusBarParcelable): Boolean {
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

//    private fun vipNotificationsDeployment(
//        sbp: StatusBarParcelable, sbn: StatusBarNotification,
//        contact: ContactDB
//    ) {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//            var i = 0
//            val vipNotif = VipNotificationsDB(
//                null,
//                sbp.id,
//                sbp.appNotifier!!,
//                sbp.tailleList,
//                sbp.tickerText!!
//            )
//            val notifId =
//                database?.VipNotificationsDao()?.insert(vipNotif)
//            while (i < sbp.key.size) {
//                if (sbp.key[i] == "android.title" || sbp.key[i] == "android.text" || sbp.key[i] == "android.largeIcon") {
//                    val vipSbn = VipSbnDB(
//                        null,
//                        notifId!!.toInt(),
//                        sbp.key[i],
//                        sbp.statusBarNotificationInfo[sbp.key[i]].toString()
//                    )
//                    database!!.VipSbnDao()
//                        .insert(vipSbn)
//                }
//                i++
//            }
//        }
//        val screenListener =
//            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        if (screenListener.isKeyguardLocked) {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
//                val i = Intent(
//                    this@NotificationListener,
//                    NotificationAlarmActivity::class.java
//                )
//                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                i.putExtra("notification", sbp)
//                this.cancelNotification(sbn.key)
//                cancelWhatsappNotif(sbn)
//                startActivity(i)
//            } else {
//                val i = Intent(
//                    this@NotificationListener,
//                    NotificationAlarmActivity::class.java
//                )
//                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                i.putExtra("notification", sbp)
//                this.cancelNotification(sbn.key)
//                cancelWhatsappNotif(sbn)
//                startActivity(i)
//            }
//        } else {
//            this.cancelNotification(sbn.key)
//            cancelWhatsappNotif(sbn)
////            displayLayoutWithSharedPreferences(
////                sbp,
////                contact.contactDB!!
////            )
//        }
//    }

    private fun displayLayoutWithSharedPreferences(
        sbp: StatusBarParcelable,
        contactDB: ContactDB?
    ) {
        if (contactDB != null) {
            if (contactDB.isCustomSound == 1) {
                alertCustomNotificationTone(contactDB.notificationTone)
            } else {
                alertNotificationTone(contactDB.notificationSound)
            }
        }

        if (appNotifiable(sbp) && sharedPreferences.getBoolean("Overlay_Preferences", false)) {
//            if (adapterNotification == null) {
//                val edit = sharedPreferences.edit()
//                edit.putBoolean("view", false)
//                edit.apply()
//            }
            if (!sharedPreferences.getBoolean("view", false)) {
                popupView = null
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                displayLayout(sbp, contactDB)
            } else {
//                adapterNotification?.addNotification(sbp)
            }
        }
    }

    private fun displayLayout(
        sbp: StatusBarParcelable,
        contactDB: ContactDB?
    ) {
        val flag = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        val parameters = WindowManager.LayoutParams(
            flag,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        parameters.gravity = Gravity.RIGHT or Gravity.TOP
        parameters.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        popupView = inflater.inflate(R.layout.layout_notification_pop_up, null)
        val popupDropable = popupView?.findViewById<ConstraintLayout>(R.id.notification_dropable)
        val popupContainer =
            popupView?.findViewById<LinearLayout>(R.id.notification_popup_main_layout)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            notifLayout(sbp, popupView, contactDB)
            windowManager?.addView(popupView, parameters) // affichage de la popupview
            popupDropable?.setOnTouchListener { view, event ->
                val metrics = DisplayMetrics()
                windowManager?.defaultDisplay?.getMetrics(metrics)
                when (event.action and MotionEvent.ACTION_MASK) {

                    MotionEvent.ACTION_DOWN -> {
                        oldPosX = event.x
                        oldPosY = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = event.x
                        val y = event.y

                        val deplacementX = x - oldPosX
                        val deplacementY = y - oldPosY

                        popupContainer?.x = positionXIntoScreen(
                            popupContainer?.x!!,
                            deplacementX,
                            popupContainer?.width?.toFloat()
                        )
                        oldPosX = x - deplacementX

                        popupContainer?.y = positionYIntoScreen(
                            popupContainer?.y,
                            deplacementY,
                            popupContainer?.height?.toFloat()
                        )
                        oldPosY = y - deplacementY
                    }
                }
                return@setOnTouchListener true
            }
        } else {
            if (sharedPreferences.getBoolean("first_notif", true)) {
                val view = inflater.inflate(R.layout.layout_notification_pop_up, null)
                val notifications: ArrayList<StatusBarParcelable> = ArrayList()
                notifications.add(sbp)
//                adapterNotification = NotifPopupRecyclerViewAdapter(
//                    applicationContext,
//                    notifications,
//                    windowManager!!,
//                    view
//                )
                val edit = sharedPreferences.edit()
                edit.putBoolean("first_notif", false)
                edit.apply()
            }
        }
    }

    private fun positionXIntoScreen(popupX: Float, deplacementX: Float, popupSizeX: Float): Float {
        val metrics = DisplayMetrics()
        windowManager!!.defaultDisplay.getMetrics(metrics)
        return if (popupX + deplacementX < 0) {
            0.0f
        } else if (popupX + deplacementX + popupSizeX < metrics.widthPixels) {
            popupX + deplacementX
        } else {
            metrics.widthPixels.toFloat() - popupSizeX
        }
    }

    private fun positionYIntoScreen(popupY: Float, deplacementY: Float, popupSizeY: Float): Float {
        val metrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(metrics)
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

    private fun notifLayout(
        sbp: StatusBarParcelable, view: View?,
        contactDB: ContactDB?
    ) {
        val notifications: ArrayList<StatusBarParcelable> = ArrayList()
        notifications.add(sbp)
        notificationPopupRecyclerView = view?.findViewById(R.id.notification_popup_recycler_view)
        notificationPopupRecyclerView?.layoutManager = LinearLayoutManager(applicationContext)
//        adapterNotification = NotifPopupRecyclerViewAdapter(
//            applicationContext,
//            notifications,
//            windowManager!!,
//            view!!
//        )
//        notificationPopupRecyclerView?.adapter = adapterNotification

//        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapterNotification))
//        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)

        if (contactDB != null) {
            if (contactDB.isCustomSound == 1) {
                alertCustomNotificationTone(contactDB.notificationTone)
            } else {
                alertNotificationTone(contactDB.notificationSound)
            }
        }

        if (notifications.size == 0) {
            alarmSound?.stop()
        }

//        if (adapterNotification?.isClose == true) {
//            alarmSound?.stop()
//        }

        val imgClose = view?.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.VISIBLE
        imgClose.setOnClickListener {
            windowManager?.removeView(view)
            popupView = null
            alarmSound?.stop()

            val edit = sharedPreferences.edit()
            edit.putBoolean("view", false)
            edit.apply()
        }
    }

    private fun appNotifiable(sbp: StatusBarParcelable): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "Bulles de discussion activées" &&
                convertPackageToString(sbp.appNotifier!!, this) != ""
    }

    private fun alertNotificationTone(sound: Int) {
        alarmSound?.stop()
        alarmSound = if (sound == -1) {
            MediaPlayer.create(this, R.raw.sms_ring)
        } else {
            MediaPlayer.create(this, sound)
        }
        alarmSound?.start()

        val editDuration = durationPreferences.edit()
        alarmSound?.duration?.let { editDuration.putInt("Alarm_Notif_Duration", it) }
        editDuration.apply()
    }

    private fun alertCustomNotificationTone(customSound: String) {
        alarmSound?.stop()
        alarmSound = MediaPlayer.create(applicationContext, Uri.parse(customSound))
        alarmSound?.start()

        val editDuration = durationPreferences.edit()
        alarmSound?.duration?.let { editDuration.putInt("Alarm_Notif_Duration", it) }
        editDuration.apply()
    }

    companion object {
        var TAG = NotificationsListenerService::class.java.simpleName
        var alarmSound: MediaPlayer? = null
    }
}