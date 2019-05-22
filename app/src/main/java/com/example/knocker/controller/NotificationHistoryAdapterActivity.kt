package com.example.knocker.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.knocker.model.ModelDB.NotificationDB
import com.example.knocker.R

class NotificationHistoryAdapterActivity(private val context:Context ,private val notifications:ArrayList<NotificationDB>) : BaseAdapter() {
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_notification_history_adapter, parent, false)
        }
        val notif = getItem(position)
        val expTxt = convertView!!.findViewById<TextView>(R.id.history_adapter_expediteur)
        val contenu= convertView.findViewById<TextView>(R.id.history_adapter_contenu)
        val date= convertView.findViewById<TextView>(R.id.history_adapter_time)
        expTxt.text=notif.contactName
        contenu.text= notif.description
        date.text=notif.dateTime
        return convertView
    }

}
