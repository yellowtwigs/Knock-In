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
import android.os.Build
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
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
import kotlin.collections.ArrayList

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
    private var notificationPopupRecyclerView: RecyclerView? = null

    private var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

    private var sharedAlarmNotifDurationPreferences: SharedPreferences? = null
    private var duration = 0

    private var sharedAlarmNotifCanRingtonePreferences: SharedPreferences? = null
    private var canRingtone = false

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
                                        this.cancelNotification(sbn.key)
                                        startActivity(i)
                                    } else {
                                        println("screenIsUnlocked")

                                        //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        this.cancelNotification(sbn.key)
                                        //}
                                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                                            var i = 0
                                            val vipNotif = VipNotificationsDB(null, sbp.id, sbp.appNotifier!!, sbp.tailleList, sbp.tickerText!!)
                                            val notifId = notification_listener_ContactsDatabase!!.VipNotificationsDao().insert(vipNotif)
                                            while(i < sbp.key.size) {
                                                if (sbp.key[i] == "android.title" || sbp.key[i] == "android.text" || sbp.key[i] == "android.largeIcon") {
                                                    val VipSbn = VipSbnDB(null, notifId!!.toInt(), sbp.key[i], sbp.statusBarNotificationInfo[sbp.key[i]].toString())
                                                    notification_listener_ContactsDatabase!!.VipSbnDao().insert(VipSbn)
                                                    println(sbp.id)
                                                    println(sbp.tailleList)
                                                    println(sbp.appNotifier)
                                                    println(sbp.key)
                                                    println(sbp.statusBarNotificationInfo)
                                                }
                                                i++
                                            }
                                            println("android 10 recois notif")
                                            //this.cancelNotification(sbn.key)
                                        }
                                        displayLayout(sbp, sharedPreferences)
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

                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        this.cancelNotification(sbn.key) /////// cancell
                                    }
                                    displayLayout(sbp, sharedPreferences)
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
        if (lastInsert != null && lastInsert.platform == notification.platform && lastInsert.title == notification.title && lastInsert.description == notification.description && notification.timestamp - lastInsert.timestamp < 1000) {
            return false
        }
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

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)

        sharedAlarmNotifDurationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        duration = sharedAlarmNotifDurationPreferences!!.getInt("Alarm_Notif_Duration", 0)

        sharedAlarmNotifCanRingtonePreferences = getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        canRingtone = sharedAlarmNotifCanRingtonePreferences!!.getBoolean("Can_RingTone", false)

        if (canRingtone) {
            alartNotifTone(sound)
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


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            sendNotificationMessage(sbp.statusBarNotificationInfo["android.title"]!!.toString(), sbp.statusBarNotificationInfo["android.text"]!!.toString(), "")
//        }

        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif", false)) {
            //this.cancelNotification(sbn.key)
            if (adapterNotification == null) {
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("view", false)
                edit.apply()
            }
            if (popupView == null || !sharedPreferences.getBoolean("view", false)) {//SharedPref nous permet de savoir si l'adapter a fermé la notification
                popupView = null
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("view", true)
                edit.apply()
                displayLayout(sbp)
            } else {
                //notifLayout(sbp, popupView)
                println(sbp)
                println(adapterNotification)
                println(popupView)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
        } else {
            println("mdrrrr")
            val sharedPreferences: SharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
            if (sharedPreferences.getBoolean("first_notif", true)) {
                val view = inflater.inflate(R.layout.layout_notification_pop_up, null)
                val notifications: ArrayList<StatusBarParcelable> = ArrayList()
                notifications.add(sbp)
                adapterNotification = NotifPopupRecyclerViewAdapter(applicationContext, notifications, windowManager!!, view, notification_alarm_NotificationMessagesAlarmSound, false)
                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                edit.putBoolean("first_notif", false)
                edit.apply()
            }
            //init DB
            //notification_listener_ContactsDatabase!!.notificationsDao().getNotification(lastInsertId)
            //add Notif VIP
            //add tout ses sbn
            displayBubble(sbp)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun displayBubble(sbp: StatusBarParcelable) {
        val notificationManager = this.getSystemService(NotificationManager::class.java)
        val icon = createIcon()
        val person = createPerson(icon)
        val channel = NotificationChannel("notifications", "vip", NotificationManager.IMPORTANCE_LOW).apply {
            description = "channel for Vip notifications"
        }
        notificationManager?.createNotificationChannel(channel)
        val notification = createNotification(icon, person, sbp)
        val bubbleMetaData = createBubbleMetadata(icon, sbp)
        notification.setBubbleMetadata(bubbleMetaData)
        startForeground(101, notification.build())
        //notificationManager?.notify(0,notification.build())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createPerson(icon: Icon): Person {
        return Person.Builder()
                .setName("Knockin")
                .setIcon(icon)
                .setBot(true)
                .setImportant(true)
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createNotification(icon: Icon, person: Person, sbp: StatusBarParcelable): Notification.Builder {
        return Notification.Builder(this, "notifications")
                .setContentTitle("KnockIn")
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.ic_app_image)
                .setCategory(Notification.CATEGORY_CALL)
                .setStyle(Notification.MessagingStyle(person)
                        .setGroupConversation(false)
                        .addMessage(sbp.tickerText.toString(), System.currentTimeMillis(), person) /// [Idée] on pourrait ajouter plusieurs messages // sbp.tickerText.toString()
                )
                .addPerson(person)
                .setShowWhen(true)
                .setContentIntent(createIntent(1, sbp))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createIcon(): Icon {
        return Icon.createWithAdaptiveBitmap(
                BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_app_image_png
                )
        )
    }

    private fun createIntent(requestCode: Int, sbp: StatusBarParcelable): PendingIntent {
        val intent = Intent(this, BubbleActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        println("c'est le string tickerText")
        println(sbp.tickerText.toString())
        //val size = adapterNotification!!.getItemCount()
        //val allNotif = adapterNotification!!.getAllNotification()
        ///setAllSbp(size, intent)
        //intent.putExtra("sizeOfArray", size)
        //intent.putExtra("com.yellowtwigs.knockin.sbp9", 17)
        /*intent.putExtra("com.yellowtwigs.knockin.sbp8", 15)
        intent.putExtra("com.yellowtwigs.knockin.sbp7", 19)
        intent.putExtra("com.yellowtwigs.knockin.sbp6", 16)
        intent.putExtra("com.yellowtwigs.knockin.sbp55", 14)*/
        val pending = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pending
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createBubbleMetadata(icon: Icon, sbp: StatusBarParcelable): Notification.BubbleMetadata {
        val requestbubble = 2
        return Notification.BubbleMetadata.Builder()
                .setDesiredHeight(600)
                .setIcon(icon)
                .apply {  }
                .setIntent(createIntent(1, sbp))
                .build()
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
        notificationPopupRecyclerView = view!!.findViewById(R.id.notification_popup_recycler_view)
        notificationPopupRecyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        adapterNotification = NotifPopupRecyclerViewAdapter(applicationContext, notifications, windowManager!!, view, notification_alarm_NotificationMessagesAlarmSound, false)
        notificationPopupRecyclerView?.adapter = adapterNotification
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapterNotification))
        itemTouchHelper.attachToRecyclerView(notificationPopupRecyclerView)

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)
        val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)

        if (notifications.size == 1) {
            alartNotifTone(sound)
        }

        if (notifications.size == 0) {
            notification_alarm_NotificationMessagesAlarmSound?.stop()
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

    private fun sendNotificationMessage(contact: String, message: String, platform: String) {
//        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        val alarmManager = getSystemService(AppCompatActivity.ALARM_SERVICE) as (AlarmManager)
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

        //Get an instance of NotificationManager//

//        val mBuilder = NotificationCompat.Builder(applicationContext)
//        mBuilder.setSmallIcon(R.drawable.ic_app_image);
//        mBuilder.setContentTitle(contact);
//        mBuilder.setContentText("$message on $platform");
//
//        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        mNotificationManager.notify(1, mBuilder.build());

        val NOTIFICATION_ID = 234;
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "my_channel_01";
            val name = "my_channel";
            val Description = "This is my channel";
            val importance = NotificationManager.IMPORTANCE_HIGH;
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.setShowBadge(false)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val mBuilder = NotificationCompat.Builder(applicationContext)
        mBuilder.setSmallIcon(R.drawable.ic_app_image)
        mBuilder.setContentTitle(contact)
        mBuilder.setContentText("$message on $platform")

        val resultIntent = Intent(applicationContext, MainActivity::class.java);
        val stackBuilder = TaskStackBuilder.create(applicationContext);
        stackBuilder.addParentStack(MainActivity::class.java);
        stackBuilder.addNextIntent(resultIntent);
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private fun isPhoneNumber(title: String): Boolean {
        println("name equals :" + title)
        val pregMatchString = "((?:\\+|00)[17](?: |\\-)?|(?:\\+|00)[1-9]\\d{0,2}(?: |\\-)?|(?:\\+|00)1\\-\\d{3}(?: |\\-)?)?(0\\d|\\([0-9]{3}\\)|[1-9]{0,3})(?:((?: |\\-)[0-9]{2}){4}|((?:[0-9]{2}){4})|((?: |\\-)[0-9]{3}(?: |\\-)[0-9]{4})|([0-9]{7}))"
        return title.matches(pregMatchString.toRegex())
    }

    fun alartNotifTone(sound: Int) {
        notification_alarm_NotificationMessagesAlarmSound?.stop()
        notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
        notification_alarm_NotificationMessagesAlarmSound!!.start()

        val editDuration: SharedPreferences.Editor = sharedAlarmNotifDurationPreferences!!.edit()
        editDuration.putInt("Alarm_Notif_Duration", notification_alarm_NotificationMessagesAlarmSound!!.duration)
        editDuration.apply()

        val editCanRingtone: SharedPreferences.Editor = sharedAlarmNotifCanRingtonePreferences!!.edit()
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

        // var listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()
        @SuppressLint("StaticFieldLeak")
        var adapterNotification: NotifPopupRecyclerViewAdapter? = null
    }
}