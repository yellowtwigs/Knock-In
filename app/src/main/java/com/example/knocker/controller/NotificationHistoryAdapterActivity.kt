package com.example.knocker.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * La Classe qui permet de remplir l'historique des notifications
 * @author Florian Striebel
 */
class NotificationHistoryAdapterActivity(private val context:Context ,private val notifications:ArrayList<NotificationDB>) : BaseAdapter() {
    var notification_history_adapter_expediteur:TextView?=null
    var notification_history_adapter_contenue:TextView?=null
    var notification_history_adapter_Date:TextView?=null
    var notification_history_adapter_App: ImageView?=null
    override fun getItem(position: Int): NotificationDB {
        return notifications[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return notifications.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        val notif = getItem(position)

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.activity_notification_history_adapter, parent, false)
            }
            notification_history_adapter_expediteur = convertView!!.findViewById<TextView>(R.id.history_adapter_expediteur)
            notification_history_adapter_contenue = convertView.findViewById<TextView>(R.id.history_adapter_contenu)
            notification_history_adapter_Date = convertView.findViewById<TextView>(R.id.history_adapter_time)
            notification_history_adapter_App=convertView.findViewById(R.id.history_adapter_imageView)
            notification_history_adapter_expediteur!!.text = notif.contactName
            notification_history_adapter_contenue!!.text = notif.description
            notification_history_adapter_Date!!.text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notif.timestamp))
            println("Date(notif.timestamp.toLong()).toString()"+Date(notif.timestamp.toLong()).toString())
            val pckManager = context.packageManager
            val icon = pckManager.getApplicationIcon(notif.platform)
            notification_history_adapter_App!!.setImageDrawable(icon)

            println("notification "+position+" "+notif.platform)

        return convertView
    }

}
