package com.example.firsttestknocker

import android.app.Notification
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NotificationHistoryAdapterActivity(private val context:Context ,private val notifications:ArrayList<Notifications>) : BaseAdapter() {
    override fun getItem(position: Int): Notifications {
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
