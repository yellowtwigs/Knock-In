package com.example.knocker.controller


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import com.example.knocker.model.ContactList
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.StatusBarParcelable
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrInterface
import java.util.*


/**
 * La Classe qui permet d'afficher les notifications prioritaires au milieu de l'écran
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class NotifAdapter(private val context: Context, private val notifications: ArrayList<StatusBarParcelable>, private val windowManager: WindowManager, private val view: View) : BaseAdapter() {

    /*private val TAG = NotificationListener::class.java.simpleName*/
    private lateinit var notification_adapter_mDbWorkerThread: DbWorkerThread
    private val FACEBOOK_PACKAGE = "com.facebook.katana"
    private val MESSENGER_PACKAGE = "com.facebook.orca"
    private val WHATSAPP_SERVICE = "com.whatsapp"
    private val GMAIL_PACKAGE = "com.google.android.gm"
    private val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    private val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1

    private var numberForPermission = ""

    private var slidr : SlidrInterface? = null

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
        notification_adapter_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_adapter_mDbWorkerThread.start()


        var view = convertView//valeur qui prendra les changement
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_notification_adapter, parent, false)
        }

        val sbp = getItem(position)

        val gestionnaireContacts = ContactList(this.context)
        val contact = gestionnaireContacts.getContact(sbp.statusBarNotificationInfo["android.title"].toString())


        val app = view!!.findViewById<View>(R.id.notification_adapter_platform) as TextView
        val content = view.findViewById<View>(R.id.notification_adapter_content) as TextView
        val appImg = view.findViewById<View>(R.id.notification_adapter_plateforme_img) as ImageView
        val senderImg = view.findViewById<View>(R.id.notification_adapter_sender_img) as ImageView
        val buttonSend = view.findViewById<View>(R.id.notification_adapter_send) as RelativeLayout
        val editText = view.findViewById<View>(R.id.notification_adapter_message_to_send) as EditText
        val showButton = view.findViewById<View>(R.id.item_notification_show_message) as AppCompatButton
        val callButton = view.findViewById<View>(R.id.item_notification_call) as AppCompatButton

