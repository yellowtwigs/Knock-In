package com.example.knocker.controller


import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap

import java.util.ArrayList

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat.checkSelfPermission
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.example.knocker.*
import com.example.knocker.model.*
import com.example.knocker.model.ModelDB.ContactDB
import androidx.core.graphics.drawable.DrawableCompat
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout


/**
 * La Classe qui permet d'afficher les notifications prioritaires au milieu de l'écran
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class NotifAdapter(private val context: Context, private val notifications: ArrayList<StatusBarParcelable>, private val windowManager: WindowManager, private val view: View) : BaseAdapter() {

    private val TAG = NotificationListener::class.java.simpleName
    private var notification_adapeter_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var notification_adapeter_mDbWorkerThread: DbWorkerThread
    private val FACEBOOK_PACKAGE = "com.facebook.katana"
    private val MESSENGER_PACKAGE = "com.facebook.orca"
    private val WHATSAPP_SERVICE = "com.whatsapp"
    private val GMAIL_PACKAGE = "com.google.android.gm"
    private val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"

    override fun getCount(): Int {
        return notifications.size
    }

    override fun getItem(position: Int): StatusBarParcelable {
        return notifications[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        notification_adapeter_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_adapeter_mDbWorkerThread.start()
        var view = convertView//valeur qui prendra les changement
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_notification_adapter, parent, false)
        }
        //System.out.println("notifications taile"+notifications.size)
        val sbp = getItem(position)
        val app = view!!.findViewById<View>(R.id.notification_adapter_platform) as TextView
        val content = view.findViewById<View>(R.id.notification_adapter_content) as TextView
        val appImg = view.findViewById<View>(R.id.notification_adapter_plateforme_img) as ImageView
        val senderImg = view.findViewById<View>(R.id.notification_adapter_sender_img) as ImageView
//        val buttonSend = convertView.findViewById<View>(R.id.notification_adapter_send) as Button
//        val editText = convertView.findViewById<View>(R.id.notification_adapter_message_to_send) as EditText

        val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.custom_shape_top_bar_notif_adapter)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)

        app.text = convertPackageToString(sbp.appNotifier)
        content.text = sbp.statusBarNotificationInfo["android.title"].toString() + ":" + sbp.statusBarNotificationInfo["android.text"]
        //appImg.setImageResource(getApplicationNotifier(sbp));

        content.setOnClickListener {
            when (app.text) {
                "Facebook" -> {
                    val uri = Uri.parse("facebook:/newsfeed")
                    val likeIng = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(likeIng)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://facebook.com/")))
                    }
                    closeNotification()
                }
                "Messenger" -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
                        context.startActivity(intent)
                    }
                    closeNotification()
                }
                "WhatsApp" -> {
                    val i = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                    try {
                        context.startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://whatsapp.com/")))
                    }
                    closeNotification()
                }
                "Gmail" -> {
                    val appIntent = Intent(Intent.ACTION_VIEW);
                    appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
                    try {
                        context.startActivity(appIntent)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://gmail.com/")))
                    }
                    closeNotification()
                }
                "Message" -> {
                    openSms(sbp)
                    closeNotification()
                }
            }
        }

        when (convertPackageToString(sbp.appNotifier)) {
            "Facebook" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_facebook))
            }
            "Messenger" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_messenger))
            }
            "WhatsApp" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_whatsapp))
            }
            "Gmail" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_gmail))
            }
            "Message" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.colorPrimary))
            }
        }

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
            senderImg.setImageBitmap(bitmap)
        }

//        val listener = View.OnClickListener { v ->
//            println("click on constraint layout")
//            val appName = convertPackageToString(sbp.appNotifier)
//            when (appName) {
//                "Facebook" -> {
//                    ContactGesture.openMessenger("", context)//TODO modifier si modification pour accès au post fb
//                    closeNotification()
//                }
//                "Messenger" -> {
//                    ContactGesture.openMessenger("", context)
//                    closeNotification()
//                }
//                "WhatsApp" -> {
//
//                    notification_adapeter_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)
//                    closeNotification()
//                }
//                "Gmail" -> {
//                    ContactGesture.openGmail(context)
//                    closeNotification()
//                }
//                "Message" -> {
//                    openSms(sbp)
//                    closeNotification()
//                }
//            }
//        }

//        content.setOnClickListener(listener)
//        app.setOnClickListener(listener)
//        app.setOnClickListener(listener)

//        buttonSend.setOnClickListener {
//            val msg = editText.text.toString()
//            val phoneNumb = compose_message_PhoneNumberEditText!!.text.toString()
//
//            if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(phoneNumb)) {
//                if (checkPermission(Manifest.permission.SEND_SMS)) {
//                    val smsManager = SmsManager.getDefault()
//                    smsManager.sendTextMessage(phoneNumb, null, msg, null, null)
//
//                    val message = Message(msg, true, "", 0, currentDate, currentHour)
//
//                    compose_message_listOfMessage.add(message)
//
//                    compose_message_ListViewMessage!!.adapter = MessageListAdapter(this, compose_message_listOfMessage)
//
//                    compose_message_MessageEditText!!.text.clear()
//                    Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@ComposeMessageActivity, "Permission denied", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this@ComposeMessageActivity, "Enter a message and a phone number", Toast.LENGTH_SHORT).show()
//            }
//        }

        return view
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0].toString() == "0") {
            val phoneNumberConvert = "+33" + phoneNumber.substring(0)
            phoneNumberConvert
        } else {
            phoneNumber
        }
    }

    fun openWhatsapp(contact: CharSequence, context: Context) {
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm = context.packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun closeNotification() {
        windowManager.removeView(view)
        val sharedPreferences = context.getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putBoolean("view", false)
        edit.apply()
    }

    private fun convertPackageToString(packageName: String): String {
        if (packageName == FACEBOOK_PACKAGE) {
            return "Facebook"
        } else if (packageName == MESSENGER_PACKAGE) {
            return "Messenger"
        } else if (packageName == WHATSAPP_SERVICE) {
            return "WhatsApp"
        } else if (packageName == GMAIL_PACKAGE) {
            return "Gmail"
        } else if (packageName == MESSAGE_PACKAGE || packageName ==


                MESSAGE_SAMSUNG_PACKAGE) {
            return "Message"
        }
        return ""
    }

    private fun canResponse(packageName: String): Boolean {
        if ((checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) && (packageName == MESSAGE_PACKAGE || packageName == WHATSAPP_SERVICE || packageName == MESSAGE_SAMSUNG_PACKAGE)) {
            return true
        }
        return false
    }

    private fun getApplicationNotifier(sbp: StatusBarParcelable): Int {

        if ((sbp.appNotifier == FACEBOOK_PACKAGE || sbp.appNotifier == MESSENGER_PACKAGE)) {
            return R.drawable.ic_facebook
        } else if (sbp.appNotifier == GMAIL_PACKAGE) {
            return R.drawable.ic_gmail
        } else if (sbp.appNotifier == WHATSAPP_SERVICE) {
            return R.drawable.ic_whatsapp_circle_menu
        }
        return R.drawable.ic_sms
    }


    /////****** code dupliqué faire attention trouvé un moyen de ne plus en avoir *******//////


    private fun openSms(sbp: StatusBarParcelable) {
        val i: Intent
        if (sbp.appNotifier.equals(MESSAGE_PACKAGE)) {
            i = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.messaging")
        } else {
            i = context.packageManager.getLaunchIntentForPackage("com.samsung.android.messaging")
        }
        i.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    fun addNotification(sbp: StatusBarParcelable) {
        notifications.add(0, sbp)
        this.notifyDataSetChanged()
    }

    private fun getContactNameFromString(NameFromSbp: String): String {
        val pregMatchString: String = ".*\\([0-9]*\\)"
        if (NameFromSbp.matches(pregMatchString.toRegex())) {
            return NameFromSbp.substring(0, TextUtils.lastIndexOf(NameFromSbp, '(')).dropLast(1)
        } else {
            println("pregmatch fail" + NameFromSbp)
            return NameFromSbp
        }
    }

    fun getContact(name: String, listContact: List<ContactDB>?): ContactDB? {

        if (name.contains(" ")) {
            listContact!!.forEach { dbContact ->

                //                println("contact "+dbContact+ "différent de name"+name)
                if (dbContact.firstName + " " + dbContact.lastName == name) {
                    return dbContact
                }
            }
        } else {
            listContact!!.forEach { dbContact ->
                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
                    return dbContact
                }
            }
        }
        return null
    }//TODO : trouver une place pour toutes les méthodes des contacts

}