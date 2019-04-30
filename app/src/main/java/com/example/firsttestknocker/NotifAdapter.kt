package com.example.firsttestknocker


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.load.engine.Resource

import java.io.IOException
import java.lang.reflect.Type
import java.util.ArrayList

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

class NotifAdapter(private val context: Context, private val notifications: ArrayList<StatusBarParcelable>, private val windowManager: WindowManager, private val view: View) : BaseAdapter() {
    private val TAG = NotificationListener::class.java.simpleName
    private val notification_listener_ContactsDatabase: ContactsRoomDatabase? = null
    private val notification_listener_mDbWorkerThread: DbWorkerThread? = null
    private val FACEBOOK_PACKAGE = "com.facebook.katana"
    private val MESSENGER_PACKAGE = "com.facebook.orca"
    private val WATHSAPP_SERVICE = "com.whatsapp"
    private val GMAIL_PACKAGE = "com.google.android.gm"
    private val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    override fun getCount(): Int {
        return notifications.size
    }

    override fun getItem(position: Int): StatusBarParcelable {
        return notifications[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_notification_adapter, parent, false)
        }
        val sbp = getItem(position)
        val app = convertView!!.findViewById<View>(R.id.notification_plateformeTv) as TextView
        val contenue = convertView.findViewById<View>(R.id.notification_contenu) as TextView
        val appImg = convertView.findViewById<View>(R.id.notification_plateformeImg) as ImageView
        val expImg = convertView.findViewById<View>(R.id.notification_expediteurImg) as ImageView
        val buttonResponse = convertView.findViewById<View>(R.id.notification_adapter_button_response) as Button
        val buttonSend = convertView.findViewById<View>(R.id.notification_adapter_button_send) as Button
        val editText = convertView.findViewById<View>(R.id.notification_adapter_editText) as EditText

        app.text = convertPackageToString(sbp.appNotifier)
        contenue.text = sbp.statusBarNotificationInfo["android.title"].toString() + ":" + sbp.statusBarNotificationInfo["android.text"]
        //appImg.setImageResource(getApplicationNotifier(sbp));

        val pckg = sbp.appNotifier
        if (sbp.statusBarNotificationInfo["android.icon"] != null) {
            val iconID = Integer.parseInt(sbp.statusBarNotificationInfo["android.icon"]!!.toString())
        }
        try {
            val pckManager = context.packageManager
            val icon = pckManager.getApplicationIcon(sbp.appNotifier)
            appImg.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (sbp.statusBarNotificationInfo["android.largeIcon"] != "") {//image de l'expediteur provenant l'application source
            println("bitmap :" + sbp.statusBarNotificationInfo["android.largeIcon"]!!)
            val bitmap = sbp.statusBarNotificationInfo["android.largeIcon"] as Bitmap?
            expImg.setImageBitmap(bitmap)
        }
        val listener = View.OnClickListener { v ->
            println("click on constraint layout")
            val app = convertPackageToString(sbp.appNotifier)
            if (v.id == R.id.notification_adapter_button_response) {
                println("click on button response")
                buttonResponse.visibility = View.GONE
                buttonSend.visibility = View.VISIBLE
                editText.visibility = View.VISIBLE
                editText.requestFocus()
                val inputMM = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMM.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            } else if (v.id == R.id.notification_adapter_button_send) {
                println("click on button send")
                val message = editText.text.toString()
                // String number= ContactInfo.
                val number= ContactInfo.getInfoWithName(sbp.statusBarNotificationInfo["android.title"].toString(),app)
                println("message :"+message + "sendto : "+ number)
                closeNotification()

            } else {

                if (app == "Facebook") {
                    gotToFacebookPage("")
                    closeNotification()
                } else if (app == "Messenger") {
                    gotToFacebookPage("")
                    closeNotification()
                } else if (app == "WhatsApp") {
                    onWhatsappClick()
                    closeNotification()
                } else if (app == "gmail") {
                    openGmail()
                    closeNotification()
                } else if (app == "message") {
                    openSms()
                    closeNotification()
                }
            }
        }

        contenue.setOnClickListener(listener)
        app.setOnClickListener(listener)
        app.setOnClickListener(listener)
        buttonResponse.setOnClickListener(listener)
        buttonSend.setOnClickListener(listener)
        return convertView
    }

    private fun closeNotification() {
        windowManager.removeView(view)
        val sharedPreferences = context.getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putBoolean("view", false)
        edit.commit()
    }

    private fun convertPackageToString(packageName: String): String {
        if (packageName == FACEBOOK_PACKAGE) {
            return "Facebook"
        } else if (packageName == MESSENGER_PACKAGE) {
            return "Messenger"
        } else if (packageName == WATHSAPP_SERVICE) {
            return "WhatsApp"
        } else if (packageName == GMAIL_PACKAGE) {
            return "gmail"
        } else if (packageName == MESSAGE_PACKAGE) {
            return "message"
        }
        return ""
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


    /////****** code dupliqué faire attention trouvé un moyen de ne plus en avoir *******//////
    private fun gotToFacebookPage(id: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$id"))
            context.startActivity(intent)
        }

    }


    private fun onWhatsappClick() {
        //String url = "https://api.whatsapp.com/";
        // try{
        //PackageManager pm = context.getPackageManager();
        //pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
        val i = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
        i!!.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
        /*} catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/

    }

    private fun openGmail() {
        val i = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
        i!!.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    private fun openSms() {
        val i = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.messaging")
        i!!.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

}

