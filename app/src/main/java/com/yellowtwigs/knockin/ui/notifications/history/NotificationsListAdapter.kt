package com.yellowtwigs.knockin.ui.notifications.history

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemNotificationBinding
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageToString

class NotificationsListAdapter(
    private val context: Context, private val onClickedCallback: (NotificationsListViewState, String, String) -> Unit
) : ListAdapter<NotificationsListViewState, NotificationsListAdapter.ViewHolder>(ContactComparator()) {

    private lateinit var notification: NotificationsListViewState

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        notification = getItem(position)
        holder.onBind(getItem(position), context, position)
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(notification: NotificationsListViewState, context: Context, position: Int) {
            val pckManager = context.packageManager
            var icon: Drawable? = null
            try {
                icon = pckManager.getApplicationIcon(notification.platform)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val icon2 = icon

            binding.apply {
                notificationImage.setImageDrawable(icon)

                notification.apply {
                    notificationContent.text = description
                    mainBackground.setBackgroundDrawable(background)
                    newIcon.isVisible = notificationIcon != null
                    newIcon.setImageDrawable(notificationIcon)
                    app.text = convertPackageToString(platform, context)
                    notificationSenderName.text = contactName
                    notificationDate.text = notification.date

                    val click = View.OnClickListener {
                        if (modeMultiSelect) {
                            if (listOfItemSelected.contains(notification)) {
                                listOfItemSelected.remove(notification)
                                notificationImage.setImageDrawable(icon2)
                            } else {
                                listOfItemSelected.add(notification)
                                notificationImage.setImageResource(R.drawable.ic_item_selected)
                            }

                            if (context is NotificationsHistoryActivity) {
                                context.recyclerLongClick(notification.id)
                                NotificationsHistoryActivity.currentPosition = position
                            }

                            if (listOfItemSelected.size > 0) {
                                modeMultiSelect = true
                                lastClick = false
                            } else {
                                modeMultiSelect = false
                                lastClick = true
                            }
                        } else {
                            onClickedCallback(notification, notification.phoneNumber, platform)
                        }
                    }
                    val longClick = View.OnLongClickListener {
                        if (listOfItemSelected.contains(notification)) {
                            listOfItemSelected.remove(notification)
                            notificationImage.setImageDrawable(icon2)
                        } else {
                            listOfItemSelected.add(notification)
                            notificationImage.setImageResource(R.drawable.ic_item_selected)
                        }

                        if (context is NotificationsHistoryActivity) {
                            context.recyclerLongClick(notification.id)
                            NotificationsHistoryActivity.currentPosition = position
                        }

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

    class ContactComparator : DiffUtil.ItemCallback<NotificationsListViewState>() {
        override fun areItemsTheSame(
            oldItem: NotificationsListViewState, newItem: NotificationsListViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationsListViewState, newItem: NotificationsListViewState
        ): Boolean {
            return oldItem.contactName == newItem.contactName && oldItem.description == newItem.description && oldItem.idContact == newItem.idContact && oldItem.title == newItem.title && oldItem.platform == newItem.platform
        }
    }

    fun deleteItem(layoutPosition: Int) {
        (context as NotificationsHistoryActivity).deleteItem(currentList[layoutPosition], layoutPosition)
    }

    fun clearAll() {
        modeMultiSelect = false
        secondClick = false
        lastClick = false
        listOfItemSelected.clear()
    }

    companion object {
        var modeMultiSelect = false
        var secondClick = false
        var lastClick = false

        private var listOfItemSelected = ArrayList<NotificationsListViewState>()
    }
}