package com.yellowtwigs.knockin.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.NotificationHistoryActivity
import com.yellowtwigs.knockin.model.ContactsRoomDatabase
import com.yellowtwigs.knockin.model.ModelDB.NotificationDB
import java.text.SimpleDateFormat
import java.util.*

/**
 * La Classe qui permet de remplir l'historique des notifications
 * @author Florian Striebel
 */
class NotificationsHistoryListViewAdapter(private val context: Context, private val notifications: List<NotificationDB>) : BaseAdapter() {

    private val listOfItemSelected = ArrayList<NotificationDB>()
    private val notification_history_ListOfNotificationDB = ArrayList<NotificationDB>()

    private var notif_history_item_Layout: RelativeLayout? = null
    private var notif_history_item_SenderName: TextView? = null
    private var notif_history_item_NotificationContent: TextView? = null
    private var notif_history_item_NotificationDate: TextView? = null
    private var notif_history_item_AppImage: AppCompatImageView? = null

    private var notif_history_ContactsDatabase: ContactsRoomDatabase? = null

    private var modeMultiSelect: Boolean? = false
    private val secondClick = false
    private var lastClick: Boolean? = false

    override fun getItem(position: Int): NotificationDB {
        return notifications[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return notifications.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        notif_history_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)
        notification_history_ListOfNotificationDB.addAll(notif_history_ContactsDatabase?.notificationsDao()?.getAllNotifications() as ArrayList<NotificationDB>)
//        ArrayList<NotificationDB>
        val (_, _, contactName, description, platform, _, timestamp) = getItem(position)
        val text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(timestamp))
        val pckManager = context.packageManager
        var icon: Drawable? = null
        try {
            icon = pckManager.getApplicationIcon(platform)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_notification_history, parent, false)
        }

        notif_history_item_Layout = view!!.findViewById(R.id.notif_history_item_layout)
        notif_history_item_SenderName = view.findViewById(R.id.notif_history_item_sender_name)
        notif_history_item_NotificationContent = view.findViewById(R.id.notif_history_item_notification_content)
        notif_history_item_NotificationDate = view.findViewById(R.id.notif_history_item_notification_time)
        notif_history_item_AppImage = view.findViewById(R.id.notif_history_item_app_image)

        val icon2 = icon

        notif_history_item_AppImage!!.setImageDrawable(icon)
        if (description.length > 100) {
            notif_history_item_NotificationContent!!.text = description.substring(0, 99) + ".."
        } else {
            notif_history_item_NotificationContent!!.text = description
        }
        notif_history_item_NotificationDate!!.text = text
        notif_history_item_SenderName!!.text = contactName

        val longClick = View.OnLongClickListener {
            view.tag = it.tag

            if (listOfItemSelected.contains(notification_history_ListOfNotificationDB[position])) {
                listOfItemSelected.remove(notification_history_ListOfNotificationDB[position])
                notif_history_item_AppImage!!.setImageDrawable(icon2)
            } else {
                listOfItemSelected.add(notification_history_ListOfNotificationDB[position])
                notif_history_item_AppImage!!.setImageResource(R.drawable.ic_item_selected)
            }

            if (context is NotificationHistoryActivity) {
                context.recyclerLongClick(position)
            }

            if (listOfItemSelected.size > 0) {
                modeMultiSelect = true
                lastClick = false
            } else {
                modeMultiSelect = false
                lastClick = true
            }

            modeMultiSelect!!
        }

        val click = View.OnClickListener {
            if (modeMultiSelect!!) {

                if (listOfItemSelected.contains(notification_history_ListOfNotificationDB[position])) {
                    listOfItemSelected.remove(notification_history_ListOfNotificationDB[position])
                    notif_history_item_AppImage!!.setImageDrawable(icon2)
                } else {
                    listOfItemSelected.add(notification_history_ListOfNotificationDB[position])
                    notif_history_item_AppImage!!.setImageResource(R.drawable.ic_item_selected)
                }

                if (context is NotificationHistoryActivity) {
                    context.recyclerLongClick(position)
                }

                if (listOfItemSelected.size > 0) {
                    modeMultiSelect = true
                    lastClick = false
                } else {
                    modeMultiSelect = false
                    lastClick = true
                }
            } else {
                if (context is NotificationHistoryActivity) {
                    context.recyclerSimpleClick(position)
                }
            }
        }

        notif_history_item_Layout!!.setOnLongClickListener(longClick)
        notif_history_item_Layout!!.setOnClickListener(click)

        return view
    }

    fun updateList(newList: List<NotificationDB>) {
        notification_history_ListOfNotificationDB.clear()
        notification_history_ListOfNotificationDB.addAll(newList)
    }
}