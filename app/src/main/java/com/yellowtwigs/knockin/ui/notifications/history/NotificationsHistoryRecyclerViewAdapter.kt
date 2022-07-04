package com.yellowtwigs.knockin.ui.notifications.history

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView

import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.data.NotificationDB

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

/**
 * La Classe qui permet de remplir l'historique des notifications
 *
 * @author Florian Striebel
 */
class NotificationsHistoryRecyclerViewAdapter(
    private val context: Context,
    private val notification_history_ListOfNotificationDB: ArrayList<NotificationDB>
) : RecyclerView.Adapter<NotificationsHistoryRecyclerViewAdapter.NotificationHistoryViewHolder>() {
    private var view: View? = null
    private var modeMultiSelect: Boolean? = false
    private val secondClick = false
    private var lastClick: Boolean? = false

    var listOfItemSelected = ArrayList<NotificationDB>()
        set(listOfItemSelected) {
            this.listOfItemSelected.clear()
            this.listOfItemSelected.addAll(listOfItemSelected)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationHistoryViewHolder {
        view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationHistoryViewHolder(view!!)
    }

    fun getItem(position: Int): NotificationDB {
        return notification_history_ListOfNotificationDB[position]
    }

    override fun onBindViewHolder(holder: NotificationHistoryViewHolder, position: Int) {
//        val (_, _, contactName, description, platform, _, timestamp) = getItem(position)
//        val text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(timestamp))
//
//        val pckManager = context.packageManager
//        var icon: Drawable? = null
//        try {
//            icon = pckManager.getApplicationIcon(platform)
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }
//
//        val icon2 = icon
//
//        holder.notif_history_item_AppImage.setImageDrawable(icon)
//        if (description.length > 100) {
//            holder.notif_history_item_NotificationContent.text = description.substring(0, 99) + ".."
//        } else {
//            holder.notif_history_item_NotificationContent.text = description
//        }
//        holder.notif_history_item_NotificationDate.text = text
//        holder.notif_history_item_SenderName.text = contactName
//
//        val longClick = View.OnLongClickListener {
//            view?.tag = holder
//
//            if (this.listOfItemSelected.contains(notification_history_ListOfNotificationDB[position])) {
//                this.listOfItemSelected.remove(notification_history_ListOfNotificationDB[position])
//                holder.notif_history_item_AppImage.setImageDrawable(icon2)
//            } else {
//                this.listOfItemSelected.add(notification_history_ListOfNotificationDB[position])
//                holder.notif_history_item_AppImage.setImageResource(R.drawable.ic_item_selected)
//            }
//
//            if (context is NotificationHistoryActivity) {
//                context.recyclerLongClick(position)
//            }
//
//            if (this.listOfItemSelected.size > 0) {
//                modeMultiSelect = true
//                lastClick = false
//            } else {
//                modeMultiSelect = false
//                lastClick = true
//            }
//
//            modeMultiSelect!!
//        }
//
//        val click = View.OnClickListener {
//            if (modeMultiSelect!!) {
//                view!!.tag = holder
//
//                if (this.listOfItemSelected.contains(notification_history_ListOfNotificationDB[position])) {
//                    this.listOfItemSelected.remove(notification_history_ListOfNotificationDB[position])
//                    holder.notif_history_item_AppImage.setImageDrawable(icon2)
//                } else {
//                    this.listOfItemSelected.add(notification_history_ListOfNotificationDB[position])
//                    holder.notif_history_item_AppImage.setImageResource(R.drawable.ic_item_selected)
//                }
//
//                if (context is NotificationHistoryActivity) {
//                    context.recyclerLongClick(position)
//                }
//
//                if (this.listOfItemSelected.size > 0) {
//                    modeMultiSelect = true
//                    lastClick = false
//                } else {
//                    modeMultiSelect = false
//                    lastClick = true
//                }
//            } else {
//                if (context is NotificationHistoryActivity) {
//                    context.recyclerSimpleClick(position)
//                }
//            }
//        }
//
//        holder.notif_history_item_Layout.setOnLongClickListener(longClick)
//        holder.notif_history_item_Layout.setOnClickListener(click)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return notification_history_ListOfNotificationDB.size
    }

    class NotificationHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        init {
        }
    }
}