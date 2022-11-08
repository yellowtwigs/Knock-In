package com.yellowtwigs.knockin.model.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemPopupNotificationBinding
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString

class PopupNotificationsListAdapter(
    private val cxt: Context,
    private val notifications: ArrayList<PopupNotificationViewState>,
    private val windowManager: WindowManager,
    private val view: View
) : RecyclerView.Adapter<PopupNotificationsListAdapter.ViewHolder>() {

    private lateinit var thisParent: ViewGroup
    var newMessage = false
    private val listOfText = mutableListOf<String>()

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private var numberForPermission = ""

    var isClose = false
    private var lastChanged: Long = 0
    private var lastChangedPosition = 0

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i("PopupNotifications", "Passe par là : 1")
        thisParent = parent
        val binding = ItemPopupNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItem(position: Int): PopupNotificationViewState {
        return notifications[position]
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPopupNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(
            popup: PopupNotificationViewState
        ) {

            binding.apply {
                if (listOfText.isEmpty()) {
                    for (i in 1..notifications.size) {
                        listOfText.add("")
                    }
                } else if (listOfText.size != notifications.size) {
                    listOfText.add(0, "")
                }

                val unwrappedDrawable =
                    AppCompatResources.getDrawable(cxt, R.drawable.item_notif_adapter_top_bar)
                val wrappedDrawable = unwrappedDrawable?.let { DrawableCompat.wrap(it) }

                platform.text = popup.platform

                when (convertPackageToString(popup.platform, cxt)) {
                    "Gmail" -> {
                        callButton.visibility = View.INVISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_gmail,
                                R.color.custom_shape_top_bar_gmail,
                                cxt
                            )
                        }
                    }
                    "Messenger" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_messenger,
                                R.color.custom_shape_top_bar_messenger,
                                cxt
                            )
                        }
                    }
                    "Telegram" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_telegram,
                                R.color.custom_shape_top_bar_telegram,
                                cxt
                            )
                        }
                    }
                    "Signal" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_signal,
                                R.color.custom_shape_top_bar_signal,
                                cxt
                            )
                        }
                    }
                    "Facebook" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_facebook,
                                R.color.custom_shape_top_bar_facebook,
                                cxt
                            )
                        }
                    }
                    "Message" -> {
                        callButton.visibility = View.VISIBLE
                        buttonSend.visibility = View.VISIBLE
                        messageToSend.visibility = View.VISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_sms,
                                R.color.custom_shape_top_bar_sms,
                                cxt
                            )
                        }
                    }
                }


                content.text = listOfText[position]

                if (newMessage && System.currentTimeMillis() - getLastChangeMillis() <= 10000) {
                    (thisParent as RecyclerView).post {
                        thisParent.requestFocusFromTouch()
                        (thisParent as RecyclerView).scrollToPosition(lastChangedPosition + 1)
                        thisParent.requestFocus()
                    }
                    messageToSend.isFocusable = true
                    newMessage = false
                } else if (newMessage && System.currentTimeMillis() - getLastChangeMillis() > 10000) {
                    (thisParent as RecyclerView).post {
                        thisParent.requestFocusFromTouch()
                        (thisParent as RecyclerView).scrollToPosition(0)
                        thisParent.requestFocus()
                    }
                    newMessage = false
                }

                if (platform.text == "WhatsApp" || platform.text == "Message") {
                    callButton.visibility = View.VISIBLE
                }

                content.text = "${popup.title} : ${popup.description}"

                showMessage.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()
                    closeNotificationPopup()
                    when (platform.text) {
                        "Facebook" -> {
                            val uri = Uri.parse("facebook:/newsfeed")
                            val likeIng = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                cxt.startActivity(likeIng)
                            } catch (e: ActivityNotFoundException) {
                                cxt.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://facebook.com/")
                                    )
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Messenger" -> {
//                            contact?.getMessengerID()
//                            if (contact?.getMessengerID() != "") {
//                                contact?.getMessengerID()
//                                    ?.let { it1 -> openMessenger(it1, context) }
//                            } else {
//                                val intent =
//                                    Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse("https://www.messenger.com/t/")
//                                    )
//                                intent.flags = FLAG_ACTIVITY_NEW_TASK
//                                cxt.startActivity(intent)
//                            }
                            closeNotificationPopup()
                        }
                        "WhatsApp" -> {
//                            openWhatsapp(contact!!.getFirstPhoneNumber())
                        }
                        "Gmail" -> {
                            val appIntent = Intent(Intent.ACTION_VIEW)
                            appIntent.flags = FLAG_ACTIVITY_NEW_TASK
                            appIntent.setClassName(
                                "com.google.android.gm",
                                "com.google.android.gm.ConversationListActivityGmail"
                            )
                            try {
                                cxt.startActivity(appIntent)
                            } catch (e: ActivityNotFoundException) {
                                cxt.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://gmail.com/")
                                    )
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Message" -> {
//                            if (contact != null) {
//                                openSms(contact.getFirstPhoneNumber(), "")
//                            } else {
//                                openSms(
//                                    sbp.statusBarNotificationInfo["android.title"].toString(),
//                                    ""
//                                )
//                            }
                            closeNotificationPopup()
                        }
                        "Signal" -> {
                            goToSignal(cxt)
                        }
                        "Telegram" -> {
//                            if (contact != null) {
//                                Log.i(
//                                    "openuTeleguramu",
//                                    "${contact.contactDB?.firstName} ${contact.contactDB?.lastName}"
//                                )
//                        goToTelegram(
//                            context,
//                            "${contact.contactDB?.firstName} ${contact.contactDB?.lastName}"
//                        )
//                            } else {
//                                goToTelegram(context, "")
//                            }
                        }
                    }
                }

                callButton.setOnClickListener {
//                    NotificationListener.alarmSound?.stop()
//                    when (holder.appPlatform!!.text) {
//                        "WhatsApp" -> {
//                            phoneCall(contact!!.getFirstPhoneNumber())
//                            closeNotificationPopup()
//                        }
//                        "Message" -> {
//                            if (contact != null) {
//                                phoneCall(contact.getFirstPhoneNumber())
//                            } else {
//                                phoneCall(sbp.statusBarNotificationInfo["android.title"].toString())
//                            }
//                            closeNotificationPopup()
//                        }
//                    }
                }

                buttonSend.setOnClickListener {
//                    NotificationListener.alarmSound?.stop()
//                    if (holder.messageToSendEditText!!.text.toString() == "") {
//                        Toast.makeText(context, R.string.notif_adapter, Toast.LENGTH_SHORT).show()
//                    } else {
//
//                        when (convertPackageToString(sbp.appNotifier!!, context)) {
//                            "WhatsApp" -> {
//                                contact?.let {
//                                    sendMessageWithWhatsapp(
//                                        it.getFirstPhoneNumber(),
//                                        holder.messageToSendEditText?.text.toString()
//                                    )
//                                }
//                                closeNotificationPopup()
//                            }
//                            "Gmail" -> {
//                                closeNotificationPopup()
//
//                                if (contact != null) {
//                                    sendMail(
//                                        contact.getFirstMail(),
//                                        sbp.statusBarNotificationInfo["android.text"].toString(),
//                                        holder.messageToSendEditText?.text.toString()
//                                    )
//                                } else {
//                                    sendMail(
//                                        "",
//                                        sbp.statusBarNotificationInfo["android.text"].toString(),
//                                        holder.messageToSendEditText?.text.toString()
//                                    )
//                                }
//                            }
//                            "Message" -> {
//                                if (contact != null) {
//                                    sendMessageWithAndroidMessage(
//                                        contact.getFirstPhoneNumber(),
//                                        holder.messageToSendEditText!!.text.toString()
//                                    )
//                                } else {
//                                    sendMessageWithAndroidMessage(
//                                        sbp.statusBarNotificationInfo["android.title"].toString(),
//                                        holder.messageToSendEditText!!.text.toString()
//                                    )
//                                }
//                                closeNotificationPopup()
////                        } else {
////                            //TODO In english
////                            Toast.makeText(context, "Vous n'avez pas autorisé l'envoi de SMS via Knockin", Toast.LENGTH_LONG).show()
////
////                            if (contact != null) {
////                                openSms(contact.getFirstPhoneNumber(), holder.messageToSendEditText!!.text.toString())
////                            } else {
////                                openSms(sbp.statusBarNotificationInfo["android.title"].toString(), holder.messageToSendEditText!!.text.toString())
////                            }
////                        }
//                            }
//                        }
//                        holder.messageToSendEditText?.setText("")
//                        if (notifications.size > 1) {
//                            notifications.removeAt(position)
//                        } else {
//                            closeNotificationPopup()
//                        }
//                    }
                }

                messageToSend.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        lastChanged = System.currentTimeMillis()
                        lastChangedPosition = position
                        listOfText.removeAt(position)
