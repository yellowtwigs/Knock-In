package com.example.firsttestknocker


import android.annotation.SuppressLint
import android.app.*
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    // Database && Thread
    private var notification_listener_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_listener_mDbWorkerThread: DbWorkerThread

    var popupView : View? = null
    var windowManager: WindowManager? = null
    var priority: Int?=null

    override fun onCreate() {
        super.onCreate()

        println("on create")
        // on init WorkerThread
        notification_listener_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_listener_mDbWorkerThread.start()

        //on get la base de données
        notification_listener_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
    }
/*
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("start command")
        return Service.START_STICKY //dire qu'on doit relancer quand il se stop
    }
*///trouver une solution au probleme de notification stop when screen is lock


    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val sbp = StatusBarParcelable(sbn)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.i(TAG,"application context "+applicationContext.toString());
        Log.i(TAG, "application notifier:" + sbp.appNotifier)
        val i = Log.i(TAG, "tickerText:" + sbp.tickerText)
        //sbn.notification.smallIcon

        for (key in sbn.notification.extras.keySet()) {
            Log.i(TAG, key + "=" + sbp.statusBarNotificationInfo.get(key))
        }
        val addNotification = Runnable {
            notification_listener_ContactsDatabase?.notificationsDao()?.insert(saveNotfication(sbp))//retourne notfication
            val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)


            val prioriteContact =ContactsPriority.getPriorityWithName(sbp.statusBarNotificationInfo.get("android.title").toString(),this.convertPackageToString(sbp.appNotifier),notification_listener_ContactsDatabase?.contactsDao()?.getAllContacts())
            println("priorité "+ prioriteContact)
            if(prioriteContact == 2){
                if (appNotifiable(sbp)&& sharedPreferences.getBoolean("popupNotif",false) ) {
                   //this.cancelNotification(sbn.key)

                    if (popupView == null || !sharedPreferences.getBoolean("view",false)) {//si nous avons déjà afficher nous ne rentrons pas ici.
                        popupView=null
                        listNotif.clear();
                        val edit: SharedPreferences.Editor = sharedPreferences.edit()
                        edit.putBoolean("view", true)
                        edit.commit()
                        displayLayout(sbp)
                    }else{
                        Log.i(TAG,"different de null"+  sharedPreferences.getBoolean("view",true) )
                        notifLayout(sbp,popupView)
                    }
                }
            }else if (prioriteContact == 1){

            }else if(prioriteContact == 0){
                println("priority 0")
               //this.cancelNotification(sbn.key)
            }

        }
        notification_listener_mDbWorkerThread.postTask(addNotification)

    }
    public fun saveNotfication(sbp:StatusBarParcelable):Notifications{
        val notif = Notifications(null,sbp.tickerText.toString(),sbp.statusBarNotificationInfo["android.title"]!!.toString(),sbp.statusBarNotificationInfo["android.text"]!!.toString(),sbp.appNotifier,0,false, SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(Calendar.getInstance().timeInMillis.toString().toLong())), java.util.Calendar.getInstance().timeInMillis.toString().dropLast(3).toInt());
        return notif;
    }
    public fun displayLayout(sbp:StatusBarParcelable){
        var flag = 0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
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
        listNotif.add(sbp)
        val listInverse : MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()
        for(i in listNotif.size-1 downTo 0 ){
            listInverse.add(listNotif.get(i))
        }//affichetr du plus récent au plus ancien les notifications
        val adapterNotification = NotifAdapter(applicationContext, listInverse as ArrayList<StatusBarParcelable>?,windowManager,view)
        val listViews = view?.findViewById<ListView>(R.id.notification_pop_up_listView)
        listViews?.adapter=adapterNotification
        val layout = view?.findViewById<View>(R.id.constraintLayout) as ConstraintLayout
        layout.setOnClickListener { //System.exit(0)
            windowManager?.removeView(view)
            popupView = null
            listNotif.clear()
            listInverse.clear()
            //effacer le window manager en rendre popup-view pour lui réaffecter de nouvelle valeur
        }

    }


    fun appNotifiable(sbp: StatusBarParcelable): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "Bulles de discussion activées" &&
                convertPackageToString(sbp.appNotifier)!=""
    }

   /* private fun getApplicationNotifier(sbp: StatusBarParcelable): Int {

        if (sbp.appNotifier == FACEBOOK_PACKAGE || sbp.appNotifier == MESSENGER_PACKAGE) {
            return R.drawable.facebook
        } else if (sbp.appNotifier == GMAIL_PACKAGE) {
            return R.drawable.gmail
        } else if (sbp.appNotifier == WATHSAPP_SERVICE) {
            return R.drawable.download
        }
        return R.drawable.sms
    }*/
    private fun convertPackageToString(packageName:String):String{
        if(packageName.equals(FACEBOOK_PACKAGE)){
            return "Facebook";
        }else if(packageName.equals(MESSENGER_PACKAGE)){
            return "Messenger";
        }else if(packageName.equals(WATHSAPP_SERVICE)){
            return "WhatsApp"
        }else if(packageName.equals(GMAIL_PACKAGE)){
            return "gmail"
        }else if(packageName.equals(MESSAGE_PACKAGE)|| packageName.equals(MESSAGE_SAMSUNG_PACKAGE)){
            return "message"
        }
        return ""
    }
    private fun afficherNotif(){

    }
    private fun empecheNotif(){

    }
    companion object {
        var TAG = NotificationListener::class.java.simpleName
        val FACEBOOK_PACKAGE = "com.facebook.katana"
        val MESSENGER_PACKAGE = "com.facebook.orca"
        val WATHSAPP_SERVICE = "com.whatsapp"
        val GMAIL_PACKAGE = "com.google.android.gm"
        val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
        val MESSAGE_SAMSUNG_PACKAGE= "com.samsung.android.messaging"
        val listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()

    }
}
