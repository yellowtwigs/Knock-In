package com.yellowtwigs.knockin.model.service

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemPopupNotificationBinding
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.ContactGesture.sendMail
import com.yellowtwigs.knockin.utils.ContactGesture.sendMessageWithAndroidMessage
import com.yellowtwigs.knockin.utils.ContactGesture.sendMessageWithWhatsapp
import com.yellowtwigs.knockin.utils.NotificationsGesture.phoneCall

class PopupNotificationsListAdapter(
    private val cxt: Context,
    private val windowManager: WindowManager,
    private val popupView: View
) : ListAdapter<PopupNotificationViewState, PopupNotificationsListAdapter.ViewHolder>(
    PopupNotificationViewStateComparator()
) {

    private lateinit var thisParent: ViewGroup
    var newMessage = false

    private val MAKE_CALL_PERMISSION_REQUEST_CODE = 1
    private var numberForPermission = ""

    var isClose = false
    private var lastChanged: Long = 0
    private var lastChangedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        thisParent = parent
        val binding = ItemPopupNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun deleteItem(position: Int) {
        NotificationsListenerService.deleteItem(position)
        notifyItemRemoved(position)

        if (NotificationsListenerService.popupNotificationViewStates.size == 0) {
            closeNotificationPopup()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPopupNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(popup: PopupNotificationViewState) {
            binding.apply {
                messageToSend.isEnabled = true

                val unwrappedDrawable =
                    AppCompatResources.getDrawable(cxt, R.drawable.item_notif_adapter_top_bar)
                val wrappedDrawable = unwrappedDrawable?.let { DrawableCompat.wrap(it) }

                platform.text = popup.platform

                when (popup.platform) {
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
                    "WhatsApp" -> {
                        callButton.visibility = View.VISIBLE
                        buttonSend.visibility = View.VISIBLE
                        messageToSend.visibility = View.VISIBLE

                        if (wrappedDrawable != null) {
                            setupIconAndColor(
                                platformImage,
                                wrappedDrawable,
                                R.drawable.ic_circular_whatsapp,
                                R.color.custom_shape_top_bar_whatsapp,
                                cxt
                            )
                        }
                    }
                }

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
                            if (popup.messengerId != "") {
                                openMessenger(popup.messengerId, cxt)
                            } else {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.messenger.com/t/")
                                    )
                                intent.flags = FLAG_ACTIVITY_NEW_TASK
                                cxt.startActivity(intent)
                            }
                            closeNotificationPopup()
                        }
                        "WhatsApp" -> {
                            openWhatsapp(popup.phoneNumber, cxt)
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
                            binding.messageToSend.text?.toString()?.let { message ->
                                sendMessageWithAndroidMessage(
                                    popup.phoneNumber,
                                    message,
                                    cxt
                                )
                            }
                            closeNotificationPopup()
                        }
                        "Signal" -> {
                            goToSignal(cxt)
                        }
                        "Telegram" -> {
                            goToTelegram(cxt, popup.phoneNumber)
                        }
                    }
                }

                callButton.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()
                    phoneCall(popup.phoneNumber)
                }

                buttonSend.setOnClickListener {
                    NotificationsListenerService.alarmSound?.stop()
                    if (messageToSend.text.toString() == "") {
                        Toast.makeText(cxt, R.string.notif_adapter, Toast.LENGTH_SHORT).show()
                    } else {
                        val message = messageToSend.text.toString()
                        when (popup.platform) {
                            "WhatsApp" -> {
                                sendMessageWithWhatsapp(popup.phoneNumber, message, cxt)
                                closeNotificationPopup()
                            }
                            "Gmail" -> {
                                sendMail(popup.email, "", message, cxt)
                                closeNotificationPopup()
                            }
                            "Message" -> {
                                sendMessageWithAndroidMessage(popup.phoneNumber, message, cxt)
                                closeNotificationPopup()
                            }
                        }
                    }
                }
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

        private fun getLastChangeMillis(): Long {
            return lastChanged
        }
    }

    private fun phoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                cxt,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(cxt, cxt.getString(R.string.allow_phone_call), Toast.LENGTH_SHORT).show()
            numberForPermission = phoneNumber
        } else {
            closeNotificationPopup()
            val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            if (numberForPermission.isEmpty()) {
                cxt.startActivity(intent)
            } else {
                cxt.startActivity(intent)
                numberForPermission = ""
            }
        }
    }

    private fun closeNotificationPopup() {
        NotificationsListenerService.alarmSound?.stop()
        windowManager.removeView(popupView)
        val sharedPreferences =
            cxt.getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        edit.putBoolean("view", false)
        edit.apply()
    }

    class PopupNotificationViewStateComparator :
        DiffUtil.ItemCallback<PopupNotificationViewState>() {
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
                    oldItem.title == newItem.title &&
                    oldItem.description == newItem.description &&
                    oldItem.platform == newItem.platform &&
                    oldItem.contactName == newItem.contactName &&
                    oldItem.phoneNumber == newItem.phoneNumber &&
                    oldItem.messengerId == newItem.messengerId &&
                    oldItem.email == newItem.email
        }

    }
}