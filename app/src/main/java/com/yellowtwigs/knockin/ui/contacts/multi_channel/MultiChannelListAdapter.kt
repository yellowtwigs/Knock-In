package com.yellowtwigs.knockin.ui.contacts.multi_channel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.MultiChannelItemLayoutBinding
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class MultiChannelListAdapter(
    private val cxt: Context,
    private val onClickedCallbackSms: (Int, AppCompatImageView, String) -> Unit,
    private val onClickedCallbackEmail: (Int, AppCompatImageView, String) -> Unit,
    private val onClickedCallbackWhatsapp: (Int, AppCompatImageView, String) -> Unit
) :
    ListAdapter<ContactsListViewState, MultiChannelListAdapter.ViewHolder>(
        MultiChannelListViewStateComparator()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            MultiChannelItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: MultiChannelItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactsListViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                name.text = contact.firstName + " " + contact.lastName

                gmail.isVisible = contact.listOfMails[0] != ""
                sms.isVisible = contact.listOfPhoneNumbers[0] != ""
                whatsapp.isVisible = isWhatsappInstalled(cxt) && contact.hasWhatsapp

                gmail.setOnClickListener {
                    onClickedCallbackEmail(contact.id, gmail, contact.listOfMails.random())

                }

                sms.setOnClickListener {
                    onClickedCallbackSms(contact.id, sms, contact.listOfPhoneNumbers.random())
                }

                whatsapp.setOnClickListener {
                    onClickedCallbackWhatsapp(
                        contact.id,
                        whatsapp,
                        contact.listOfPhoneNumbers.random()
                    )
                }
            }
        }
    }

    class MultiChannelListViewStateComparator : DiffUtil.ItemCallback<ContactsListViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactsListViewState,
            newItem: ContactsListViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactsListViewState,
            newItem: ContactsListViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64 &&
                    oldItem.listOfPhoneNumbers == newItem.listOfPhoneNumbers &&
                    oldItem.listOfMails == newItem.listOfMails &&
                    oldItem.priority == newItem.priority &&
                    oldItem.isFavorite == newItem.isFavorite &&
                    oldItem.messengerId == newItem.messengerId &&
                    oldItem.hasWhatsapp == newItem.hasWhatsapp &&
                    oldItem.hasTelegram == newItem.hasTelegram &&
                    oldItem.hasSignal == newItem.hasSignal
        }
    }
}