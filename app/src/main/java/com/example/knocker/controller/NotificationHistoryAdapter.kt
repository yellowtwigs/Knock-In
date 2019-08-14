package com.example.knocker.controller

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import com.example.knocker.model.ContactsRoomDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * La Classe qui permet de remplir l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryAdapter(private val context: Context, private val notifications: List<NotificationDB>) : BaseAdapter() {
    private var notification_history_adapter_expediteur: TextView? = null
    private var notification_history_adapter_contenue: TextView? = null
    private var notification_history_adapter_Date: TextView? = null
    private var notification_history_adapter_App: AppCompatImageView? = null
    private var listOfPhoneNumber = ArrayList<String>()
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

    fun getListOfPhoneNumber(): ArrayList<String> {
        return listOfPhoneNumber
    }

    @SuppressLint("SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        notif_history_ContactsDatabase = ContactsRoomDatabase.getDatabase(context)

        var view = convertView

        val notif = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_notification_history, parent, false)
        }
        notification_history_adapter_expediteur = view!!.findViewById(R.id.history_adapter_expediteur)
        notification_history_adapter_contenue = view.findViewById(R.id.history_adapter_contenu)
        notification_history_adapter_Date = view.findViewById(R.id.history_adapter_time)
        notification_history_adapter_App = view.findViewById(R.id.history_adapter_imageView)
        notification_history_adapter_expediteur!!.text = notif.contactName
        notification_history_adapter_contenue!!.text = notif.description
        notification_history_adapter_Date!!.text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notif.timestamp))

        val pckManager = context.packageManager
        val icon = pckManager.getApplicationIcon(notif.platform)
        notification_history_adapter_App!!.setImageDrawable(icon)

        println("notification " + position + " " + notif.platform)

        return view
    }
}