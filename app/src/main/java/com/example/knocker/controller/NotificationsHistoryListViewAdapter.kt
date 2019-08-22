package com.example.knocker.controller

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.knocker.R
import com.example.knocker.controller.activity.NotificationHistoryActivity
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.ModelDB.NotificationDB
import java.text.SimpleDateFormat
import java.util.*

/**
 * La Classe qui permet de remplir l'historique des notifications
 * @author Florian Striebel
 */
class NotificationsHistoryListViewAdapter(private val context: Context, private val notifications: List<NotificationDB>) : BaseAdapter() {

    private var notification_history_adapter_layout: ConstraintLayout? = null
    private val listOfItemSelected = ArrayList<NotificationDB>()
    private val notification_history_ListOfNotificationDB = ArrayList<NotificationDB>()


    private var notification_history_adapter_expediteur: TextView? = null
    private var notification_history_adapter_contenue: TextView? = null
    private var notification_history_adapter_Date: TextView? = null
    private var notification_history_adapter_App: AppCompatImageView? = null
    private var notif_history_ContactsDatabase: ContactsRoomDatabase? = null

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

        var view = convertView

        val notif = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_notification_history, parent, false)
        }
        notification_history_adapter_layout = view!!.findViewById(R.id.history_adapter_layout)
        notification_history_adapter_expediteur = view.findViewById(R.id.history_adapter_expediteur)
        notification_history_adapter_contenue = view.findViewById(R.id.history_adapter_contenu)
        notification_history_adapter_Date = view.findViewById(R.id.history_adapter_time)
        notification_history_adapter_App = view.findViewById(R.id.history_adapter_imageView)
        notification_history_adapter_expediteur!!.text = notif.contactName
        notification_history_adapter_Date!!.text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notif.timestamp))

        val pckManager = context.packageManager
        val icon = pckManager.getApplicationIcon(notif.platform)
        notification_history_adapter_App!!.setImageDrawable(icon)

        println("notification " + position + " " + notif.platform)

        if (notif.description.length > 100) {
            notification_history_adapter_contenue!!.text = notif.description.substring(0, 99) + ".."
        } else {
            notification_history_adapter_contenue!!.text = notif.description
        }

//        notification_history_adapter_layout!!.setOnLongClickListener {
//            val notifSelected = notif_history_ContactsDatabase!!.notificationsDao().getNotification(notification_history_ListOfNotificationDB[position].id!!)
//
//            if (listOfItemSelected.contains(notifSelected)) {
//                listOfItemSelected.remove(notifSelected)
//
//                notification_history_adapter_App!!.setImageDrawable(icon)
//            } else {
//                listOfItemSelected.add(notifSelected)
//                notification_history_adapter_App!!.setImageDrawable(context.getDrawable(R.drawable.ic_contact_selected))
//            }
//
//            if (context is NotificationHistoryActivity) {
//                context.longNotifHistoryListItemClick(position)
//            }
//
//            true
//        }

        return view
    }
}