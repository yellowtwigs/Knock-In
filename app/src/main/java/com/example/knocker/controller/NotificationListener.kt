package com.example.knocker.controller

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.widget.AppCompatImageView
import com.example.knocker.R
import com.example.knocker.controller.activity.NotificationAlarmActivity
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.NotificationDB
import java.util.*
import kotlin.collections.ArrayList
import android.util.DisplayMetrics
import com.example.knocker.controller.activity.NotificationHistoryActivity

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
    private var listViews:ListView?= null
    /**
     * La première fois que le service est crée nous définnissons les valeurs pour les threads
     */
    override fun onCreate() {
        super.onCreate()

        println("on create")
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
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val sbp = StatusBarParcelable(sbn)
        if (sharedPreferences.getBoolean("serviceNotif", false) && messagesNotUseless(sbp)) {
            sbp.castName()//permet de récupérer le vrai nom ou numéro du contact
            val name = sbp.statusBarNotificationInfo["android.title"].toString()
            val app = this.convertPackageToString(sbp.appNotifier!!)

            val gestionnaireContact = ContactList(this)
            val addNotification = Runnable {
                val notification = saveNotfication(sbp,
                        gestionnaireContact.getContactId(name))
                val contact: ContactWithAllInformation?

                if (notification != null && notificationNotDouble(notification) && sbp.appNotifier != this.packageName && sbp.appNotifier != "com.samsung.android.incallui") {
                    if (notification.platform != this.packageName) {
                        notification.insert(notification_listener_ContactsDatabase!!)//ajouter notification a la database

                    }
                    if (isPhoneNumber(name)) {
                        println("is a phone number")
                        contact = gestionnaireContact.getContactFromNumber(name)
                        if (contact != null)
                            sbp.changeToContactName(contact)
                    } else {
                        println("not a phone number")
                        contact = gestionnaireContact.getContactWithName(name, app)
                    }
                    if (contact != null) {

                        println("I know this contact$contact")
                        when {
                            contact.contactDB!!.contactPriority == 2 -> {
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
                            }
                            contact.contactDB!!.contactPriority == 1 -> {
                                if (sharedPreferences.getBoolean("mask_prio_1", false)) {
                                    this.cancelNotification(sbn.key)
                                }
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

    private fun notificationNotDouble(notification: NotificationDB): Boolean {

        val lastInsertId = notification_listener_ContactsDatabase!!.notificationsDao().lastInsert()
        println("dernière insertion $lastInsertId")
        val lastInsert = notification_listener_ContactsDatabase!!.notificationsDao().getNotification(lastInsertId)
        println("voici la liste "+notification_listener_ContactsDatabase!!.notificationsDao().lastInsertByTime(System.currentTimeMillis()))
        val listLastInsert = notification_listener_ContactsDatabase!!.notificationsDao().lastInsertByTime(System.currentTimeMillis())
        for(lastNotif in listLastInsert){
            println("titre"+(lastNotif.title.equals(notification.title)).toString())
            println(" plateform "+lastNotif.platform.equals(notification.platform))
            println("description "+lastNotif.description.equals(notification.description))
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
    private fun saveNotfication(sbp: StatusBarParcelable, contactId: Int): NotificationDB? {
        if (sbp.statusBarNotificationInfo["android.title"] != null && sbp.statusBarNotificationInfo["android.text"] != null) {

            return NotificationDB(null,
                    sbp.tickerText.toString(),
                    sbp.statusBarNotificationInfo["android.title"]!!.toString(),
                    sbp.statusBarNotificationInfo["android.text"]!!.toString(),
                    sbp.appNotifier!!, false,
                    System.currentTimeMillis(), 0,
                    contactId)
        } else {
            return null
        }
    }

    private fun messagesNotUseless(sbp: StatusBarParcelable): Boolean {
        val pregMatchString = resources.getString(R.string.new_messages)
        return !(sbp.statusBarNotificationInfo["android.title"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.text"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or sbp.statusBarNotificationInfo["android.description"].toString().toLowerCase().contains(pregMatchString.toLowerCase())
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Chat heads active")
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Messenger")
                or (sbp.statusBarNotificationInfo["android.title"].toString() == "Bulles de discussion activées"))
    }

    private fun displayLayout(sbp: StatusBarParcelable, sharedPreferences: SharedPreferences) {
        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif", false)) {
            //this.cancelNotification(sbn.key)

            if (popupView == null || !sharedPreferences.getBoolean("view", false)) {//si nous avons déjà afficher nous ne rentrons pas ici.
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
        listViews = view.findViewById<ListView>(R.id.notification_pop_up_listView)
        listViews?.adapter = adapterNotification
        val imgClose = view.findViewById<View>(R.id.notification_popup_close) as AppCompatImageView
        imgClose.visibility = View.VISIBLE
        imgClose.setOnClickListener {
            //System.exit(0)
            windowManager?.removeView(view)
            popupView = null
            //effacer le window manager en rendre popup-view null pour lui réaffecter de nouvelle valeur
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
        const val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
        const val XIAOMI_MESSAGE_PACKAGE = "com.android.mms"
        const val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"
        const val TELEGRAM_PACKAGE = "org.telegram.messenger"

        // var listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()
        @SuppressLint("StaticFieldLeak")
        var adapterNotification: NotifAdapter? = null
    }
}