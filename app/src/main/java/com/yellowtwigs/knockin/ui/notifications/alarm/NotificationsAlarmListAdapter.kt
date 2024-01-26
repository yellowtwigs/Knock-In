package com.yellowtwigs.knockin.ui.notifications.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemNotificationAlarmBinding

class NotificationsAlarmListAdapter(
    private val context: Context,
    private val onClickedCallback: (Boolean, String, String, NotificationAlarmViewState) -> Unit
) :
    ListAdapter<NotificationAlarmViewState, NotificationsAlarmListAdapter.ViewHolder>(NotificationsAlarmComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNotificationAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), context, onClickedCallback)
    }

    class ViewHolder(private val binding: ItemNotificationAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isSMS = false

        fun onBind(
            notification: NotificationAlarmViewState,
            context: Context,
            onClickedCallback: (Boolean, String, String, NotificationAlarmViewState) -> Unit
        ) {
            binding.messageContent.text = context.getString(R.string.message_from, notification.title)
            binding.messageHour.text = notification.dateTime

            when (notification.platform) {
                "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {
                    isSMS = true
                    binding.messageImage.setImageResource(R.drawable.ic_micon)
                }

                "com.whatsapp" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_circular_whatsapp)
                }

                "com.google.android.gm" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_circular_gmail)
                }

                "com.microsoft.office.outlook" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_outlook)
                }

                "org.thoughtcrime.securesms" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_circular_signal)
                }

                "org.telegram.messenger" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_telegram)
                }

                "com.facebook.katana" -> {
                    isSMS = false
                    binding.messageImage.setImageResource(R.drawable.ic_circular_messenger)
                }
            }

            binding.root.setOnClickListener {
                onClickedCallback(isSMS, notification.title!!, notification.platform!!, notification)
            }
        }
    }

    object NotificationsAlarmComparator : DiffUtil.ItemCallback<NotificationAlarmViewState>() {
        override fun areItemsTheSame(oldItem: NotificationAlarmViewState, newItem: NotificationAlarmViewState): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: NotificationAlarmViewState, newItem: NotificationAlarmViewState): Boolean {
            return oldItem.contactId == newItem.contactId &&
                    oldItem.platform == newItem.platform
        }
    }
}