package com.yellowtwigs.knockin.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.DbWorkerThread
import com.yellowtwigs.knockin.model.StatusBarParcelable
import java.util.*

/**
 * La Classe qui permet d'afficher les notifications prioritaires au milieu de l'écran
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class NotifPopupRecyclerViewAdapter(private val context: Context, private val notifications: ArrayList<StatusBarParcelable>, private val windowManager: WindowManager, private val view: View, private val notification_alarm_NotificationMessagesAlarmSound: MediaPlayer?
) : RecyclerView.Adapter<NotifPopupRecyclerViewAdapter.NotificationHistoryViewHolder>() {

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

    private var thisParent: ViewGroup? = null

    private var lastChanged: Long = 0
    private var lastChangedPosition = 0
    private var newMessage: Boolean = false
    private val listOftext: MutableList<String> = mutableListOf()

//    private var mRecentlyDeletedItem = StatusBarParcelable
//    private var mRecentlyDeletedItemPosition

    override fun getItemCount(): Int {
        return notifications.size
    }

//    override fun getItem(position: Int): StatusBarParcelable {
//        return notifications[position]
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHistoryViewHolder {
        thisParent = parent
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_adapter, parent, false)
        return NotificationHistoryViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItem(position: Int): StatusBarParcelable {
        return notifications[position]
    }

    fun getlastChangePos(): Int {
        return lastChangedPosition
    }

    fun getlastChangeMillis(): Long {
        return lastChanged
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NotificationHistoryViewHolder, position: Int) {
        notification_adapter_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        notification_adapter_mDbWorkerThread.start()

        if (listOftext.isEmpty()) {
            for (i in 1..notifications.size) {
                listOftext.add("")
            }
        } else if (listOftext.size != notifications.size) {
            listOftext.add(0, "")
        }

        val sbp = getItem(position)

        val gestionnaireContacts = ContactManager(context)
        val contact = gestionnaireContacts.getContact(sbp.statusBarNotificationInfo["android.title"].toString())

        val unwrappedDrawable = AppCompatResources.getDrawable(context, R.drawable.item_notif_adapter_top_bar)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)

        holder.appPlateform!!.text = convertPackageToString(sbp.appNotifier!!)

        holder.messageToSendEditText!!.setText(listOftext[position])
        System.out.println("text content" + listOftext[position] + " message" + sbp.statusBarNotificationInfo["android.text"])

        when (convertPackageToString(sbp.appNotifier!!)) {
            "Gmail" -> {
                holder.callButton!!.visibility = View.INVISIBLE
            }
            "Messenger" -> {
                holder.callButton!!.visibility = View.INVISIBLE
                holder.buttonSend!!.visibility = View.INVISIBLE
                holder.messageToSendEditText!!.visibility = View.INVISIBLE
            }
        }

        if (newMessage && System.currentTimeMillis() - NotificationListener.adapterNotification!!.getlastChangeMillis() <= 10000) {
            println("last text changed")
            (thisParent!! as RecyclerView).post {
                val thisParentRecyclerView = thisParent as RecyclerView
                thisParentRecyclerView.requestFocusFromTouch()
                thisParentRecyclerView.scrollToPosition(lastChangedPosition + 1)
                thisParentRecyclerView.requestFocus()
            }
            holder.messageToSendEditText!!.isFocusable = true
            newMessage = false
        } else if (newMessage && System.currentTimeMillis() - NotificationListener.adapterNotification!!.getlastChangeMillis() > 10000) {
            (thisParent!! as RecyclerView).post {
                val thisParentRecyclerView = thisParent as RecyclerView
                thisParentRecyclerView.requestFocusFromTouch()
                thisParentRecyclerView.scrollToPosition(0)
                thisParentRecyclerView.requestFocus()
            }
            newMessage = false
        }

        if (holder.appPlateform!!.text == "WhatsApp" || holder.appPlateform!!.text == "Message") {
            holder.callButton!!.visibility = View.VISIBLE
        }

        holder.notifContent!!.text = sbp.statusBarNotificationInfo["android.title"].toString() + ":" + sbp.statusBarNotificationInfo["android.text"]
        //appImg.setImageResource(getApplicationNotifier(sbp));

        holder.goToAppButton!!.setOnClickListener {
            closeNotificationPopup()
            when (holder.appPlateform!!.text) {
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
                    contact!!.getMessengerID()
                    if (contact.getMessengerID() != "") {
                        openMessenger(contact.getMessengerID())
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/"))
                        intent.flags = FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    closeNotificationPopup()
                }
                "WhatsApp" -> {
                    openWhatsapp(contact!!.getFirstPhoneNumber())
                }
                "Gmail" -> {
                    val appIntent = Intent(Intent.ACTION_VIEW)
                    appIntent.flags = FLAG_ACTIVITY_NEW_TASK
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
                    if (contact != null) {
                        openSms(contact.getFirstPhoneNumber(), "")
                    } else {
                        openSms(sbp.statusBarNotificationInfo["android.title"].toString(), "")
                    }
                    closeNotificationPopup()
                }
            }
        }

        holder.callButton!!.setOnClickListener {
            when (holder.appPlateform!!.text) {
                "WhatsApp" -> {
                    phoneCall(contact!!.getFirstPhoneNumber())
                    closeNotificationPopup()
                }
                "Message" -> {
                    if (contact != null) {
                        phoneCall(contact.getFirstPhoneNumber())
                    } else {
                        phoneCall(sbp.statusBarNotificationInfo["android.title"].toString())
                    }
                    closeNotificationPopup()
                }
            }
        }

        when (convertPackageToString(sbp.appNotifier!!)) {
            "Facebook" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_facebook, null))
            }
            "Messenger" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_messenger, null))
            }
            "WhatsApp" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_whatsapp, null))
            }
            "Gmail" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.custom_shape_top_bar_notif_adapter_gmail, null))
            }
            "Message" -> {
                DrawableCompat.setTint(wrappedDrawable, context.resources.getColor(R.color.colorPrimary, null))
            }
        }

        holder.buttonSend!!.setOnClickListener {
            if (holder.messageToSendEditText!!.text.toString() == "") {
                Toast.makeText(context, R.string.notif_adapter, Toast.LENGTH_SHORT).show()
            } else {

                when (convertPackageToString(sbp.appNotifier!!)) {
                    "WhatsApp" -> {
                        sendMessageWithWhatsapp(contact!!.getFirstPhoneNumber(), holder.messageToSendEditText!!.text.toString())
                        closeNotificationPopup()
                    }
                    "Gmail" -> {
                        closeNotificationPopup()

                        if (contact != null) {
                            sendMail(contact.getFirstMail(), sbp.statusBarNotificationInfo["android.text"].toString(), holder.messageToSendEditText!!.text.toString())
                        } else {
                            sendMail("", sbp.statusBarNotificationInfo["android.text"].toString(), holder.messageToSendEditText!!.text.toString())
                        }
                    }
                    "Message" -> {
//                        if (checkPermission(Manifest.permission.SEND_SMS)) {
                        if (contact != null) {
                            sendMessageWithAndroidMessage(contact.getFirstPhoneNumber(), holder.messageToSendEditText!!.text.toString())
                        } else {
                            sendMessageWithAndroidMessage(sbp.statusBarNotificationInfo["android.title"].toString(), holder.messageToSendEditText!!.text.toString())
                        }
                        closeNotificationPopup()
//                        } else {
//                            //TODO In english
//                            Toast.makeText(context, "Vous n'avez pas autorisé l'envoi de SMS via Knockin", Toast.LENGTH_LONG).show()
//
//                            if (contact != null) {
//                                openSms(contact.getFirstPhoneNumber(), holder.messageToSendEditText!!.text.toString())
//                            } else {
//                                openSms(sbp.statusBarNotificationInfo["android.title"].toString(), holder.messageToSendEditText!!.text.toString())
//                            }
//                        }
                    }
                }
                holder.messageToSendEditText!!.setText("")
                if (notifications.size > 1) {
                    notifications.removeAt(position)
                } else {
                    closeNotificationPopup()
                }
                notifyDataSetChanged()
            }

        }
        holder.messageToSendEditText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lastChanged = System.currentTimeMillis()
                lastChangedPosition = position
                listOftext.removeAt(position)
                listOftext.add(position, holder.messageToSendEditText!!.text.toString())
                println("text change at$lastChanged at position $position")
            }

        })
        /* if (sbp.statusBarNotificationInfo["android.icon"] != null) {
              val iconID = Integer.parseInt(sbp.statusBarNotificationInfo["android.icon"]!!.toString())
          }*/
        try {
            val pckManager = context.packageManager
            val icon = pckManager.getApplicationIcon(sbp.appNotifier!!)
            holder.appImageView!!.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (sbp.statusBarNotificationInfo["android.largeIcon"] != "" && sbp.statusBarNotificationInfo["android.largeIcon"] != null) {//image de l'expediteur provenant l'application source
            println("bitmap :" + sbp.statusBarNotificationInfo["android.largeIcon"]!!)
            val bitmap = sbp.statusBarNotificationInfo["android.largeIcon"] as Bitmap?
            holder.senderImageView!!.setImageBitmap(bitmap)
        }
    }

    //TODO Ask for the permission before call
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
            Toast.makeText(context, R.string.cockpit_toast_phone_number_empty, Toast.LENGTH_SHORT).show()
        }
    }

    //region ========================================== Functions ===========================================

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

        context.startActivity(intent.setFlags(FLAG_ACTIVITY_NEW_TASK))
    }

    private fun sendMessageWithAndroidMessage(phoneNumber: String, msg: String) {
        val message = "smsto:" + phoneNumber
        val i = Intent(Intent.ACTION_SENDTO, Uri.parse(message))
        i.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i.putExtra("sms_body", msg))

