package com.example.knocker.controller


import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import com.example.knocker.R
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.NotificationDB
import java.util.*
import kotlin.collections.ArrayList

/**
 * Service qui nous permet de traiter les notifications
 */

@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    // Database && Thread
    private var notification_listener_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_listener_mDbWorkerThread: DbWorkerThread

    var popupView: View? = null
    var windowManager: WindowManager? = null
    var priority: Int? = null

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
    fun toggleNotificationListenerService() {
        val pm = getPackageManager()
        val cmpName = ComponentName(this, NotificationListener::class.java)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(cmpName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

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
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("serviceNotif", true)) {
            val sbp = StatusBarParcelable(sbn)
            sbp.castName()//permet de récupérer le vrai nom ou numéro du contact
            val name= sbp.statusBarNotificationInfo.get("android.title").toString()
            val app =this.convertPackageToString(sbp.appNotifier)

            val gestionnaireContact:ContactList= ContactList(this)
            val addNotification = Runnable {
                val notification = saveNotfication(sbp,
                        gestionnaireContact.getContactId(name))
                var contact:ContactWithAllInformation?
                if (notification != null) {
                    notification.insert(notification_listener_ContactsDatabase!!)//ajouter notification a la database
                }


                if(isPhoneNumber(name)) {

                    println("is a phone number")
                    contact = gestionnaireContact.getContactFromNumber(name)
                    if (contact!=null)
                        sbp.changeToContactName(contact)
                }else{
                    println("not a phone number")
                     contact = gestionnaireContact.getContactWithName(name, app)
                }
                if(contact!=null) {

                    if (contact!!.contactDB!!.contactPriority == 2) {
                        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif", false)) {
                            //this.cancelNotification(sbn.key)

                            if (popupView == null || !sharedPreferences.getBoolean("view", false)) {//si nous avons déjà afficher nous ne rentrons pas ici.
                                popupView = null
                                val edit: SharedPreferences.Editor = sharedPreferences.edit()
                                edit.putBoolean("view", true)
                                edit.commit()
                                displayLayout(sbp)
                            } else {
                                Log.i(TAG, "different de null" + sharedPreferences.getBoolean("view", true))
                                //notifLayout(sbp, popupView)
                                adapterNotification!!.addNotification(sbp)
                            }
                        }
                    } else if (contact.contactDB!!.contactPriority == 1) {

                    } else if (contact.contactDB!!.contactPriority == 0) {
                        println("priority 0")
                        this.cancelNotification(sbn.key)
                    }
                }

            }
            notification_listener_mDbWorkerThread.postTask(addNotification)
        }
    }
//SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(Calendar.getInstance().timeInMillis.toString().toLong()))    /// timestamp to date
    /**
     * Crée une notification qui sera sauvegardé
     *
     */
    public fun saveNotfication(sbp: StatusBarParcelable, contactId: Int): NotificationDB? {
        if (sbp.statusBarNotificationInfo["android.title"] != null && sbp.statusBarNotificationInfo["android.text"].toString() != null) {
            val notif = NotificationDB(null, sbp.tickerText.toString(), sbp.statusBarNotificationInfo["android.title"]!!.toString(), sbp.statusBarNotificationInfo["android.text"]!!.toString(), sbp.appNotifier, false, Calendar.getInstance().timeInMillis.toString().toLong(), 0, contactId);
            return notif;
        } else {
            return null
        }
    }

    public fun displayLayout(sbp: StatusBarParcelable) {
        var flag : Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            flag = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
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
        notifLayout(sbp, popupView)
        windowManager!!.addView(popupView, parameters) // affichage de la popupview
    }

    private fun notifLayout(sbp: StatusBarParcelable, view: View?) {
        val notifications:ArrayList<StatusBarParcelable> = ArrayList<StatusBarParcelable>()
        notifications.add(sbp)
        adapterNotification = NotifAdapter(applicationContext, notifications, windowManager!!, view!!)
        val listViews = view.findViewById<ListView>(R.id.notification_pop_up_listView)
        listViews?.adapter = adapterNotification
        val layout = view.findViewById<View>(R.id.constraintLayout) as ConstraintLayout
        layout.setOnClickListener {
            //System.exit(0)
            windowManager?.removeView(view)
            popupView = null
            //effacer le window manager en rendre popup-view null pour lui réaffecter de nouvelle valeur
        }

    }//TODO:améliorer l'algorithmie


    fun appNotifiable(sbp: StatusBarParcelable): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "Bulles de discussion activées" &&
                convertPackageToString(sbp.appNotifier) != ""
    }

    private fun convertPackageToString(packageName: String): String {
        if (packageName.equals(FACEBOOK_PACKAGE)) {
            return "Facebook";
        } else if (packageName.equals(MESSENGER_PACKAGE)) {
            return "Messenger";
        } else if (packageName.equals(WATHSAPP_SERVICE)) {
            return "WhatsApp"
        } else if (packageName.equals(GMAIL_PACKAGE)) {
            return "gmail"
        } else if (packageName.equals(MESSAGE_PACKAGE) || packageName.equals(MESSAGE_SAMSUNG_PACKAGE)) {
            return "message"
        }
        return ""
    }
    private fun  isPhoneNumber(title:String):Boolean{
        val pregMatchString = "((\\+33|0)[0-9]{9})|(0[0-9]\\s([0-9]{2}\\s){3}[0-9]{2})"
        return title.matches(pregMatchString.toRegex())
    }

    private fun afficherNotif() {

    }

    private fun empecheNotif() {

    }

    companion object {
        var TAG = NotificationListener::class.java.simpleName
        val FACEBOOK_PACKAGE = "com.facebook.katana"
        val MESSENGER_PACKAGE = "com.facebook.orca"
        val WATHSAPP_SERVICE = "com.whatsapp"
        val GMAIL_PACKAGE = "com.google.android.gm"
        val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
        val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"
       // var listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()
        var adapterNotification: NotifAdapter? = null
    }



}