//        slidr = Slidr.attach(this.context as Activity)

        val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.custom_shape_top_bar_notif_adapter)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)

        app.text = convertPackageToString(sbp.appNotifier!!)

        if (app.text == "WhatsApp" || app.text == "Message") {
            callButton.visibility = View.VISIBLE
        }

        content.text = sbp.statusBarNotificationInfo["android.title"].toString() + ":" + sbp.statusBarNotificationInfo["android.text"]
        //appImg.setImageResource(getApplicationNotifier(sbp));

        showButton.setOnClickListener {
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
                    closeNotificationPopup()
                }
                "Messenger" -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
                        context.startActivity(intent)
                    }
                    closeNotificationPopup()
                }
                "WhatsApp" -> {
                    openWhatsapp(contact!!.getFirstPhoneNumber())
                }
                "Gmail" -> {
                    val appIntent = Intent(Intent.ACTION_VIEW)
                    appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
                    try {
                        context.startActivity(appIntent)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://gmail.com/")))
                    }
                    closeNotificationPopup()
                }
                "Message" -> {
                    openSms(contact!!.getFirstPhoneNumber(), "")
                    closeNotificationPopup()
                }
            }
        }

        callButton.setOnClickListener {
            when (app.text) {
                "WhatsApp" -> {
                    phoneCall(contact!!.getFirstPhoneNumber())
                    closeNotificationPopup()
                }
                "Message" -> {
                    phoneCall(contact!!.getFirstPhoneNumber())
                    closeNotificationPopup()
                }
            }
        }

        when (convertPackageToString(sbp.appNotifier!!)) {
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

        buttonSend.setOnClickListener {
            if (editText.text.toString() == "") {
                Toast.makeText(context, R.string.notif_adapter, Toast.LENGTH_SHORT).show()
            } else {

                when (convertPackageToString(sbp.appNotifier!!)) {
                    "WhatsApp" -> {
                        sendMessageWithWhatsapp(contact!!.getFirstPhoneNumber(), editText.text.toString())
                        closeNotificationPopup()
                    }
                    "Gmail" -> {
                    }
                    "Message" -> {
                        if (checkPermission(Manifest.permission.SEND_SMS)) {
                            sendMessageWithAndroidMessage(contact!!.getFirstPhoneNumber(), editText.text.toString())
                            //closeNotificationPopup()
                        } else {
                            //TODO In english
                            Toast.makeText(context, "Vous n'avez pas autorisé l'envoi de SMS via Knocker", Toast.LENGTH_LONG).show()
                            openSms(contact!!.getFirstPhoneNumber(), editText.text.toString())
                        }
                    }
                }
                editText.setText("")
                if (notifications.size > 1) {
                    notifications.removeAt(position)
                } else {
                    closeNotificationPopup()
                }
                notifyDataSetChanged()
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

        if (sbp.statusBarNotificationInfo["android.largeIcon"] != "" && sbp.statusBarNotificationInfo["android.largeIcon"] != null) {//image de l'expediteur provenant l'application source
            println("bitmap :" + sbp.statusBarNotificationInfo["android.largeIcon"]!!)
            val bitmap = sbp.statusBarNotificationInfo["android.largeIcon"] as Bitmap?
            senderImg.setImageBitmap(bitmap)
        }
        return view
    }

    private fun phoneCall(phoneNumber: String) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_REQUEST_CODE)
                numberForPermission = phoneNumber
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                if (numberForPermission.isEmpty()) {
                    context.startActivity(intent)
                } else {
                    context.startActivity(intent)
                    numberForPermission = ""
                }
            }
        } else {
            Toast.makeText(context, R.string.phone_log_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
        }
    }

    //region ========================================== Functions ===========================================

    fun lockSlide(view: View){
        slidr!!.lock()
    }

    fun unLockSlide(view: View){
        slidr!!.unlock()
    }

    private fun openSms(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("sms_body", message)

        context.startActivity(intent)
    }

    private fun sendMessageWithWhatsapp(phoneNumber: String, msg: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message&text=$msg")

        context.startActivity(intent)
    }

    private fun openWhatsapp(phoneNumber: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message")

        context.startActivity(intent)
    }

    private fun sendMessageWithAndroidMessage(phoneNumber: String, msg: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, msg, null, null)

        Toast.makeText(context, R.string.notif_adapter_message_sent,
                Toast.LENGTH_LONG).show()
    }

    private fun checkPermission(permission: String): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(context, permission)
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0].toString() == "0") {
            val phoneNumberConvert = "+33" + phoneNumber.substring(0)
            phoneNumberConvert
        } else {
            phoneNumber
        }
    }

    private fun closeNotificationPopup() {
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

//    private fun canResponse(packageName: String): Boolean {
//        if ((checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) && (packageName == MESSAGE_PACKAGE || packageName == WHATSAPP_SERVICE || packageName == MESSAGE_SAMSUNG_PACKAGE)) {
//            return true
//        }
//        return false
//    }

    /*private fun getApplicationNotifier(sbp: StatusBarParcelable): Int {

        if ((sbp.appNotifier == FACEBOOK_PACKAGE || sbp.appNotifier == MESSENGER_PACKAGE)) {
            return R.drawable.ic_facebook
        } else if (sbp.appNotifier == GMAIL_PACKAGE) {
            return R.drawable.ic_gmail
        } else if (sbp.appNotifier == WHATSAPP_SERVICE) {
            return R.drawable.ic_whatsapp_circle_menu
        }
        return R.drawable.ic_sms_selector
    }*/


    /////****** code dupliqué faire attention trouvé un moyen de ne plus en avoir *******//////

    fun addNotification(sbp: StatusBarParcelable) {
        notifications.add(0, sbp)
        this.notifyDataSetChanged()
    }

//    private fun getContactNameFromString(NameFromSbp: String): String {
//        val pregMatchString: String = ".*\\([0-9]*\\)"
//        if (NameFromSbp.matches(pregMatchString.toRegex())) {
//            return NameFromSbp.substring(0, TextUtils.lastIndexOf(NameFromSbp, '(')).dropLast(1)
//        } else {
//            println("pregmatch fail$NameFromSbp")
//            return NameFromSbp
//        }
//    }

//    fun getContact(name: String, listContact: List<ContactDB>?): ContactDB? {
//
//        if (name.contains(" ")) {
//            listContact!!.forEach { dbContact ->
//
//                //                println("contact "+dbContact+ "différent de name"+name)
//                if (dbContact.firstName + " " + dbContact.lastName == name) {
//                    return dbContact
//                }
//            }
//        } else {
//            listContact!!.forEach { dbContact ->
//                if (dbContact.firstName == name && dbContact.lastName == "" || dbContact.firstName == "" && dbContact.lastName == name) {
//                    return dbContact
//                }
//            }
//        }
//        return null
//    }//TODO : trouver une place pour toutes les méthodes des contacts

    //endregion
}