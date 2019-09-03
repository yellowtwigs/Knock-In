package com.yellowtwigs.knocker.controller

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.yellowtwigs.knocker.R
import com.yellowtwigs.knocker.controller.activity.EditContactActivity
import com.yellowtwigs.knocker.controller.activity.MainActivity
import com.yellowtwigs.knocker.controller.activity.NotificationAlarmActivity
import com.yellowtwigs.knocker.controller.activity.group.GroupActivity
import com.yellowtwigs.knocker.model.*
import com.yellowtwigs.knocker.model.ModelDB.ContactDB
import com.yellowtwigs.knocker.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knocker.model.ModelDB.GroupDB
import com.yellowtwigs.knocker.model.ModelDB.NotificationDB

import java.util.ArrayList
import java.util.Objects

import java.sql.DriverManager.println

/**
 * La Classe qui permet de remplir la RecyclerView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class NotificationAlarmRecyclerViewAdapter(private val context: Context, private var notification_alarm_ListOfNotification: MutableList<StatusBarParcelable>, private var nbOfSMS: Int, private var nbOfWhatsappMsg: Int) : RecyclerView.Adapter<NotificationAlarmRecyclerViewAdapter.NotificationAlarmViewHolder>() {
    private var notification_alarm_ContactsDatabase: ContactsRoomDatabase? = null
    private var view: View? = null

    private val FACEBOOK_PACKAGE = "com.facebook.katana"
    private val MESSENGER_PACKAGE = "com.facebook.orca"
    private val WHATSAPP_SERVICE = "com.whatsapp"
    private val GMAIL_PACKAGE = "com.google.android.gm"
    private val MESSAGE_PACKAGE = "com.google.android.apps.messaging"
    private val MESSAGE_SAMSUNG_PACKAGE = "com.samsung.android.messaging"

    private var sharedNbMessagesPreferences: SharedPreferences? = null

    fun getItem(position: Int): StatusBarParcelable {
        return notification_alarm_ListOfNotification[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationAlarmViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_notification_alarm_layout, parent, false)

        return NotificationAlarmViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: NotificationAlarmViewHolder, position: Int) {
        val notification = getItem(position)
        val gestionnaireContacts = ContactManager(context)

        sharedNbMessagesPreferences = context.getSharedPreferences("nbOfSMS", Context.MODE_PRIVATE)
        sharedNbMessagesPreferences = context.getSharedPreferences("nbOfWhatsappMsg", Context.MODE_PRIVATE)

        val contact = gestionnaireContacts.getContact(notification_alarm_ListOfNotification[position].statusBarNotificationInfo["android.title"] as String)

        notification_alarm_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)

        if (context is NotificationAlarmActivity) {

            when (notification.appNotifier!!) {
                "com.whatsapp" -> {
                    holder.item_recycler_notification_alarm_ImageView.setImageResource(R.drawable.ic_circular_whatsapp)

                    if (nbOfWhatsappMsg == 1) {
                        holder.item_recycler_notification_alarm_Text.text = nbOfWhatsappMsg.toString() + " " + context.getString(R.string.notification_alarm_message_received)
                    } else {
                        holder.item_recycler_notification_alarm_Text.text = nbOfWhatsappMsg.toString() + " " + context.getString(R.string.notification_alarm_messages_received)
                    }
                }

                "com.google.android.gm" -> holder.item_recycler_notification_alarm_ImageView.setImageResource(R.drawable.ic_gmail)

                "com.google.android.apps.messaging",
                "com.android.mms",
                "com.samsung.android.messaging" -> {
                    holder.item_recycler_notification_alarm_ImageView.setImageResource(R.drawable.ic_sms)

                    if (nbOfSMS == 1) {
                        holder.item_recycler_notification_alarm_Text.text = nbOfSMS.toString() + " " + context.getString(R.string.notification_alarm_message_received)
                    } else {
                        holder.item_recycler_notification_alarm_Text.text = nbOfSMS.toString() + " " + context.getString(R.string.notification_alarm_messages_received)
                    }
                }
            }
        }

        holder.item_recycler_notification_alarm_Layout.setOnClickListener {
            when (notification.appNotifier!!) {
                "com.whatsapp" -> {
                    if (contact != null) {
                        openWhatsapp(contact.getFirstPhoneNumber())
                    } else {
                        openWhatsapp(notification.statusBarNotificationInfo["android.title"] as String)
                    }
                }

                "com.google.android.gm" -> context.startActivity(Intent())

                "com.google.android.apps.messaging",
                "com.android.mms",
                "com.samsung.android.messaging" -> {
                    if (contact != null) {
                        openSms(contact.getFirstPhoneNumber())
                    } else {
                        openSms(notification.statusBarNotificationInfo["android.title"] as String)
                    }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return notification_alarm_ListOfNotification.size.toLong()
    }

    private fun openSms(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
        edit.putInt("nbOfSMS", 0)
        edit.putInt("nbOfWhatsappMsg", 0)
        edit.apply()

        if (context is NotificationAlarmActivity) {
            context.startActivity(intent)
            context.finish()
        }
    }

    private fun openWhatsapp(phoneNumber: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message")

        val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
        edit.putInt("nbOfSMS", 0)
        edit.putInt("nbOfWhatsappMsg", 0)
        edit.apply()

        if (context is NotificationAlarmActivity) {
            context.startActivity(intent)
            context.finish()
        }
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33 $phoneNumber"
        } else phoneNumber
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return notification_alarm_ListOfNotification.size
    }

    inner class NotificationAlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item_recycler_notification_alarm_Layout: RelativeLayout = view.findViewById(R.id.item_recycler_notification_alarm_layout)
        var item_recycler_notification_alarm_ImageView: AppCompatImageView = view.findViewById(R.id.item_recycler_notification_alarm_image)
        var item_recycler_notification_alarm_Text: TextView = view.findViewById(R.id.item_recycler_notification_alarm_text)
    }
}