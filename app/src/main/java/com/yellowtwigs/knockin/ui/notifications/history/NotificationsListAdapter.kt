package com.yellowtwigs.knockin.ui.notifications.history

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemNotificationBinding
import com.yellowtwigs.knockin.model.data.NotificationDB

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

/**
 * La Classe qui permet de remplir l'historique des notifications
 *
 * @author Florian Striebel
 */
class NotificationsListAdapter(private val context: Context) :
    ListAdapter<NotificationDB, NotificationsListAdapter.ViewHolder>(ContactComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), context)
    }

    class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(notificationDB: NotificationDB, context: Context) {
            val text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(notificationDB.timestamp))

            val pckManager = context.packageManager
            var icon: Drawable? = null
            try {
                icon = pckManager.getApplicationIcon(notificationDB.platform)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val icon2 = icon

            binding.apply {
                notificationImage.setImageDrawable(icon)

                notificationDB.apply {
                    if (description.length > 151) {
                        notificationContent.text =
                            description.substring(0, 150) + ".."
                    } else {
                        notificationContent.text = description
                    }

                    if (contactName.length > 25) {
                        notificationSenderName.text =
                            contactName.substring(0, 24) + ".."
                    } else {
                        notificationSenderName.text = contactName
                    }
                    notificationDate.text = text

                    val click = View.OnClickListener {
                        Log.i("notifications", "${modeMultiSelect}")
                        if (modeMultiSelect) {
                            if (listOfItemSelected.contains(notificationDB)
                            ) {
                                listOfItemSelected.remove(notificationDB)
                                notificationImage.setImageDrawable(icon2)
                            } else {
                                listOfItemSelected.add(notificationDB)
                                notificationImage.setImageResource(R.drawable.ic_item_selected)
                            }

//                            if (context is NotificationHistoryActivity) {
//                                context.recyclerLongClick(position)
//                            }

                            if (listOfItemSelected.size > 0) {
                                modeMultiSelect = true
                                lastClick = false
                            } else {
                                modeMultiSelect = false
                                lastClick = true
                            }
                        } else {
//                            if (context is NotificationHistoryActivity) {
//                                context.recyclerSimpleClick(position)
//                            }
                        }
                    }
                    val longClick = View.OnLongClickListener {
                        if (listOfItemSelected.contains(notificationDB)) {
                            listOfItemSelected.remove(notificationDB)
                            notificationImage.setImageDrawable(icon2)
                        } else {
                            listOfItemSelected.add(notificationDB)
                            notificationImage.setImageResource(R.drawable.ic_item_selected)
                        }

//                        if (context is NotificationHistoryActivity) {
//                            context.recyclerLongClick(position)
//                        }

                        if (listOfItemSelected.size > 0) {
                            modeMultiSelect = true
                            lastClick = false
                        } else {
                            modeMultiSelect = false
                            lastClick = true
                        }

                        modeMultiSelect
                    }

                    root.setOnLongClickListener(longClick)
                    root.setOnClickListener(click)
                }
            }
        }
    }

    class ContactComparator : DiffUtil.ItemCallback<NotificationDB>() {
        override fun areItemsTheSame(
            oldItem: NotificationDB,
            newItem: NotificationDB
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationDB,
            newItem: NotificationDB
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.contactName == newItem.contactName &&
                    oldItem.description == newItem.description &&
                    oldItem.idContact == newItem.idContact &&
                    oldItem.title == newItem.title &&
                    oldItem.platform == newItem.platform

        }
    }

    fun clearAll(){
        modeMultiSelect = false
        secondClick = false
        lastClick = false
        listOfItemSelected.clear()
    }

    companion object {
        var modeMultiSelect = false
        var secondClick = false
        var lastClick = false

        private var listOfItemSelected = ArrayList<NotificationDB>()
            set(listOfItemSelected) {
                this.listOfItemSelected.clear()
                this.listOfItemSelected.addAll(listOfItemSelected)
            }
    }
}