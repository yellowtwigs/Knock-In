package com.yellowtwigs.knockin.ui.notifications

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.service.notification.NotificationListenerService
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemPopupNotificationBinding
import com.yellowtwigs.knockin.model.database.ContactsDatabase
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.model.service.NotificationsListenerService
import com.yellowtwigs.knockin.model.service.PopupNotificationViewState
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.ContactGesture.sendMessageWithAndroidMessage
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString
import com.yellowtwigs.knockin.utils.NotificationsGesture.phoneCall
import java.util.*

class PopupNotificationsListAdapter(
    private val context: Context,
    private val notifications: ArrayList<StatusBarParcelable>,
    private val windowManager: WindowManager,
    private val view: View
) : ListAdapter<PopupNotificationViewState, PopupNotificationsListAdapter.ViewHolder>(
    NotificationComparator()
) {

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private var numberForPermission = ""

    var isClose = false

    private var thisParent: ViewGroup? = null

    private var lastChanged: Long = 0
    private var lastChangedPosition = 0
    private var newMessage: Boolean = false
    private val listOfText: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        thisParent = parent
        return ViewHolder(
            ItemPopupNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun getLastChangePos(): Int {
        return lastChangedPosition
    }

    fun getLastChangeMillis(): Long {
        return lastChanged
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    //region ========================================== Functions ===========================================

    fun deleteItem(position: Int) {
//        val mRecentlyDeletedItem = notifications[position]
//        val contactsDatabase = ContactsDatabase.getDatabase(context)
//        contactsDatabase?.VipNotificationsDao()
//            ?.deleteVipNotificationsWithId(notifications[position].id.toString())
//        contactsDatabase?.VipSbnDao()?.deleteSbnWithNotifId(notifications[position].id.toString())
//        notifications.remove(mRecentlyDeletedItem)
//        notifyItemRemoved(position)
//
//        NotificationsListenerService.alarmSound?.stop()
//
//        if (notifications.size == 0) {
//            closeNotificationPopup()
//        }
    }

    private fun closeNotificationPopup() {
        windowManager.removeView(view)
        val sharedPreferences =
            context.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putBoolean("view", false)
        edit.apply()
    }

    fun addNotification(sbp: StatusBarParcelable) {
        if (!notifications.contains(sbp)) {
            notifications.add(0, sbp)
            newMessage = true
            this.notifyDataSetChanged()
        }
    }

    //endregion

    inner class ViewHolder(private val binding: ItemPopupNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(popupNotificationViewState: PopupNotificationViewState) {
//            if (listOftext.isEmpty()) {
//                for (i in 1..notifications.size) {
//                    listOftext.add("")
//                }
//            } else if (listOftext.size != notifications.size) {
//                listOftext.add(0, "")
//            }

            val unwrappedDrawable =
                AppCompatResources.getDrawable(context, R.drawable.item_notif_adapter_top_bar)
            val wrappedDrawable = unwrappedDrawable?.let { DrawableCompat.wrap(it) }

            binding.apply {
                platform.text = convertPackageToString(popupNotificationViewState.platform, context)
//                messageToSend.setText(listOftext[position])

                when (convertPackageToString(popupNotificationViewState.platform, context)) {
                    "Gmail" -> {
                        callButton.visibility = View.INVISIBLE
                    }
                    "Messenger" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE
                    }
                    "Telegram" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE
                    }
                    "Signal" -> {
                        callButton.visibility = View.INVISIBLE
                        buttonSend.visibility = View.INVISIBLE
                        messageToSend.visibility = View.INVISIBLE
                    }
                    "WhatsApp" -> {
                        callButton.visibility = View.VISIBLE
                        buttonSend.visibility = View.VISIBLE
                        messageToSend.visibility = View.VISIBLE
                    }
                    "Message" -> {
                        callButton.visibility = View.VISIBLE
                        buttonSend.visibility = View.VISIBLE
                        messageToSend.visibility = View.VISIBLE
                    }
                }

                content.text =
                    popupNotificationViewState.contactName + " : " + popupNotificationViewState.description

                when (convertPackageToString(popupNotificationViewState.platform, context)) {
                    "Facebook" -> {
                        if (wrappedDrawable != null) {
                            DrawableCompat.setTint(
                                wrappedDrawable,
                                context.resources.getColor(
                                    R.color.custom_shape_top_bar_notif_adapter_facebook,
                                    null
                                )
                            )
                        }
                    }
                    "Messenger" -> {
                        if (wrappedDrawable != null) {
                            DrawableCompat.setTint(
                                wrappedDrawable,
                                context.resources.getColor(
                                    R.color.custom_shape_top_bar_notif_adapter_messenger,
                                    null
                                )
                            )
                        }
                    }
                    "WhatsApp" -> {
                        if (wrappedDrawable != null) {
                            DrawableCompat.setTint(
                                wrappedDrawable,
                                context.resources.getColor(
                                    R.color.custom_shape_top_bar_notif_adapter_whatsapp,
                                    null
                                )
                            )
                        }
                    }
                    "Gmail" -> {
                        if (wrappedDrawable != null) {
                            DrawableCompat.setTint(
                                wrappedDrawable,
                                context.resources.getColor(
                                    R.color.custom_shape_top_bar_notif_adapter_gmail,
                                    null
                                )
                            )
                        }
                    }
                    "Message" -> {
                        if (wrappedDrawable != null) {
                            DrawableCompat.setTint(
                                wrappedDrawable,
                                context.resources.getColor(R.color.colorPrimary, null)
                            )
                        }
                    }
                }

                showMessage.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()
                    closeNotificationPopup()
                    when (convertPackageToString(popupNotificationViewState.platform, context)) {
                        "Facebook" -> {
                            val uri = Uri.parse("facebook:/newsfeed")
                            val likeIng = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(likeIng)
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://facebook.com/")
                                    )
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Messenger" -> {
                            if (popupNotificationViewState.messengerId != "" &&
                                popupNotificationViewState.messengerId.isNotEmpty() &&
                                popupNotificationViewState.messengerId.isNotBlank()
                            ) {
                                openMessenger(popupNotificationViewState.messengerId, context)
                            } else {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.messenger.com/t/")
                                    )
                                intent.flags = FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                            closeNotificationPopup()
                        }
                        "WhatsApp" -> {
                            openWhatsapp(popupNotificationViewState.phoneNumber, context)
                        }
                        "Gmail" -> {
                            val appIntent = Intent(Intent.ACTION_VIEW)
                            appIntent.flags = FLAG_ACTIVITY_NEW_TASK
                            appIntent.setClassName(
                                "com.google.android.gm",
                                "com.google.android.gm.ConversationListActivityGmail"
                            )
                            try {
                                context.startActivity(appIntent)
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://gmail.com/")
                                    )
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Message" -> {
                            if (popupNotificationViewState.phoneNumber != "" && popupNotificationViewState.phoneNumber.isNotBlank()
                                && popupNotificationViewState.phoneNumber.isNotEmpty()
                            ) {
                                openSms(
                                    popupNotificationViewState.phoneNumber,
                                    context
                                )
                            } else {
                                openSms(
                                    popupNotificationViewState.phoneNumber,
                                    context
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Signal" -> {
                            goToSignal(context)
                        }
                        "Telegram" -> {
                            goToTelegram(context, "")
                        }
                    }
                }

                callButton.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()
                    when (convertPackageToString(popupNotificationViewState.platform, context)) {
                        "WhatsApp" -> {
                            phoneCall(
                                context,
                                popupNotificationViewState.phoneNumber,
                                MAKE_CALL_PERMISSION_REQUEST_CODE
                            )
                            closeNotificationPopup()
                        }
                        "Message" -> {
                            if (popupNotificationViewState.phoneNumber != "" && popupNotificationViewState.phoneNumber.isNotBlank()
                                && popupNotificationViewState.phoneNumber.isNotEmpty()
                            ) {
                                phoneCall(
                                    context,
                                    popupNotificationViewState.phoneNumber,
                                    MAKE_CALL_PERMISSION_REQUEST_CODE
                                )
                            } else {
                                phoneCall(
                                    context,
                                    popupNotificationViewState.title,
                                    MAKE_CALL_PERMISSION_REQUEST_CODE
                                )
                            }
                            closeNotificationPopup()
                        }
                    }
                }

//                if (newMessage && System.currentTimeMillis() - NotificationsListenerService.adapterNotification.getlastChangeMillis() <= 10000) {
//                    (thisParent!! as RecyclerView).post {
//                        val thisParentRecyclerView = thisParent as RecyclerView
//                        thisParentRecyclerView.requestFocusFromTouch()
//                        thisParentRecyclerView.scrollToPosition(lastChangedPosition + 1)
//                        thisParentRecyclerView.requestFocus()
//                    }
//                    holder.messageToSendEditText?.isFocusable = true
//                    newMessage = false
//                } else if (newMessage && System.currentTimeMillis() - NotificationsListenerService.adapterNotification.getlastChangeMillis() > 10000) {
//                    (thisParent!! as RecyclerView).post {
//                        val thisParentRecyclerView = thisParent as RecyclerView
//                        thisParentRecyclerView.requestFocusFromTouch()
//                        thisParentRecyclerView.scrollToPosition(0)
//                        thisParentRecyclerView.requestFocus()
//                    }
//                    newMessage = false
//                }


                buttonSend.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()

                    if (messageToSend.text.toString() == "") {
                        Toast.makeText(context, R.string.notif_adapter, Toast.LENGTH_SHORT).show()
                    } else {
                        when (convertPackageToString(
                            popupNotificationViewState.platform,
                            context
                        )) {
                            "WhatsApp" -> {
                                sendMessageWithAndroidMessage(
                                    popupNotificationViewState.phoneNumber,
                                    messageToSend.text.toString(),
                                    context
                                )
                                closeNotificationPopup()
                            }
                            "Gmail" -> {
                                closeNotificationPopup()

//                                if (popupNotificationViewState.email != "") {
//                                    sendMail(
//                                        popupNotificationViewState.email,
//                                        sbp.statusBarNotificationInfo["android.text"].toString(),
//                                        messageToSend.text.toString()
//                                    )
//                                } else {
//                                    sendMail(
//                                        popupNotificationViewState.email,
//                                        sbp.statusBarNotificationInfo["android.text"].toString(),
//                                        messageToSend.text.toString()
//
//                                    )
//                                }
                            }
                            "Message" -> {
                                if (popupNotificationViewState.phoneNumber != "") {
                                    sendMessageWithAndroidMessage(
                                        popupNotificationViewState.phoneNumber,
                                        messageToSend.text.toString(),
                                        context
                                    )
                                } else {
                                    sendMessageWithAndroidMessage(
                                        popupNotificationViewState.title,
                                        messageToSend.text.toString(),
                                        context
                                    )
                                }
                                closeNotificationPopup()
                            }
                        }
                        messageToSend.setText("")
                        if (notifications.size > 1) {
                            notifications.removeAt(position)
                        } else {
                            closeNotificationPopup()
                        }
                    }

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
//                        lastChanged = System.currentTimeMillis()
//                        lastChangedPosition = position
//                        listOftext.removeAt(position)
//                        listOftext.add(position, holder.messageToSendEditText!!.text.toString())
                        NotificationsListenerService.alarmSound?.stop()
                    }
                })
                try {
                    val pckManager = context.packageManager
                    val icon = pckManager.getApplicationIcon(popupNotificationViewState.platform)
                    platformImage.setImageDrawable(icon)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    class NotificationComparator : DiffUtil.ItemCallback<PopupNotificationViewState>() {
        override fun areItemsTheSame(
            oldItem: PopupNotificationViewState,
            newItem: PopupNotificationViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PopupNotificationViewState,
            newItem: PopupNotificationViewState
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.contactName == newItem.contactName &&
                    oldItem.description == newItem.description &&
                    oldItem.title == newItem.title &&
                    oldItem.platform == newItem.platform

        }
    }
}