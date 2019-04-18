package com.example.firsttestknocker


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
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
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import java.util.ArrayList


@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    // Database && Thread
    private var notification_listener_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_listener_mDbWorkerThread: DbWorkerThread

    var popupView : View? = null
    var windowManager: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        // on init WorkerThread
        notification_listener_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_listener_mDbWorkerThread.start()

        //on get la base de données
        notification_listener_ContactsDatabase = ContactsRoomDatabase.getDatabase(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val intent = Intent("com.example.testnotifiacation.notificationExemple")
        val sbp = StatusBarParcelable(sbn)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val addNotification = Runnable {
            notification_listener_ContactsDatabase?.notificationsDao()?.insert(saveNotfication(sbp))//retourne notfication
        }
        notification_listener_mDbWorkerThread.postTask(addNotification)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        if (appNotifiable(sbp) && sharedPreferences.getBoolean("popupNotif",false)) {
            this.cancelNotification(sbn.key)
            Log.i(TAG,"application context s"+applicationContext.toString());
            Log.i(TAG, "application notifier:" + sbp.appNotifier)
            Log.i(TAG, "tickerText:" + sbp.tickerText)
            for (key in sbn.notification.extras.keySet()) {
                Log.i(TAG, key + "=" + sbp.statusBarNotificationInfo.get(key))
            }
            if (popupView == null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        displayLayout(sbp);
                    }
                } else {
                    displayLayout(sbp);
                }
            }else{
                Log.i(TAG,"different de null")
                notifLayout(sbp,popupView)
            }
        }

    }
    public fun saveNotfication(sbp:StatusBarParcelable):Notifications{
        val notif = Notifications(null,sbp.tickerText.toString(),sbp.statusBarNotificationInfo["android.title"]!!.toString(),sbp.statusBarNotificationInfo["android.text"]!!.toString(),sbp.appNotifier,0,false);
        return notif;
    }
    public fun displayLayout(sbp:StatusBarParcelable){
        val parameters = WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
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
        val adapterNotification = NotifAdapter(applicationContext, listInverse as ArrayList<StatusBarParcelable>?)
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
      /*  val expediteur = view.findViewById<TextView>(R.id.expediteur2)
        val message = view.findViewById<View>(R.id.txtView2) as TextView
        val expTxt=sbp.statusBarNotificationInfo["android.title"]
        expediteur.text = "$expTxt"
        val msgTxt= sbp.statusBarNotificationInfo["android.text"]
        message.text ="$msgTxt"
        val layout = view.findViewById<View>(R.id.constraintLayout) as ConstraintLayout
        val imgPlat = view.findViewById<View>(R.id.imageView3) as ImageView
        imgPlat.setImageResource(getApplicationNotifier(sbp))
        layout.setOnClickListener { System.exit(0) }*/

    }


    fun appNotifiable(sbp: StatusBarParcelable): Boolean {
        return sbp.statusBarNotificationInfo["android.title"] != "Chat heads active" &&
                sbp.statusBarNotificationInfo["android.title"] != "Messenger" &&
                sbp.statusBarNotificationInfo["android.title"] != "android" &&
                sbp.appNotifier != "com.google.android.gms" &&
                sbp.appNotifier != "com.android.providers.downloads" &&
                sbp.appNotifier != "com.samsung.android.da.daagent" &&
                sbp.appNotifier != "com.android.vending" &&
                sbp.statusBarNotificationInfo["android.text"] != null &&
                sbp.appNotifier != "android"
    }

    private fun getApplicationNotifier(sbp: StatusBarParcelable): Int {

        if (sbp.appNotifier == FACEBOOK_PACKAGE || sbp.appNotifier == MESSENGER_PACKAGE) {
            return R.drawable.facebook
        } else if (sbp.appNotifier == GMAIL_PACKAGE) {
            return R.drawable.gmail
        } else if (sbp.appNotifier == WATHSAPP_SERVICE) {
            return R.drawable.download
        }
        return R.drawable.sms
    }
    private fun convertPackageToString(packageName:String):String{
        if(packageName.equals(FACEBOOK_PACKAGE)){
            return "Facebook";
        }else if(packageName.equals(MESSENGER_PACKAGE)){
            return "Messenger";
        }else if(packageName.equals(WATHSAPP_SERVICE)){
            return "WhatsApp"
        }else if(packageName.equals(GMAIL_PACKAGE)){
            return "gmail"
        }else if(packageName.equals(MESSAGE_PACKAGE)){
            return "message"
        }
        return ""
    }
    companion object {
        var TAG = NotificationListener::class.java.simpleName
        val FACEBOOK_PACKAGE = "com.facebook.katana"
        val MESSENGER_PACKAGE = "com.facebook.orca"
        val WATHSAPP_SERVICE = "com.whatsapp"
        val GMAIL_PACKAGE = "com.google.android.gm"
        val MESSAGE_PACKAGE = ""
        val listNotif: MutableList<StatusBarParcelable> = mutableListOf<StatusBarParcelable>()

    }
}
