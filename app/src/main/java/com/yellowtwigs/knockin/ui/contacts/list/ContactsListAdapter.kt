package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactListBinding
import com.yellowtwigs.knockin.ui.contacts.Main2Activity
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isMessengerInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.isSignalInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.isTelegramInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.openMailApp
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture
import kotlin.collections.ArrayList

class ContactsListAdapter(private val cxt: Context, private val onClickedCallback: (Int) -> Unit) :
    ListAdapter<ContactsListViewState, ContactsListAdapter.ViewHolder>(
        ContactsListViewStateComparator()
    ) {

    private var modeMultiSelect = false
    var listOfItemSelected = ArrayList<ContactsListViewState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContactListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactsListViewState) {
            binding.apply {
                val sp = cxt.getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)
                val appsSupportBought = sp.getBoolean("Apps_Support_Bought", false)

                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                name.text = contact.firstName + " " + contact.lastName

                if (contact.listOfMails.isNotEmpty()) {
                    mailLayout.visibility = View.VISIBLE
                } else {
                    mailLayout.visibility = View.GONE
                }
                if (contact.listOfPhoneNumbers.isNotEmpty()) {
                    callLayout.visibility = View.VISIBLE
                    smsLayout.visibility = View.VISIBLE
                } else {
                    callLayout.visibility = View.GONE
                    smsLayout.visibility = View.GONE
                }
                if (isWhatsappInstalled(cxt) && contact.hasWhatsapp) {
                    whatsappLayout.visibility = View.VISIBLE
                } else {
                    whatsappLayout.visibility = View.GONE
                }
                if (isMessengerInstalled(cxt) && contact.messengerId != "") {
                    if (!appsSupportBought) {
                        messengerIcon.setImageResource(R.drawable.ic_messenger_disable)
                    }
                    messengerLayout.visibility = View.VISIBLE
                } else {
                    messengerLayout.visibility = View.GONE
                }
                if (isTelegramInstalled(cxt) && contact.hasTelegram) {
                    if (!appsSupportBought) {
                        telegramIcon.setImageResource(R.drawable.ic_telegram_disable)
                    }
                    telegramLayout.visibility = View.VISIBLE
                } else {
                    telegramLayout.visibility = View.GONE
                }
                if (isSignalInstalled(cxt) && contact.hasSignal) {
                    if (!appsSupportBought) {
                        signalIcon.setImageResource(R.drawable.ic_signal_disable)
                    }
                    signalLayout.visibility = View.VISIBLE
                } else {
                    signalLayout.visibility = View.GONE
                }

                val listener = View.OnClickListener { v: View ->
                    when (v.id) {
                        smsLayout.id -> {
                            openSms(contact.listOfPhoneNumbers[0], cxt as Main2Activity)
                        }
                        callLayout.id -> {
                            callPhone(contact.listOfPhoneNumbers[0], cxt)
                        }
                        mailLayout.id -> {
                            openMailApp(contact.listOfMails[0], cxt)
                        }
                        messengerLayout.id -> {
                            openMessenger(contact.messengerId, cxt)
                        }
                        whatsappLayout.id -> {
                            openWhatsapp(contact.listOfPhoneNumbers[0], cxt)
                        }
                        telegramLayout.id -> {
                            goToTelegram(cxt, contact.listOfPhoneNumbers[0])
                        }
                        signalLayout.id -> {
                            goToSignal(cxt)
                        }
//                        itemLayout.id -> {
//                            if(modeMultiSelect){
//
//                            }else{
//                                val intent = Intent(cxt, EditContactDetailsActivity::class.java)
//                                intent.putExtra("ContactId", contact.id)
//                                intent.putExtra("position", position)
//                                cxt.startActivity(intent)
//                            }
//                        }
                    }
                }

                smsLayout.setOnClickListener(listener)
                callLayout.setOnClickListener(listener)
                mailLayout.setOnClickListener(listener)
                messengerLayout.setOnClickListener(listener)
                whatsappLayout.setOnClickListener(listener)
                telegramLayout.setOnClickListener(listener)
                signalLayout.setOnClickListener(listener)

                itemLayout.setOnClickListener {
                    onClickedCallback(contact.id)
                }


            }
        }
    }

    class ContactsListViewStateComparator : DiffUtil.ItemCallback<ContactsListViewState>() {
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
            return oldItem.id == newItem.id &&
                    oldItem.firstName == newItem.firstName &&
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