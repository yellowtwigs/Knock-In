package com.example.firsttestknocker


import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
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
import android.widget.TextView


@SuppressLint("OverrideAbstract")
class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val intent = Intent("com.example.testnotifiacation.notificationExemple")
        val sbp = StatusBarParcelable(sbn)
        intent.putExtra("statusBar", sbp)
        sendBroadcast(intent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        this.cancelNotification(sbn.key)
        if (appNotifiable(sbp)) {
            Log.i(TAG, "application notifier:" + sbn.packageName)
            Log.i(TAG, "tickerText:" + sbn.notification.tickerText)

            for (key in sbn.notification.extras.keySet()) {
                Log.i(TAG, key + "=" + sbn.notification.extras.get(key))
            }
            if(Build.VERSION.SDK_INT>=23) {
                if (Settings.canDrawOverlays(this)) {
                    displayLayout(sbp);
                }
            }else{
                displayLayout(sbp);
            }
        }

    }
    public fun displayLayout(sbp:StatusBarParcelable){
        val parameters = WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT)
        parameters.gravity = Gravity.RIGHT or Gravity.TOP
        parameters.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_notification_pop_up, null)
        notifLayout(sbp, popupView)

        windowManager.addView(popupView, parameters)
    }
    private fun notifLayout(sbp: StatusBarParcelable, view: View) {
        val expediteur = view.findViewById<TextView>(R.id.expediteur2)
        val message = view.findViewById<View>(R.id.txtView2) as TextView
        expediteur.text = sbp.statusBarNotificationInfo["android.title"]!!.toString() + ""
        message.text = sbp.statusBarNotificationInfo["android.text"]!!.toString() + ""
        val layout = view.findViewById<View>(R.id.constraintLayout) as ConstraintLayout
        val imgPlat = view.findViewById<View>(R.id.imageView3) as ImageView
        imgPlat.setImageResource(getApplicationNotifier(sbp))
        layout.setOnClickListener { System.exit(0) }
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

    companion object {
        var TAG = NotificationListener::class.java.simpleName
        val FACEBOOK_PACKAGE = "com.facebook.katana"
        val MESSENGER_PACKAGE = "com.facebook.orca"
        val WATHSAPP_SERVICE = "com.whatsapp"
        val GMAIL_PACKAGE = "com.google.android.gm"
        val MESSAGE_PACKAGE = ""
    }
}
