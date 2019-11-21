package com.yellowtwigs.knockin.controller

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.*
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.AppCompatImageView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.NotificationAlarmActivity
import com.yellowtwigs.knockin.model.*
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB
import kotlin.collections.ArrayList
import android.util.DisplayMetrics

/**
 * Service qui nous permet de traiter les notifications
 */

@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    // Database && Thread
    private var notification_listener_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_listener_mDbWorkerThread: DbWorkerThread
    private var oldPosX: Float = 0.0f
    private var oldPosY: Float = 0.0f
    private var popupView: View? = null
    private var windowManager: WindowManager? = null
    private var listViews: ListView? = null
    /**
     * La première fois que le service est crée nous définnissons les valeurs pour les threads
     */
    override fun onCreate() {
        super.onCreate()
        // on init WorkerThread
        notification_listener_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_listener_mDbWorkerThread.start()

        //on get la base de données
        notification_listener_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
        println("lancement du service")

        //pour empêcher le listener de s'arreter
    }

    /**
     * Permet de relance le service
     */
    /*fun toggleNotificationListenerService() {
        val pm = packageManager
        val cmpName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }*/

    /**
     * Si le service à été déconnecté on demande d'être reconnecter
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        println("isDisconnect")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            println("into the rebind")
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    /**
     *Lors de la réception d'un message celle-ci effectue le traitement de la notification selon sa priorité
     * @param sbn StatusBarNotification élément de la statusbar qui vient d'être publié
     */
    @SuppressLint("WrongConstant")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val sbp = StatusBarParcelable(sbn)
        if (sharedPreferences.getBoolean("serviceNotif", false) && messagesNotUseless(sbp)) {

            sbp.castName()//permet de récupérer le vrai nom ou numéro du contact
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val message = sbp.statusBarNotificationInfo["android.text"].toString()
            val app = this.convertPackageToString(sbp.appNotifier!!)

            val gestionnaireContact = ContactManager(this)
            if (message == "Incoming voice call" || message == "Appel vocal entrant") {

            } else {
                val addNotification = Runnable {
                    var contact: ContactWithAllInformation?
                    //region Permet de Changer un numéro de téléphone en Prenom Nom
                    if (isPhoneNumber(name)) {
                        println("is a phone number")
                        contact = gestionnaireContact.getContactFromNumber(name)
                        if (contact != null)
                            sbp.changeToContactName(contact)
                    } else {
                        println("not a phone number")
                        contact = gestionnaireContact.getContactWithName(name, app)
                    }
                    //endregion
                    val notification = saveNotification(sbp, gestionnaireContact.getContactId(name))
                    if (notification != null && notificationNotDouble(notification) && sbp.appNotifier != this.packageName && sbp.appNotifier != "com.samsung.android.incallui") {
                        notification.insertNotifications(notification_listener_ContactsDatabase!!) //ajouter notification a la database

                        if (contact != null) {
                            when {
                                contact.contactDB!!.contactPriority == 2 -> {
                                    val screenListener: KeyguardManager = this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                                    if (screenListener.isKeyguardLocked) {
                                        val i = Intent(this@NotificationListener, NotificationAlarmActivity::class.java)
                                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        i.putExtra("notification", sbp)
                                        startActivity(i)
                                    } else {
                                        println("screenIsUnlocked")
                                        this.cancelNotification(sbn.key)
                                        displayLayout(sbp, sharedPreferences)
                                        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
                                        var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

                                        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", 1)
                                        when (sound) {
                                            R.raw.xylophone_tone -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.sms_ring -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.bass_slap -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.off_the_curve_groove -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.funk_yall -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.u_cant_hold_no_groove -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.cold_sweat -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.keyboard_funky_tone -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.caravan -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.moanin_jazz -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.blue_bossa -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.dolphin_dance -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.autumn_leaves -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.freddie_freeloader -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.beautiful_chords_progression -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.interstellar_main_theme -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.relax_sms -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.gravity -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.slow_dancing -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                            R.raw.scorpion_theme -> {
                                                notification_alarm_NotificationMessagesAlarmSound?.stop()
                                                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                                                notification_alarm_NotificationMessagesAlarmSound!!.start()
                                            }
                                        }
                                    }
                                }
                                contact.contactDB!!.contactPriority == 1 -> {
                                }
                                contact.contactDB!!.contactPriority == 0 -> {
                                    println("priority 0")
                                    this.cancelNotification(sbn.key)
                                }
                            }
                        } else {
                            println("I don't know this contact$contact")
                            if (sbn.packageName == MESSAGE_PACKAGE || sbn.packageName == MESSAGE_SAMSUNG_PACKAGE || sbn.packageName == XIAOMI_MESSAGE_PACKAGE) {
                                val screenListener: KeyguardManager = this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                                if (screenListener.isKeyguardLocked) {
                                    println("screenIsLocked")
                                    val i = Intent(this@NotificationListener, NotificationAlarmActivity::class.java)
                                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    i.putExtra("notification", sbp)
                                    startActivity(i)
                                } else {

                                    println("screenIsUnlocked")
                                    displayLayout(sbp, sharedPreferences)
                                    cancelNotification(sbn.key)
                                }
                            } else {
                                println("bad package " + sbn.packageName)
                            }
                        }
                    }
                }
                notification_listener_mDbWorkerThread.postTask(addNotification)
            }
        }
    }

    private fun notificationNotDouble(notification: NotificationDB): Boolean {

        val lastInsertId = notification_listener_ContactsDatabase!!.notificationsDao().lastInsert()
        println("dernière insertion $lastInsertId")
        val lastInsert = notification_listener_ContactsDatabase!!.notificationsDao().getNotification(lastInsertId)
        println("voici la liste " + notification_listener_ContactsDatabase!!.notificationsDao().lastInsertByTime(System.currentTimeMillis()))
        val listLastInsert = notification_listener_ContactsDatabase!!.notificationsDao().lastInsertByTime(System.currentTimeMillis())
        for (lastNotif in listLastInsert) {
            println("titre" + (lastNotif.title == notification.title).toString())
            println(" plateform " + (lastNotif.platform == notification.platform))
            println("description " + (lastNotif.description == notification.description))
            if (lastNotif != null && lastNotif.platform == notification.platform && lastNotif.description == notification.description) {
                return false
            }
        }
        /*if (lastInsert != null && lastInsert.platform == notification.platform && lastInsert.title == notification.title && lastInsert.description == notification.description && notification.timestamp - lastInsert.timestamp < 1000) {
            return false
        }*/
        return true
    }
    //SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(Calendar.getInstance().timeInMillis.toString().toLong()))    /// timestamp to date
    /**
     * Crée une notification qui sera sauvegardé
     *
     */
    private fun saveNotification(sbp: StatusBarParcelable, contactId: Int): NotificationDB? {
        return if (sbp.statusBarNotificationInfo["android.title"] != null && sbp.statusBarNotificationInfo["android.text"] != null) {

            NotificationDB(null,
                    sbp.tickerText.toString(),
                    sbp.statusBarNotificationInfo["android.title"]!!.toString(),
                    sbp.statusBarNotificationInfo["android.text"]!!.toString(),
                    sbp.appNotifier!!, false,
                    System.currentTimeMillis(), 0,
                    contactId)
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
        return !(sbp.statusBarNotificationInfo["android.title"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.text"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.description"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Chat heads active")//Passer ces messages dans des strings
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Messenger")
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Bulles de discussion activées"))
    }

    private fun displayLayout(sbp: StatusBarParcelable, sharedPreferences: SharedPreferences) {
        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif", false)) {
            //this.cancelNotification(sbn.key)

            if (popupView == null || !sharedPreferences.getBoolean("view", false)) {//SharedPref nous permet de savoir si l'adapter a fermé la notification
                popupView = null
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                displayLayout(sbp)
            } else {
                Log.i(TAG, "different de null" + sharedPreferences.getBoolean("view", true))
                //notifLayout(sbp, popupView)
                adapterNotification!!.addNotification(sbp)
            }
        }
    }

    private fun displayLayout(sbp: StatusBarParcelable) {
        val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        val parameters = WindowManager.LayoutParams(
                flag,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT)
        parameters.gravity = Gravity.RIGHT or Gravity.TOP
        parameters.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        popupView = inflater.inflate(R.layout.layout_notification_pop_up, null)
        val popupDropable = popupView!!.findViewById<ConstraintLayout>(R.id.notification_dropable)
        val popupContainer = popupView!!.findViewById<LinearLayout>(R.id.notification_popup_main_layout)
        notifLayout(sbp, popupView)

        windowManager!!.addView(popupView, parameters) // affichage de la popupview

        popupDropable!!.setOnTouchListener { view, event ->

            val metrics = DisplayMetrics()
            windowManager!!.defaultDisplay.getMetrics(metrics)
            when (event.action and MotionEvent.ACTION_MASK) {

                MotionEvent.ACTION_DOWN -> {
                    println("action Down")
                    oldPosX = event.x
                    oldPosY = event.y

                    println("oldx" + oldPosX + "oldy" + oldPosY)
                    //xDelta = (popupContainer.x - event.rawX).toInt()
                    //yDelta = (popupContainer.y - event.rawY).toInt()
                }

                MotionEvent.ACTION_UP -> {
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                }
                MotionEvent.ACTION_POINTER_UP -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    println("action move")
                    val x = event.x
                    val y = event.y

                    val deplacementX = x - oldPosX
                    val deplacementY = y - oldPosY

                    popupContainer.x = positionXIntoScreen(popupContainer.x, deplacementX, popupContainer.width.toFloat())//(popupContainer.x + deplacementX)
                    oldPosX = x - deplacementX

                    popupContainer.y = positionYIntoScreen(popupContainer.y, deplacementY, popupContainer.height.toFloat())//(popupContainer.y + deplacementY)
                    oldPosY = y - deplacementY
                }
            }
            return@setOnTouchListener true
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

    private fun notifLayout(sbp: StatusBarParcelable, view: View?) {

        val notifications: ArrayList<StatusBarParcelable> = ArrayList()
        notifications.add(sbp)
        adapterNotification = NotifAdapter(applicationContext, notifications, windowManager!!, view!!)
        listViews = view.findViewById(R.id.notification_pop_up_listView)
        listViews?.adapter = adapterNotification


        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
        var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", 1)

        notification_alarm_NotificationMessagesAlarmSound?.stop()
        when (sound) {
            R.raw.xylophone_tone -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.sms_ring -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.bass_slap -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.off_the_curve_groove -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.funk_yall -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.u_cant_hold_no_groove -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.cold_sweat -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.keyboard_funky_tone -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.caravan -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.moanin_jazz -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.blue_bossa -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.dolphin_dance -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.autumn_leaves -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.freddie_freeloader -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.beautiful_chords_progression -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.interstellar_main_theme -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.relax_sms -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.gravity -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.slow_dancing -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.scorpion_theme -> {
                notification_alarm_NotificationMessagesAlarmSound?.stop()
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
        }

        val imgClose = view.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.VISIBLE
        imgClose.setOnClickListener {
            //System.exit(0)
            windowManager?.removeView(view)
            popupView = null
            //effacer le window manager en rendre popup-view null pour lui réaffecter de nouvelle valeur

            notification_alarm_NotificationMessagesAlarmSound?.stop()
        }
    }//TODO:améliorer l'algorithmie


    private fun appNotifiable(sbp: StatusBarParcelable): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "Bulles de discussion activées" &&
                convertPackageToString(sbp.appNotifier!!) != ""
    }

    private fun convertPackageToString(packageName: String): String {
        if (packageName == FACEBOOK_PACKAGE) {
            return "Facebook"
        } else if (packageName == MESSENGER_PACKAGE) {
            return "Messenger"
        } else if (packageName == WHATSAPP_SERVICE) {
            return "WhatsApp"
        } else if (packageName == GMAIL_PACKAGE) {
            return "gmail"
        } else if (packageName == OUTLOOK_PACKAGE) {
            return "Outlook"
        } else if (packageName == MESSAGE_PACKAGE || packageName == MESSAGE_SAMSUNG_PACKAGE || packageName == XIAOMI_MESSAGE_PACKAGE) {
            return "message"
        }
        return ""
    }

    private fun isPhoneNumber(title: String): Boolean {
        println("name equals :" + title)
        val pregMatchString = "((?:\\+|00)[17](?: |\\-)?|(?:\\+|00)[1-9]\\d{0,2}(?: |\\-)?|(?:\\+|00)1\\-\\d{3}(?: |\\-)?)?(0\\d|\\([0-9]{3}\\)|[1-9]{0,3})(?:((?: |\\-)[0-9]{2}){4}|((?:[0-9]{2}){4})|((?: |\\-)[0-9]{3}(?: |\\-)[0-9]{4})|([0-9]{7}))"
        return title.matches(pregMatchString.toRegex())
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

        // var listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()
        @SuppressLint("StaticFieldLeak")
        var adapterNotification: NotifAdapter? = null
    }
}