package com.yellowtwigs.knockin.controller

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.NotificationAlarmActivity
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB
import com.yellowtwigs.knockin.model.ModelDB.VipNotificationsDB
import com.yellowtwigs.knockin.model.ModelDB.VipSbnDB
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.ui.adapters.NotifPopupRecyclerViewAdapter
import com.yellowtwigs.knockin.utils.Converter.convertPackageToString

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

    private var alarmSound: MediaPlayer? = null
    private lateinit var popupViewSharedPreferences: SharedPreferences

    private var sharedAlarmNotifDurationPreferences: SharedPreferences? = null
    private var duration = 0

    private var sharedAlarmNotifCanRingtonePreferences: SharedPreferences? = null
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
        val sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        popupViewSharedPreferences =
            getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val sbp = StatusBarParcelable(sbn)
        if (sharedPreferences.getBoolean("serviceNotif", false) && messagesNotUseless(sbp)) {

            sbp.castName()//permet de récupérer le vrai nom ou numéro du contact
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()
            val app = convertPackageToString(sbp.appNotifier!!, this)

            val contactManager = ContactManager(this)
            if (message == "Incoming voice call" || message == "Appel vocal entrant") {

            } else {
                val addNotification = Runnable {
                    val contact: ContactWithAllInformation?

                    //region Permet de Changer un numéro de téléphone en Prenom Nom
                    if (isPhoneNumber(name)) {
                        contact = contactManager.getContactFromNumber(name)
                        if (contact != null)
                            sbp.changeToContactName(contact)
                    } else {
                        contact = contactManager.getContactWithName(name, app)
                    }
                    //endregion

                    val notification = saveNotification(sbp, contactManager.getContactId(name))
                    if (notification != null && notificationNotDouble(notification) && sbp.appNotifier != this.packageName &&
                        sbp.appNotifier != "com.samsung.android.incallui"
                    ) {
                        notification.insertNotifications(database!!) //ajouter notification a la database
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                            if (contact != null) {
                                when (contact.contactDB!!.contactPriority) {
                                    2 -> {
                                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) { //get le message dans la DB et le cancel si c'est le meme
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
                                                sharedPreferences,
                                                contact.contactDB!!.notificationSound
                                            )
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
                                                    sharedPreferences,
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
                                                sharedPreferences,
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

    private fun displayLayoutWithSharedPreferences(
        sbp: StatusBarParcelable,
        sharedPreferences: SharedPreferences,
        idSound: Int?
    ) {
        val sharedAlarmNotifTonePreferences: SharedPreferences =
            getSharedPreferences("Alarm_Tone", Context.MODE_PRIVATE)
        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Tone", R.raw.sms_ring)

        sharedAlarmNotifDurationPreferences =
            getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        duration = sharedAlarmNotifDurationPreferences!!.getInt("Alarm_Notif_Duration", 3892)

        sharedAlarmNotifCanRingtonePreferences =
            getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        canRingtone = sharedAlarmNotifCanRingtonePreferences!!.getBoolean("Can_RingTone", true)

        val customSound = sharedAlarmNotifTonePreferences.getString("Alarm_Custom_Tone", null)

        if (canRingtone) {
            if (customSound == null) {
                if (idSound != null) {
                    if (sound == idSound) {
                        alertNotificationTone(sound)
                    } else {
                        alertNotificationTone(idSound)
                    }
                }
            } else {
                alertCustomNotificationTone(customSound.toString())
            }
        }

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                canRingtone = true

                val edit: SharedPreferences.Editor = sharedAlarmNotifCanRingtonePreferences!!.edit()
                edit.putBoolean("Can_RingTone", canRingtone)
                edit.apply()

                handler.postDelayed(this, duration.toLong())
            }
        }, duration.toLong())

        if (appNotifiable(sbp) && popupViewSharedPreferences.getBoolean("popupNotif", false)) {
            if(adapterNotification == null){
                val edit = popupViewSharedPreferences.edit()
                edit.putBoolean("view", false)
                edit.apply()
            }
            if (!popupViewSharedPreferences.getBoolean("view", false)) {
                popupView = null
                val edit = popupViewSharedPreferences.edit()
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

        Log.i("displayLayout", "Passe par la 2")
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
            val sharedPreferences =
                getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
            if (sharedPreferences.getBoolean("first_notif", true)) {
                val view = inflater.inflate(R.layout.layout_notification_pop_up, null)
                val notifications: ArrayList<StatusBarParcelable> = ArrayList()
                notifications.add(sbp)
                adapterNotification = NotifPopupRecyclerViewAdapter(
                    applicationContext,
                    notifications,
                    windowManager!!,
                    view,
                    alarmSound
                )
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("first_notif", false)
                edit.apply()
            }
        }
    }

    private fun positionXIntoScreen(popupX: Float, deplacementX: Float, popupSizeX: Float): Float {
        val metrics = DisplayMetrics()
        windowManager!!.defaultDisplay.getMetrics(metrics)
        if (popupX + deplacementX < 0) {
            return 0.0f
        } else if (popupX + deplacementX + popupSizeX < metrics.widthPixels) {
            return popupX + deplacementX
        } else {
            return metrics.widthPixels.toFloat() - popupSizeX
        }
    }

    private fun positionYIntoScreen(popupY: Float, deplacementY: Float, popupSizeY: Float): Float {
        val metrics = DisplayMetrics()
        windowManager!!.defaultDisplay.getMetrics(metrics)
        if (popupY + deplacementY < 0) {
            return 0.0f
        } else if (popupY + deplacementY + popupSizeY < metrics.heightPixels) {
            return popupY + deplacementY
        } else {
            return metrics.heightPixels.toFloat() - popupSizeY
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
            view!!,
            alarmSound
        )
        notificationPopupRecyclerView?.adapter = adapterNotification
        val itemTouchHelper =
            ItemTouchHelper(SwipeToDeleteCallback(adapterNotification, alarmSound))
        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)

        val sharedAlarmNotifTonePreferences: SharedPreferences =
            getSharedPreferences("Alarm_Tone", Context.MODE_PRIVATE)
        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Tone", R.raw.sms_ring)

        if (notifications.size == 1) {
            val customSound =
                sharedAlarmNotifTonePreferences.getString("Alarm_Custom_Tone", null)
            if (customSound == null) {
                if (idSound != null) {
                    if (sound == idSound) {
                        alertNotificationTone(sound)
                    } else {
                        alertNotificationTone(idSound)
                    }
                }
            } else {
                alertCustomNotificationTone(customSound.toString())
            }
        }

        if (notifications.size == 0) {
            alarmSound?.stop()
        }

        val imgClose = view.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.VISIBLE
        imgClose.setOnClickListener {
            windowManager?.removeView(view)
            popupView = null
            alarmSound?.stop()

            val edit = popupViewSharedPreferences.edit()
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

    fun alertNotificationTone(sound: Int) {
        alarmSound?.stop()
        alarmSound = MediaPlayer.create(this, sound)
        alarmSound?.start()

        val editDuration = sharedAlarmNotifDurationPreferences?.edit()
        editDuration?.putInt(
            "Alarm_Notif_Duration",
            alarmSound!!.duration
        )
        editDuration?.apply()

        val editCanRingtone: SharedPreferences.Editor =
            sharedAlarmNotifCanRingtonePreferences!!.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()
    }

    fun alertCustomNotificationTone(customSound: String) {
        alarmSound?.stop()
        alarmSound =
            MediaPlayer.create(this, Uri.parse(customSound))
        alarmSound!!.start()

        val editDuration: SharedPreferences.Editor = sharedAlarmNotifDurationPreferences!!.edit()
        editDuration.putInt(
            "Alarm_Notif_Duration",
            alarmSound!!.duration
        )
        editDuration.apply()

        val editCanRingtone: SharedPreferences.Editor =
            sharedAlarmNotifCanRingtonePreferences!!.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()

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

        @SuppressLint("StaticFieldLeak")
        var adapterNotification: NotifPopupRecyclerViewAdapter? = null
    }
}