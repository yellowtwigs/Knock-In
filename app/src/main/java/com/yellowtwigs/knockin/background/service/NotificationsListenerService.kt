package com.yellowtwigs.knockin.background.service

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.Spannable
import android.text.SpannableString
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.domain.notifications.NotificationsListenerUseCases
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.addNotificationViewStateToList
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.appNotifiable
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.cancelWhatsappNotification
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.messagesNotUseless
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.positionXIntoScreen
import com.yellowtwigs.knockin.background.service.NotificationsListenerGesture.positionYIntoScreen
import com.yellowtwigs.knockin.ui.notifications.alarm.NotificationAlarmActivity
import com.yellowtwigs.knockin.ui.notifications.alarm.NotificationAlarmViewState
import com.yellowtwigs.knockin.utils.ContactGesture.isPhoneNumber
import com.yellowtwigs.knockin.utils.ContactGesture.isValidEmail
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToPhoneNumbersWithSpinner
import com.yellowtwigs.knockin.utils.Converter.convertTimeToEndTime
import com.yellowtwigs.knockin.utils.Converter.convertTimeToHour
import com.yellowtwigs.knockin.utils.Converter.convertTimeToMinutes
import com.yellowtwigs.knockin.utils.Converter.convertTimeToStartTime
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString
import com.yellowtwigs.knockin.utils.NotificationsGesture.isFromAPhoneCallApp
import com.yellowtwigs.knockin.utils.NotificationsGesture.isMessagingApp
import com.yellowtwigs.knockin.utils.NotificationsGesture.isPhoneCall
import com.yellowtwigs.knockin.utils.NotificationsGesture.isSocialMedia
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NotificationsListenerService : NotificationListenerService() {

    private var oldPosX: Float = 0.0f
    private var oldPosY: Float = 0.0f

    private var windowManager: WindowManager? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var durationPreferences: SharedPreferences

    private var popupView: View? = null

    @Inject
    lateinit var notificationsListenerUseCases: NotificationsListenerUseCases

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        requestRebind(ComponentName(this, NotificationListenerService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (popupView != null) {
            windowManager?.removeView(popupView)
            popupView = null

            val edit = sharedPreferences.edit()
            edit.putBoolean("first_notif", true)
            edit.apply()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        context = applicationContext
        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        durationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)

        var sbp = StatusBarParcelable(sbn, 0)
        if (sharedPreferences.getBoolean("serviceNotif", true) && messagesNotUseless(sbp, resources)) {
            sbp.castName()
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()

            Log.i("AlarmMessages", "name : $name")
            Log.i("AlarmMessages", "message : $message")
            Log.i("AlarmMessages", "sbp.dateTime : ${sbp.dateTime}")

            if (name != "" && message != "" && name != "null" && message != "null") {
                if (sbp.appNotifier?.let { convertPackageToString(it, this) } != "") {
                    if (message.contains("call") || message.contains("Incoming") || message.contains("Incoming Call")
                        || message.contains("Appel entrant") || message.contains("Appel en cours") || message.contains("Appel...") || message
                            .contains("Dialing") || message.contains
                            ("appel entrant")
                        || message.contains("dialing")
                    ) {
                    } else {
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

                            val currentTimeMillis = System.currentTimeMillis()
                            val currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), java.time.ZoneId.systemDefault())
                            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                            val formattedTime = currentDateTime.format(formatter)
                            val time = System.currentTimeMillis()
                            val isSystem = if (sbp.appNotifier?.let { isMessagingApp(it, applicationContext) } == true) {
                                0
                            } else if (sbp.appNotifier?.let { isSocialMedia(it) } == true) {
                                0
                            } else if (sbp.appNotifier?.let { isPhoneCall(it) } == true) {
                                0
                            } else {
                                1
                            }

                            val notification = if (contact != null) {
                                sbp = StatusBarParcelable(sbn, contact.id)
                                NotificationDB(
                                    id = 0,
                                    title = "",
                                    contactName = sbp.statusBarNotificationInfo["android.title"].toString(),
                                    description = sbp.statusBarNotificationInfo["android.text"].toString(),
                                    platform = sbp.appNotifier!!,
                                    timestamp = time,
                                    isCancellable = 0,
                                    idContact = contact.id,
                                    priority = contact.priority,
                                    phoneNumber = contact.listOfPhoneNumbers[0],
                                    mail = contact.listOfMails[0],
                                    messengerId = contact.messengerId,
                                    isSystem = isSystem
                                )
                            } else {
                                NotificationDB(
                                    id = 0,
                                    title = "",
                                    contactName = sbp.statusBarNotificationInfo["android.title"].toString(),
                                    description = sbp.statusBarNotificationInfo["android.text"].toString(),
                                    platform = sbp.appNotifier!!,
                                    timestamp = time,
                                    isCancellable = 0,
                                    idContact = 0,
                                    priority = 0,
                                    phoneNumber = sbp.statusBarNotificationInfo["android.title"].toString(),
                                    mail = sbp.statusBarNotificationInfo["android.title"].toString(),
                                    messengerId = "",
                                    isSystem = isSystem
                                )
                            }

                            if (sbp.appNotifier != "com.samsung.android.incallui") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    saveNotification.invoke(notification)
                                }
                                cancelWhatsappNotification(sbn, this@NotificationsListenerService)
                                contact?.let { contactDb ->
                                    if(sbp.appNotifier?.let { isFromAPhoneCallApp(it, this@NotificationsListenerService) } == true){
                                        displayNotification(sbp, sbn, contactDb, formattedTime)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationsListenerUseCases.saveNotification.invoke(
                            NotificationDB(
                                0,
                                sbp.tickerText.toString(),
                                sbp.statusBarNotificationInfo["android.title"].toString(),
                                sbp.statusBarNotificationInfo["android.text"].toString(),
                                sbp.appNotifier!!,
                                System.currentTimeMillis(),
                                0,
                                0,
                                0,
                                sbp.statusBarNotificationInfo["android.title"].toString(),
                                sbp.statusBarNotificationInfo["android.title"].toString(),
                                "",
                                1
                            )
                        )
                    }
                }
            }
        }
    }

    private fun displayNotification(sbp: StatusBarParcelable, sbn: StatusBarNotification, contact: ContactDB, formattedTime: String) {
        contact.apply {
            when (priority) {
                2 -> {
                    val date = DateFormat.getTimeInstance().calendar.time
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val hours = cal.get(Calendar.HOUR_OF_DAY)
                    val minutes = cal.get(Calendar.MINUTE)
                    val today = cal.get(Calendar.DAY_OF_WEEK)

                    val vipScheduleValueSharedPreferences = getSharedPreferences("VipScheduleValue", Context.MODE_PRIVATE)

                    val startTime = convertTimeToStartTime(hourLimitForNotification)
                    val hourStart = convertTimeToHour(startTime)
                    val minutesStart = convertTimeToMinutes(startTime)
                    val endTime = convertTimeToEndTime(hourLimitForNotification)
                    val hourEnd = convertTimeToHour(endTime)
                    val minutesEnd = convertTimeToMinutes(endTime)

                    when (vipSchedule) {
                        1 -> vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                        2 -> {
                            if (today in 1..4 || today == 7) {
                                if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
                                    if (hourStart.toInt() == hours) {
                                        if (minutes >= minutesStart.toInt()) {
                                            if (hourEnd.toInt() == hours) {
                                                if (minutes <= minutesEnd.toInt()) {
                                                    vipNotificationsDeployment(
                                                        sbp, sbn, contact, formattedTime
                                                    )
                                                }
                                            } else {
                                                vipNotificationsDeployment(
                                                    sbp, sbn, contact, formattedTime
                                                )
                                            }
                                        }
                                    } else if (hourEnd.toInt() == hours) {
                                        if (minutes <= minutesEnd.toInt()) {
                                            vipNotificationsDeployment(
                                                sbp, sbn, contact, formattedTime
                                            )
                                        }
                                    } else {
                                        vipNotificationsDeployment(
                                            sbp, sbn, contact, formattedTime
                                        )
                                    }
                                }
                            }
                        }

                        3 -> {
                            if (today == 5 || today == 6) {
                                vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                            }
                        }

                        4 -> {
                            if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
                                if (hourStart.toInt() == hours) {
                                    if (minutes >= minutesStart.toInt()) {
                                        if (hourEnd.toInt() == hours) {
                                            if (minutes <= minutesEnd.toInt()) {
                                                vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                            }
                                        } else {
                                            vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                        }
                                    }
                                } else if (hourEnd.toInt() == hours) {
                                    if (minutes <= minutesEnd.toInt()) {
                                        vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                    }
                                } else {
                                    vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                }
                            }
                        }

                        else -> {
                            if (vipScheduleValueSharedPreferences.getBoolean(
                                    "VipScheduleValue", false
                                )
                            ) {
                                if (today == 5 || today == 6) {
                                    val notificationsHour = getSharedPreferences(
                                        "TeleworkingReminder", Context.MODE_PRIVATE
                                    ).getString(
                                        "TeleworkingReminder", ""
                                    )

                                    if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
                                        if (hourStart.toInt() == hours) {
                                            if (minutes >= minutesStart.toInt()) {
                                                if (hourEnd.toInt() == hours) {
                                                    if (minutes <= minutesEnd.toInt()) {
                                                        vipNotificationsDeployment(
                                                            sbp, sbn, contact, formattedTime
                                                        )
                                                    }
                                                } else {
                                                    vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                                }
                                            }
                                        } else if (hourEnd.toInt() == hours) {
                                            if (minutes <= minutesEnd.toInt()) {
                                                vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                            }
                                        } else {
                                            vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                                        }
                                    }
                                }
                            } else {
                                vipNotificationsDeployment(sbp, sbn, contact, formattedTime)
                            }
                        }
                    }
                }

                1 -> {
                }

                0 -> {
                    this@NotificationsListenerService.cancelAllNotifications()
                    cancelWhatsappNotification(sbn, this@NotificationsListenerService)
                }
            }
        }
    }

    private fun vipNotificationsDeployment(sbp: StatusBarParcelable, sbn: StatusBarNotification, contact: ContactDB, formattedTime: String) {
        val screenListener = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val targetKey1 = "android.title"
        val targetKey2 = "android.text"

        val entryWithTitle = sbp.statusBarNotificationInfo.entries.find { it.key == targetKey1 }
        val entryWithText = sbp.statusBarNotificationInfo.entries.find { it.key == targetKey2 }
        val title = entryWithTitle?.value as String
        val text = when (entryWithText?.value) {
            is String -> {
                entryWithText.value as String
            }

            is SpannableString -> {
                entryWithText.value as SpannableString
            }

            else -> {
                ""
            }
        }

        val notification = NotificationAlarmViewState(
            title = title,
            content = text.toString(),
            platform = sbp.appNotifier,
            contactId = sbp.contactId,
            dateTime = sbp.dateTime
        )
        notificationsList.add(notification)

        if (screenListener.isKeyguardLocked) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                val i = Intent(this, NotificationAlarmActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("notification", sbp)

                cancelNotification(sbn.key)
                cancelWhatsappNotification(sbn, this)
                startActivity(i)
            } else {
                val i = Intent(this, NotificationAlarmActivity::class.java)
                i.putExtra("notification", sbp)
                cancelNotification(sbn.key)
                cancelWhatsappNotification(sbn, this)
                startActivity(i)
            }
        } else {
            cancelNotification(sbn.key)
            cancelWhatsappNotification(sbn, this)
            displayLayoutWithSharedPreferences(sbp, contact, formattedTime)
        }
    }

    private fun displayLayoutWithSharedPreferences(sbp: StatusBarParcelable, contactDB: ContactDB, formattedTime: String) {
        if (contactDB.isCustomSound == 1) {
            alertCustomNotificationTone(contactDB.notificationTone)
        } else {
            alertNotificationTone(contactDB.notificationSound)
        }

        if (appNotifiable(sbp, applicationContext) && sharedPreferences.getBoolean("popupNotif", false)) {
            if (adapterNotifications == null) {
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", false)
                edit.apply()
            }

            if (!sharedPreferences.getBoolean("view", false)) {
                popupView = null
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                displayLayout(sbp, contactDB, formattedTime)
            } else {
                popupNotificationViewStates.add(
                    PopupNotificationViewState(
                        title = sbp.statusBarNotificationInfo["android.title"].toString(),
                        description = sbp.statusBarNotificationInfo["android.text"].toString(),
                        platform = convertPackageToString(sbp.appNotifier!!, this),
                        contactName = "${contactDB.firstName} ${contactDB.lastName}",
                        date = formattedTime,
                        transformPhoneNumberToPhoneNumbersWithSpinner(contactDB.listOfPhoneNumbers),
                        contactDB.messengerId,
                        contactDB.listOfMails[0]
                    )
                )

                adapterNotifications?.submitList(popupNotificationViewStates.sortedByDescending {
                    it.date
                }.distinctBy {
                    PopupNotificationParams(
                        contactName = it.contactName,
                        description = it.description,
                        platform = it.platform,
                        date = it.date,
                    )
                })
                recyclerView?.adapter = adapterNotifications

                if (popupNotificationViewStates.size == 1) {
                    numberOfMessages?.text = context?.getString(R.string.popup_1_message)
                } else {
                    numberOfMessages?.text = context?.getString(R.string.messages_with_number, popupNotificationViewStates.size)
                }
            }
        }
    }

    private fun displayLayout(sbp: StatusBarParcelable, contactDB: ContactDB, formattedTime: String) {
        val flag = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        val parameters = WindowManager.LayoutParams(
            flag, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT
        )
        parameters.gravity = Gravity.RIGHT or Gravity.TOP
        parameters.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        popupView = inflater.inflate(R.layout.layout_notification_pop_up, null)
        val popupDropable = popupView?.findViewById<ConstraintLayout>(R.id.notification_dropable)
        val container = popupView?.findViewById<LinearLayout>(R.id.notification_popup_main_layout)
        recyclerView = popupView?.findViewById(R.id.notification_popup_recycler_view)
        recyclerView?.layoutManager = LinearLayoutManager(applicationContext)

        numberOfMessages = popupView?.findViewById(R.id.number_of_messages)

        numberOfMessages?.text = getString(R.string.popup_1_message)

        if (windowManager != null && popupView != null) {
            adapterNotifications = PopupNotificationsListAdapter(
                applicationContext, windowManager!!, popupView!!
            )
        }

        notificationsRecyclerViewDisplay(sbp, contactDB, formattedTime)

        try {
            windowManager?.addView(popupView, parameters)
        } finally {
            windowManager?.updateViewLayout(popupView, parameters)
        }
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

                    container?.x = positionXIntoScreen(
                        container?.x!!, deplacementX, container?.width?.toFloat()
                            ?: 0F, windowManager!!
                    )
                    oldPosX = x - deplacementX

                    container?.y = positionYIntoScreen(
                        container?.y ?: 0F, deplacementY, container?.height?.toFloat()
                            ?: 0F, windowManager!!
                    )
                    oldPosY = y - deplacementY
                }
            }
            return@setOnTouchListener true
        }

        if (sharedPreferences.getBoolean("first_notif", true)) {
            contactDB.let {
                if (!popupNotificationViewStates.contains(
                        PopupNotificationViewState(
                            title = sbp.statusBarNotificationInfo["android.title"].toString(),
                            description = sbp.statusBarNotificationInfo["android.text"].toString(),
                            platform = convertPackageToString(sbp.appNotifier!!, this),
                            contactName = "${it.firstName} ${it.lastName}",
                            date = formattedTime,
                            listOfPhoneNumbersWithSpinner = transformPhoneNumberToPhoneNumbersWithSpinner(contactDB.listOfPhoneNumbers),
                            messengerId = it.messengerId,
                            email = it.listOfMails[0]
                        )
                    )
                ) {
                    popupNotificationViewStates.add(
                        PopupNotificationViewState(
                            title = sbp.statusBarNotificationInfo["android.title"].toString(),
                            description = sbp.statusBarNotificationInfo["android.text"].toString(),
                            platform = convertPackageToString(sbp.appNotifier!!, this),
                            contactName = "${it.firstName} ${it.lastName}",
                            date = formattedTime,
                            listOfPhoneNumbersWithSpinner = transformPhoneNumberToPhoneNumbersWithSpinner(contactDB.listOfPhoneNumbers),
                            messengerId = it.messengerId,
                            email = it.listOfMails[0]
                        )
                    )
                }
            }

            val list = popupNotificationViewStates.sortedByDescending {
                it.date
            }.distinctBy {
                PopupNotificationParams(
                    contactName = it.contactName,
                    description = it.description,
                    platform = it.platform,
                    date = it.date,
                )
            }

            adapterNotifications?.submitList(null)
            adapterNotifications?.submitList(list)
            recyclerView?.adapter = adapterNotifications

            val edit = sharedPreferences.edit()
            edit.putBoolean("first_notif", false)
            edit.apply()
        }
    }

    private fun notificationsRecyclerViewDisplay(sbp: StatusBarParcelable, contactDB: ContactDB, formattedTime: String) {
        addNotificationViewStateToList(popupNotificationViewStates, contactDB, sbp, applicationContext, formattedTime)

        adapterNotifications?.let { it ->
            it.submitList(null)
            it.submitList(popupNotificationViewStates.sortedByDescending { notification ->
                notification.date
            }.distinctBy {
                PopupNotificationParams(
                    contactName = it.contactName,
                    description = it.description,
                    platform = it.platform,
                    date = it.date,
                )
            })
            recyclerView?.adapter = it
            val itemTouchHelper = ItemTouchHelper(PopupNotificationsSwipeDelete(it))
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        if (popupNotificationViewStates.size == 0) {
            alarmSound?.stop()
        }

//        if (adapterNotification?.isClose == true) {
//            alarmSound?.stop()
//        }

        val imgClose = popupView?.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.VISIBLE
        imgClose.setOnClickListener {
            popupView?.let {
                windowManager?.removeView(it)
            }
            popupView = null
            alarmSound?.stop()
            popupNotificationViewStates.clear()
            notificationsList.clear()

            val edit = sharedPreferences.edit()
            edit.putBoolean("view", false)
            edit.putBoolean("first_notif", true)
            edit.apply()
        }
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
        var alarmSound: MediaPlayer? = null
        var numberOfMessages: TextView? = null
        val popupNotificationViewStates = mutableSetOf<PopupNotificationViewState>()
        val notificationsList = mutableSetOf<NotificationAlarmViewState>()
        var adapterNotifications: PopupNotificationsListAdapter? = null
        private var recyclerView: RecyclerView? = null
        var context: Context? = null

        fun deleteItem(notification: PopupNotificationViewState) {
            if (popupNotificationViewStates.distinctBy {
                    PopupNotificationParams(
                        contactName = it.contactName,
                        description = it.description,
                        platform = it.platform,
                        date = it.date,
                    )
                }.isNotEmpty()) {
                popupNotificationViewStates.remove(notification)
            }

            adapterNotifications?.let { it ->
                it.submitList(null)

                val list = popupNotificationViewStates.sortedByDescending { notification ->
                    notification.date
                }

                it.submitList(list)
                recyclerView?.adapter = it
                val itemTouchHelper = ItemTouchHelper(PopupNotificationsSwipeDelete(it))
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }

            if (popupNotificationViewStates.distinctBy {
                    PopupNotificationParams(
                        contactName = it.contactName,
                        description = it.description,
                        platform = it.platform,
                        date = it.date,
                    )
                }.size == 1) {
                numberOfMessages?.text = context?.getString(R.string.popup_1_message)
            } else {
                numberOfMessages?.text = context?.getString(R.string.messages_with_number, popupNotificationViewStates.size)
            }
            alarmSound?.stop()
        }
    }
}