//                        listOfText.add(position, holder.messageToSendEditText!!.text.toString())
//                        NotificationListener.alarmSound?.stop()
                    }
                })
//                try {
//                    val pckManager = context.packageManager
//                    val icon = pckManager.getApplicationIcon(sbp.appNotifier!!)
//                    holder.appImageView!!.setImageDrawable(icon)
//                } catch (e: PackageManager.NameNotFoundException) {
//                    e.printStackTrace()
//                }
            }
        }

        private fun setupIconAndColor(
            platformImage: AppCompatImageView,
            wrappedDrawable: Drawable,
            iconId: Int,
            colorId: Int,
            cxt: Context
        ) {
            platformImage.setImageResource(iconId)
            DrawableCompat.setTint(
                wrappedDrawable,
                cxt.resources.getColor(
                    colorId,
                    null
                )
            )
        }

        private fun getLastChangePos(): Int {
            return lastChangedPosition
        }

        private fun getLastChangeMillis(): Long {
            return lastChanged
        }

        private fun closeNotificationPopup() {
            windowManager.removeView(view)
            val sharedPreferences =
                cxt.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
            val edit = sharedPreferences.edit()
            edit.putBoolean("view", false)
            edit.apply()
        }
    }

    fun addNotification(sbp: PopupNotificationViewState) {
        if (!notifications.contains(sbp)) {
            notifications.add(0, sbp)
            newMessage = true
        }
    }

    fun deleteItem(position: Int) {
//        val mRecentlyDeletedItem = notifications[position]
//        val contactsDatabase = ContactsRoomDatabase.getDatabase(context)
//        contactsDatabase?.VipNotificationsDao()
//            ?.deleteVipNotificationsWithId(notifications[position].id.toString())
//        contactsDatabase?.VipSbnDao()?.deleteSbnWithNotifId(notifications[position].id.toString())
//        notifications.remove(mRecentlyDeletedItem)
//        notifyItemRemoved(position)
//
//        NotificationListener.alarmSound?.stop()
//
//        if (notifications.size == 0) {
//            closeNotificationPopup()
//        }
    }
}