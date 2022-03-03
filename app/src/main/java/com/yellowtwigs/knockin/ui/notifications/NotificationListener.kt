package com.yellowtwigs.knockin.ui.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.database.Cursor
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.model.data.*
import com.yellowtwigs.knockin.utils.Converter.convertPackageToString
import java.io.File
import java.io.FileInputStream
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Service qui nous permet de traiter les notifications
 */
@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    // Database && Thread
    private var database: ContactsRoomDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private var oldPosX: Float = 0.0f
    private var oldPosY: Float = 0.0f
    private var popupView: View? = null
    private var windowManager: WindowManager? = null
    private var notificationPopupRecyclerView: RecyclerView? = null

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var durationPreferences: SharedPreferences
    private var duration = 0

    private lateinit var tonePreferences: SharedPreferences
    private lateinit var alarmCustomTonePreferences: SharedPreferences
    private var customSound = ""

    private lateinit var schedulePreferences: SharedPreferences

    private lateinit var canRingtonePreferences: SharedPreferences
    private var canRingtone = false


    /**
     * La première fois que le service est crée nous définnissons les valeurs pour les threads
     */
    override fun onCreate() {
        super.onCreate()
        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        database = ContactsRoomDatabase.getDatabase(this)
    }

    /**
     * Si le service à été déconnecté on demande d'être reconnecter
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    /**
     *Lors de la réception d'un message celle-ci effectue le traitement de la notification selon sa priorité
     * @param sbn StatusBarNotification élément de la statusbar qui vient d'être publié
     */
    @SuppressLint("WrongConstant")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        canRingtonePreferences = getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        durationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        tonePreferences = getSharedPreferences("Alarm_Tone", Context.MODE_PRIVATE)
        alarmCustomTonePreferences = getSharedPreferences("Alarm_Custom_Tone", Context.MODE_PRIVATE)
        schedulePreferences = getSharedPreferences("Schedule_VIP", Context.MODE_PRIVATE)

        val sbp = StatusBarParcelable(sbn)
        if (sharedPreferences.getBoolean("serviceNotif", false) && messagesNotUseless(sbp)) {

            sbp.castName()
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()
            val app = convertPackageToString(sbp.appNotifier!!, this)

            val contactManager = ContactManager(this)
            if (message == "Incoming voice call" || message == "Appel vocal entrant") {

            } else {
                val addNotification = Runnable {
                    val contact: ContactWithAllInformation?

                    if (isPhoneNumber(name)) {
                        contact = contactManager.getContactFromNumber(name)
                        if (contact != null)
                            sbp.changeToContactName(contact)
                    } else {
                        contact = contactManager.getContactWithName(name, app)
                    }

                    val notification = saveNotification(sbp, contactManager.getContactId(name))
                    if (notification != null && notificationNotDouble(notification) && sbp.appNotifier != this.packageName &&
                        sbp.appNotifier != "com.samsung.android.incallui"
                    ) {
                        notification.insertNotifications(database!!) //ajouter notification a la database
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                            if (contact != null) {
                                when (contact.contactDB?.contactPriority) {
                                    2 -> {
                                        val date = DateFormat.getTimeInstance().calendar.time
                                        val cal = Calendar.getInstance()
                                        cal.time = date
                                        val hours = cal.get(Calendar.HOUR_OF_DAY)
                                        val today = cal.get(Calendar.DAY_OF_WEEK)

                                        when (schedulePreferences.getInt("Schedule_VIP", 1)) {
                                            1 -> {
                                                vipNotificationsDeployment(sbp, sbn, contact)
                                            }
                                            2 -> {
                                                if (hours in 7..20) {
                                                    vipNotificationsDeployment(sbp, sbn, contact)
                                                }
                                            }
                                            3 -> {
                                                if (today in 1..4 || today == 7) {
                                                    vipNotificationsDeployment(sbp, sbn, contact)
                                                }
                                            }
                                            4 -> {
                                                if (hours in 7..20) {
                                                    if (today == 5 || today == 6) {
                                                        vipNotificationsDeployment(
                                                            sbp,
                                                            sbn,
                                                            contact
                                                        )
                                                    }
                                                } else if (today in 1..4 || today == 7) {
                                                    vipNotificationsDeployment(sbp, sbn, contact)
                                                }
                                            }
                                            else -> {
                                                vipNotificationsDeployment(sbp, sbn, contact)
                                            }
                                        }
                                    }
                                    1 -> {
                                    }
                                    0 -> {
                                        this.cancelAllNotifications()
                                        cancelWhatsappNotif(sbn)
                                    }
                                }
                            } else {
                                if (sbn.key.contains("whatsapp")) {
                                    cancelWhatsappNotif(sbn)
                                } else {
                                    if (sbn.packageName == MESSAGE_PACKAGE || sbn.packageName == MESSAGE_SAMSUNG_PACKAGE || sbn.packageName == XIAOMI_MESSAGE_PACKAGE || sbn.packageName == WHATSAPP_SERVICE) {
                                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                                            var i = 0
                                            val vipNotif = VipNotificationsDB(
                                                null,
                                                sbp.id,
                                                sbp.appNotifier!!,
                                                sbp.tailleList,
                                                sbp.tickerText!!
                                            )
                                            val notifId =
                                                database!!.VipNotificationsDao()
                                                    .insert(vipNotif)
                                            while (i < sbp.key.size) {
                                                if (sbp.key[i] == "android.title" || sbp.key[i] == "android.text" || sbp.key[i] == "android.largeIcon") {
                                                    val VipSbn = VipSbnDB(
                                                        null,
                                                        notifId!!.toInt(),
                                                        sbp.key[i],
                                                        sbp.statusBarNotificationInfo[sbp.key[i]].toString()
                                                    )
                                                    database!!.VipSbnDao()
                                                        .insert(VipSbn)
                                                }
                                                i++
                                            }
                                            //this.cancelNotification(sbn.key)
                                        }
                                        val screenListener: KeyguardManager =
                                            this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                                        if (screenListener.isKeyguardLocked) {
                                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                                                this.cancelNotification(sbn.key)
                                                cancelWhatsappNotif(sbn)

                                                displayLayoutWithSharedPreferences(
                                                    sbp,
                                                    null
                                                )
                                            } else {
                                                val i = Intent(
                                                    this@NotificationListener,
                                                    NotificationAlarmActivity::class.java
                                                )
                                                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                i.putExtra("notification", sbp)
                                                this.cancelNotification(sbn.key)
                                                cancelWhatsappNotif(sbn)
                                                startActivity(i)
                                            }
                                        } else {
                                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                                                this.cancelNotification(sbn.key)
                                                cancelWhatsappNotif(sbn)
                                            }
                                            displayLayoutWithSharedPreferences(
                                                sbp,
                                                null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                mDbWorkerThread.postTask(addNotification)
            }
        }
    }

    private fun cancelWhatsappNotif(sbn: StatusBarNotification) {
        this.activeNotifications.forEach {
            if (it.key.contains("whatsapp") && sbn.key.takeLast(6) == it.key.takeLast(6)) {
                this.cancelNotification(it.key)
            }
        }
    }

    private fun notificationNotDouble(notification: NotificationDB): Boolean {
        val lastInsertId = database!!.notificationsDao().lastInsert()
        val lastInsert = database!!.notificationsDao()
            .getNotification(lastInsertId)
        val listLastInsert = database!!.notificationsDao()
            .lastInsertByTime(System.currentTimeMillis())
        for (lastNotif in listLastInsert) {
            if (lastNotif != null && lastNotif.platform == notification.platform && lastNotif.description == notification.description) {
                return false
            }
        }
        if (lastInsert != null && lastInsert.platform == notification.platform && lastInsert.title == notification.title && lastInsert.description == notification.description && notification.timestamp - lastInsert.timestamp < 1000) {
            return false
        }
        return true
    }

    /**
     * Crée une notification qui sera sauvegardé
     */
    private fun saveNotification(sbp: StatusBarParcelable, contactId: Int): NotificationDB? {
        return if (sbp.statusBarNotificationInfo["android.title"] != null && sbp.statusBarNotificationInfo["android.text"] != null) {
            NotificationDB(
                null,
                sbp.tickerText.toString(),
                sbp.statusBarNotificationInfo["android.title"]!!.toString(),
                sbp.statusBarNotificationInfo["android.text"]!!.toString(),
                sbp.appNotifier!!, false,
                System.currentTimeMillis(), 0,
                contactId
            )
        } else {
            null
        }
    }

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

    private fun vipNotificationsDeployment(
        sbp: StatusBarParcelable,
        sbn: StatusBarNotification,
        contact: ContactWithAllInformation
    ) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            var i = 0
            val vipNotif = VipNotificationsDB(
                null,
                sbp.id,
                sbp.appNotifier!!,
                sbp.tailleList,
                sbp.tickerText!!
            )
            val notifId =
                database?.VipNotificationsDao()?.insert(vipNotif)
            while (i < sbp.key.size) {
                if (sbp.key[i] == "android.title" || sbp.key[i] == "android.text" || sbp.key[i] == "android.largeIcon") {
                    val vipSbn = VipSbnDB(
                        null,
                        notifId!!.toInt(),
                        sbp.key[i],
                        sbp.statusBarNotificationInfo[sbp.key[i]].toString()
                    )
                    database!!.VipSbnDao()
                        .insert(vipSbn)
                }
                i++
            }
        }
        val screenListener =
            getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (screenListener.isKeyguardLocked) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                val i = Intent(
                    this@NotificationListener,
                    NotificationAlarmActivity::class.java
                )
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("notification", sbp)
                i.putExtra(
                    "notificationSound",
                    contact.contactDB?.notificationSound
                )
                this.cancelNotification(sbn.key)
                cancelWhatsappNotif(sbn)
                startActivity(i)
            } else {
                val i = Intent(
                    this@NotificationListener,
                    NotificationAlarmActivity::class.java
                )
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                i.putExtra("notification", sbp)
                this.cancelNotification(sbn.key)
                cancelWhatsappNotif(sbn)
                startActivity(i)
            }
        } else {
            this.cancelNotification(sbn.key)
            cancelWhatsappNotif(sbn)
            displayLayoutWithSharedPreferences(
                sbp,
                contact.contactDB!!.notificationSound
            )
        }
    }

    private fun displayLayoutWithSharedPreferences(sbp: StatusBarParcelable, idSound: Int?) {
        soundRingtone(idSound)

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                canRingtone = true
                val edit = canRingtonePreferences.edit()
                edit?.putBoolean("Can_RingTone", canRingtone)
                edit?.apply()

                handler.postDelayed(this, duration.toLong())
            }
        }, duration.toLong())

        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif", false)) {
            if (adapterNotification == null) {
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", false)
                edit.apply()
            }
            if (!sharedPreferences.getBoolean("view", false)) {
                popupView = null
                val edit = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                displayLayout(sbp, idSound)
            } else {
                adapterNotification?.addNotification(sbp)
            }
        }
    }

    private fun displayLayout(
        sbp: StatusBarParcelable,
        idSound: Int?
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
            notifLayout(sbp, popupView, idSound)
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
                adapterNotification = NotifPopupRecyclerViewAdapter(
                    applicationContext,
                    notifications,
                    windowManager!!,
                    view
                )
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
        idSound: Int?
    ) {
        val notifications: ArrayList<StatusBarParcelable> = ArrayList()
        notifications.add(sbp)
        notificationPopupRecyclerView = view?.findViewById(R.id.notification_popup_recycler_view)
        notificationPopupRecyclerView?.layoutManager = LinearLayoutManager(applicationContext)
        adapterNotification = NotifPopupRecyclerViewAdapter(
            applicationContext,
            notifications,
            windowManager!!,
            view!!
        )
        notificationPopupRecyclerView?.adapter = adapterNotification

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapterNotification))
        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)

        soundRingtone(idSound)

        if (notifications.size == 0) {
            alarmSound?.stop()
        }

        if (adapterNotification?.isClose == true) {
            alarmSound?.stop()
        }

        val imgClose = view.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
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

    private fun isPhoneNumber(title: String): Boolean {
        val pregMatchString =
            "((?:\\+|00)[17](?: |\\-)?|(?:\\+|00)[1-9]\\d{0,2}(?: |\\-)?|(?:\\+|00)1\\-\\d{3}(?: |\\-)?)?(0\\d|\\([0-9]{3}\\)|[1-9]{0,3})(?:((?: |\\-)[0-9]{2}){4}|((?:[0-9]{2}){4})|((?: |\\-)[0-9]{3}(?: |\\-)[0-9]{4})|([0-9]{7}))"
        return title.matches(pregMatchString.toRegex())
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

        val editCanRingtone = canRingtonePreferences.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()
    }

    private fun alertCustomNotificationTone(customSound: String) {
        alarmSound?.stop()
        alarmSound = MediaPlayer.create(applicationContext, Uri.parse(customSound))
        alarmSound?.start()

        val editDuration = durationPreferences.edit()
        alarmSound?.duration?.let { editDuration.putInt("Alarm_Notif_Duration", it) }
        editDuration.apply()

        val editCanRingtone = canRingtonePreferences.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()
    }

    private fun soundRingtone(idSound: Int?) {
        duration = durationPreferences.getInt("Alarm_Notif_Duration", 4000)
        canRingtone = canRingtonePreferences.getBoolean("Can_RingTone", true)
        customSound = alarmCustomTonePreferences.getString("Alarm_Custom_Tone", "").toString()

        if (canRingtone) {
            if (idSound != null) {
                if (idSound != R.raw.sms_ring) {
                    if (idSound == -1 && customSound.isNotEmpty()) {
                        alertCustomNotificationTone(customSound)
                    } else {
                        alertNotificationTone(idSound)
                    }
                } else {
                    alertNotificationTone(R.raw.sms_ring)
                }
            }
        }
    }

    companion object {
        var TAG = NotificationListener::class.java.simpleName
        const val FACEBOOK_PACKAGE = "com.facebook.katana"
        const val MESSENGER_PACKAGE = "com.facebook.orca"
        const val WHATSAPP_SERVICE = "com.whatsapp"
        const val GMAIL_PACKAGE = "com.google.android.gm"
        const val OUTLOOK_PACKAGE = "com.microsoft.office.outlook"
        const val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
        const val XIAOMI_MESSAGE_PACKAGE = "com.android.mms"
        const val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"
        const val TELEGRAM_PACKAGE = "org.telegram.messenger"
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        var alarmSound: MediaPlayer? = null

        @SuppressLint("StaticFieldLeak")
        var adapterNotification: NotifPopupRecyclerViewAdapter? = null
    }
}