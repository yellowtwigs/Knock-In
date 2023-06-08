package com.yellowtwigs.knockin.model.service

import android.annotation.SuppressLint
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
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.addNotificationViewStateToList
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.appNotifiable
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.cancelWhatsappNotification
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.messagesNotUseless
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.positionXIntoScreen
import com.yellowtwigs.knockin.model.service.NotificationsListenerGesture.positionYIntoScreen
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.notifications.NotificationAlarmActivity
import com.yellowtwigs.knockin.utils.ContactGesture.isPhoneNumber
import com.yellowtwigs.knockin.utils.ContactGesture.isValidEmail
import com.yellowtwigs.knockin.utils.ContactGesture.transformPhoneNumberToPhoneNumbersWithSpinner
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

@AndroidEntryPoint
@SuppressLint("OverrideAbstract")
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
        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        durationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)


        var sbp = StatusBarParcelable(sbn, 0)
        if (sharedPreferences.getBoolean("serviceNotif", true) && messagesNotUseless(sbp, resources)) {
            sbp.castName()
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()

            Log.i("GetKnockinNotif", "sbp.appNotifier : ${sbp.appNotifier}")
            Log.i("GetKnockinNotif", "name : $name")
            Log.i("GetKnockinNotif", "message : $message")

            if (name != "" && message != "" && name != "null" && message != "null") {
                if (sbp.appNotifier?.let { convertPackageToString(it, this) } != "") {
                    if (message.contains("call") || message.contains("Incoming") || message.contains(
                            "Incoming Call"
                        ) || message.contains("Appel entrant") || message.contains("appel entrant")
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
                            Log.i("GoToWithContact", "sbp.appNotifier : ${sbp.appNotifier}")

                            val notification = if (contact != null) {
                                sbp = StatusBarParcelable(sbn, contact.id)
                                NotificationDB(
                                    0,
                                    sbp.tickerText.toString(),
                                    sbp.statusBarNotificationInfo["android.title"].toString(),
                                    sbp.statusBarNotificationInfo["android.text"].toString(),
                                    sbp.appNotifier!!,
                                    System.currentTimeMillis(),
                                    0,
                                    contact.id,
                                    contact.priority,
                                    contact.listOfPhoneNumbers[0],
                                    contact.listOfMails[0],
                                    contact.messengerId,
                                    0
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
                                    0,
                                    sbp.statusBarNotificationInfo["android.title"].toString(),
                                    sbp.statusBarNotificationInfo["android.title"].toString(),
                                    "",
                                    0
                                )
                            }

                            if (sbp.appNotifier != "com.samsung.android.incallui") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    saveNotification.invoke(notification)
                                }
                                cancelWhatsappNotification(
                                    sbn, this@NotificationsListenerService
                                )
                                contact?.let {
                                    displayNotification(sbp, sbn, it)
                                }
                            }
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationsListenerUseCases.apply {
                            if (!checkDuplicateNotificationUseCase.invoke(
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
                            ) {
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
        }
    }

    private fun displayNotification(
        sbp: StatusBarParcelable, sbn: StatusBarNotification, contact: ContactDB
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

                    val vipScheduleValueSharedPreferences = getSharedPreferences(
                        "VipScheduleValue", Context.MODE_PRIVATE
                    )

                    val startTime = convertTimeToStartTime(hourLimitForNotification)
                    val hourStart = convertTimeToHour(startTime)
                    val minutesStart = convertTimeToMinutes(startTime)
                    val endTime = convertTimeToEndTime(hourLimitForNotification)
                    val hourEnd = convertTimeToHour(endTime)
                    val minutesEnd = convertTimeToMinutes(endTime)

                    when (vipSchedule) {
                        1 -> vipNotificationsDeployment(sbp, sbn, contact)
                        2 -> {
                            if (today in 1..4 || today == 7) {
                                if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
                                    if (hourStart.toInt() == hours) {
                                        if (minutes >= minutesStart.toInt()) {
                                            if (hourEnd.toInt() == hours) {
                                                if (minutes <= minutesEnd.toInt()) {
                                                    vipNotificationsDeployment(
                                                        sbp, sbn, contact
                                                    )
                                                }
                                            } else {
                                                vipNotificationsDeployment(
                                                    sbp, sbn, contact
                                                )
                                            }
                                        }
                                    } else if (hourEnd.toInt() == hours) {
                                        if (minutes <= minutesEnd.toInt()) {
                                            vipNotificationsDeployment(
                                                sbp, sbn, contact
                                            )
                                        }
                                    } else {
                                        vipNotificationsDeployment(
                                            sbp, sbn, contact
                                        )
                                    }
                                }
                            }
                        }
                        3 -> {
                            if (today == 5 || today == 6) {
                                vipNotificationsDeployment(sbp, sbn, contact)
                            }
                        }
                        4 -> {
                            if (hourStart.toInt() <= hours && hourEnd.toInt() >= hours) {
                                if (hourStart.toInt() == hours) {
                                    if (minutes >= minutesStart.toInt()) {
                                        if (hourEnd.toInt() == hours) {
                                            if (minutes <= minutesEnd.toInt()) {
                                                vipNotificationsDeployment(sbp, sbn, contact)
                                            }
                                        } else {
                                            vipNotificationsDeployment(sbp, sbn, contact)
                                        }
                                    }
                                } else if (hourEnd.toInt() == hours) {
                                    if (minutes <= minutesEnd.toInt()) {
                                        vipNotificationsDeployment(sbp, sbn, contact)
                                    }
                                } else {
                                    vipNotificationsDeployment(sbp, sbn, contact)
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
                                                            sbp, sbn, contact
                                                        )
                                                    }
                                                } else {
                                                    vipNotificationsDeployment(sbp, sbn, contact)
                                                }
                                            }
                                        } else if (hourEnd.toInt() == hours) {
                                            if (minutes <= minutesEnd.toInt()) {
                                                vipNotificationsDeployment(sbp, sbn, contact)
                                            }
                                        } else {
                                            vipNotificationsDeployment(sbp, sbn, contact)
                                        }
                                    }
                                }
                            } else {
                                vipNotificationsDeployment(sbp, sbn, contact)
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

    fun vipNotificationsDeployment(sbp: StatusBarParcelable, sbn: StatusBarNotification, contact: ContactDB) {
        val screenListener = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
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
            displayLayoutWithSharedPreferences(sbp, contact)
        }
    }


    private fun displayLayoutWithSharedPreferences(
        sbp: StatusBarParcelable, contactDB: ContactDB
    ) {
        Log.i("GetDefaultSound", "contactDB.isCustomSound : ${contactDB.isCustomSound}")
        if (contactDB.isCustomSound == 1) {
            Log.i("GetDefaultSound", "contactDB.notificationTone : ${contactDB.notificationTone}")
            alertCustomNotificationTone(contactDB.notificationTone)
        } else {
            Log.i("GetDefaultSound", "contactDB.notificationSound : ${contactDB.notificationSound}")
            alertNotificationTone(contactDB.notificationSound)
        }

        if (appNotifiable(sbp, applicationContext) && sharedPreferences.getBoolean(
                "popupNotif", false
            )
        ) {
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
                displayLayout(sbp, contactDB)
            } else {
                popupNotificationViewStates.add(
                    PopupNotificationViewState(
                        popupNotificationViewStates.size,
                        sbp.statusBarNotificationInfo["android.title"].toString(),
                        sbp.statusBarNotificationInfo["android.text"].toString(),
                        convertPackageToString(sbp.appNotifier!!, this),
                        "${contactDB.firstName} ${contactDB.lastName}",
                        transformPhoneNumberToPhoneNumbersWithSpinner(contactDB.listOfPhoneNumbers),
                        contactDB.messengerId,
                        contactDB.listOfMails[0]
                    )
                )

                adapterNotifications?.submitList(popupNotificationViewStates.sortedByDescending {
                    it.id
                }.distinct())
                recyclerView?.adapter = adapterNotifications

                numberOfMessages?.text = "${popupNotificationViewStates.size} messages"
            }
        }
    }

    private fun displayLayout(
        sbp: StatusBarParcelable, contactDB: ContactDB
    ) {
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

        numberOfMessages = popupView?.findViewById<TextView>(R.id.number_of_messages)

        numberOfMessages?.text = "1 message"

        if (windowManager != null && popupView != null) {
            adapterNotifications = PopupNotificationsListAdapter(
                applicationContext, windowManager!!, popupView!!
            )
        }

        notificationsRecyclerViewDisplay(sbp, contactDB)

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
                        container?.x!!, deplacementX, container?.width?.toFloat() ?: 0F, windowManager!!
                    )
                    oldPosX = x - deplacementX

                    container?.y = positionYIntoScreen(
                        container?.y ?: 0F, deplacementY, container?.height?.toFloat() ?: 0F, windowManager!!
                    )
                    oldPosY = y - deplacementY
                }
            }
            return@setOnTouchListener true
        }

        if (sharedPreferences.getBoolean("first_notif", true)) {
            contactDB.let {
                popupNotificationViewStates.add(
                    PopupNotificationViewState(
                        popupNotificationViewStates.size,
                        sbp.statusBarNotificationInfo["android.title"].toString(),
                        sbp.statusBarNotificationInfo["android.text"].toString(),
                        convertPackageToString(sbp.appNotifier!!, this),
                        "${it.firstName} ${it.lastName}",
                        transformPhoneNumberToPhoneNumbersWithSpinner(contactDB.listOfPhoneNumbers),
                        it.messengerId,
                        it.listOfMails[0]
                    )
                )
            }

            adapterNotifications?.submitList(null)
            adapterNotifications?.submitList(popupNotificationViewStates.sortedByDescending {
                it.id
            }.distinct())
            recyclerView?.adapter = adapterNotifications

            val edit = sharedPreferences.edit()
            edit.putBoolean("first_notif", false)
            edit.apply()
        }
    }

    private fun notificationsRecyclerViewDisplay(
        sbp: StatusBarParcelable, contactDB: ContactDB
    ) {
        addNotificationViewStateToList(
            popupNotificationViewStates, contactDB, sbp, applicationContext
        )

        adapterNotifications?.let { it ->
            it.submitList(null)
            it.submitList(popupNotificationViewStates.sortedByDescending { notification ->
                notification.id
            }.distinct())
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
        var TAG = NotificationsListenerService::class.java.simpleName
        var alarmSound: MediaPlayer? = null
        var numberOfMessages: TextView? = null
        val popupNotificationViewStates = arrayListOf<PopupNotificationViewState>()
        var adapterNotifications: PopupNotificationsListAdapter? = null
        private var recyclerView: RecyclerView? = null

        fun deleteItem(position: Int) {
            if (popupNotificationViewStates.size > position) {
                popupNotificationViewStates.removeAt(position)
            }

            adapterNotifications?.let { it ->
                it.submitList(null)
                it.submitList(popupNotificationViewStates.sortedByDescending { notification ->
                    notification.id
                }.distinct())
                recyclerView?.adapter = it
                val itemTouchHelper = ItemTouchHelper(PopupNotificationsSwipeDelete(it))
                itemTouchHelper.attachToRecyclerView(recyclerView)
            }

            if (popupNotificationViewStates.size == 1) {
                numberOfMessages?.text = "1 message"
            } else {
                numberOfMessages?.text = "${popupNotificationViewStates.size} messages"
            }
            alarmSound?.stop()
        }
    }
}