//        val smsManager = SmsManager.getDefault()
//        smsManager.sendTextMessage(phoneNumber, null, msg, null, null)
//
//        Toast.makeText(context, R.string.notif_adapter_message_sent,
//                Toast.LENGTH_LONG).show()
    }

    private fun sendMail(mail: String, subject: String, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        val mailSubject = "RE: $subject"
        intent.putExtra(Intent.EXTRA_EMAIL, mail)
        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        intent.putExtra(Intent.EXTRA_SUBJECT, mailSubject)
        intent.flags = FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }

    private fun openMessenger(messengerId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$messengerId"))
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
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

    fun deleteItem(position: Int) {
        val mRecentlyDeletedItem = notifications[position]
        notifications.remove(mRecentlyDeletedItem)
        notifyItemRemoved(position)

        if (notifications.size == 0) {
            closeNotificationPopup()
        }
    }

    private fun closeNotificationPopup() {

        windowManager.removeView(view)

        notification_alarm_NotificationMessagesAlarmSound?.stop()
        val sharedPreferences = context.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
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
        } else if (packageName == MESSAGE_PACKAGE || packageName == MESSAGE_SAMSUNG_PACKAGE) {
            return "Message"
        }
        return ""
    }

    class NotificationHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var appPlateform: TextView? = null
        var notifContent: TextView? = null
        var appImageView: AppCompatImageView? = null
        var senderImageView: AppCompatImageView? = null
        var buttonSend: AppCompatImageView? = null
        var messageToSendEditText: EditText? = null
        var goToAppButton: AppCompatButton? = null
        var callButton: AppCompatButton? = null

        init {
            appPlateform = view.findViewById(R.id.notification_adapter_platform)
            notifContent = view.findViewById(R.id.notification_adapter_content)
            appImageView = view.findViewById(R.id.notification_adapter_plateforme_img)
            senderImageView = view.findViewById(R.id.notification_adapter_sender_img)
            buttonSend = view.findViewById(R.id.notification_adapter_send)
            messageToSendEditText = view.findViewById(R.id.notification_adapter_message_to_send)
            goToAppButton = view.findViewById(R.id.item_notification_show_message)
            callButton = view.findViewById(R.id.item_notification_call)
        }
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
        if (!notifications.contains(sbp)) {
            notifications.add(0, sbp)
            newMessage = true
            this.notifyDataSetChanged()
        }
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
//    }//TODO : trouver une place pour toutes les méthodes des contactList

    //endregion
}