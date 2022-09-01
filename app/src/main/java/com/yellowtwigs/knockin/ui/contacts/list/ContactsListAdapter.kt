package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SectionIndexer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactListBinding
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
import java.util.*
import kotlin.collections.ArrayList

class ContactsListAdapter(private val cxt: Context, private val onClickedCallback: (Int) -> Unit) :
    ListAdapter<ContactsListViewState, ContactsListAdapter.ViewHolder>(
        ContactsListViewStateComparator()
    ), SectionIndexer {

    private var modeMultiSelect = false
    private var isScrolling = false
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

                var cpt = 1

                if (contact.listOfMails.isNotEmpty() && contact.listOfMails[0].isNotEmpty() && contact.listOfMails[0].isNotBlank()) {
                    cpt += 1
                    mailLayout.visibility = View.VISIBLE
                } else {
                    mailLayout.visibility = View.GONE
                }
                if (contact.listOfPhoneNumbers.isNotEmpty()) {
                    cpt += 2
                    callLayout.visibility = View.VISIBLE
                    smsLayout.visibility = View.VISIBLE
                } else {
                    callLayout.visibility = View.GONE
                    smsLayout.visibility = View.GONE
                }
                if (isWhatsappInstalled(cxt) && contact.hasWhatsapp) {
                    cpt += 1
                    whatsappLayout.visibility = View.VISIBLE
                } else {
                    whatsappLayout.visibility = View.GONE
                }
                if (isMessengerInstalled(cxt) && contact.messengerId != "") {
                    cpt += 1
                    if (!appsSupportBought) {
                        messengerIcon.setImageResource(R.drawable.ic_messenger_disable)
                    }
                    messengerLayout.visibility = View.VISIBLE
                } else {
                    messengerLayout.visibility = View.GONE
                }
                if (isTelegramInstalled(cxt) && contact.hasTelegram) {
                    cpt += 1
                    if (!appsSupportBought) {
                        telegramIcon.setImageResource(R.drawable.ic_telegram_disable)
                    }
                    telegramLayout.visibility = View.VISIBLE
                } else {
                    telegramLayout.visibility = View.GONE
                }
                if (isSignalInstalled(cxt) && contact.hasSignal) {
                    cpt += 1
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
                            openSms(contact.listOfPhoneNumbers[0], cxt as ContactsListActivity)
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
                        editLayout.id -> {
                            onClickedCallback(contact.id)
                        }
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
                    Log.i("onScrollStateChanged", "onScrollStateChanged")
                    if (listContactItemMenu.visibility == View.GONE) {
                        val slideUp = AnimationUtils.loadAnimation(cxt, R.anim.slide_up)
                        listContactItemMenu.startAnimation(slideUp)
                        listContactItemMenu.visibility = View.VISIBLE
                    } else {
                        listContactItemMenu.visibility = View.GONE
                    }
                }

                if (isScrolling) {
                    listContactItemMenu.visibility = View.GONE
                }

                val param = listContactItemMenu.layoutParams as ViewGroup.MarginLayoutParams
                listContactItemMenu.scrollBarFadeDuration = 20000
                listContactItemMenu.scrollBarSize = 25
                listContactItemMenu.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY

                when (cpt) {
                    3 -> {
                        param.setMargins(230, 0, 0, 0)
                        listContactItemMenu.layoutParams = param
                    }
                    4 -> {
                        param.setMargins(150, 0, 0, 0)
                        listContactItemMenu.layoutParams = param
                    }
                    5 -> {
                        param.setMargins(50, 0, 0, 0)
                        listContactItemMenu.layoutParams = param
                    }
                    6 -> {
                        param.setMargins(0, 0, 0, 0)
                        listContactItemMenu.layoutParams = param
                    }
                }
            }
        }
    }

    fun setIsScrolling(scroll: Boolean) {
        isScrolling = scroll
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

    override fun getSectionForPosition(position: Int): Int {
        return position
    }

    override fun getSections(): Array<Any> {
        val alphabetFull: ArrayList<String> = ArrayList()

        val mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        for (element in mSections) {
            alphabetFull.add(element.toString())
        }
        return alphabetFull.toArray(arrayOfNulls<String>(0))
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        val sections = arrayListOf(
            "",
            "#",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z"
        )

        var position = 0

        currentList.forEachIndexed { index, contact ->
            // sectionIndex = 7
            // sections[sectionIndex] = G
//            sections[sectionIndex]

            val firstLetter = if (contact.firstName.isNotBlank() && contact.firstName != "") {
                contact.firstName.first().uppercase()
            } else {
                contact.lastName.first().uppercase()
            }

            if (firstLetter == sections[sectionIndex]) {
                position = index
                return@forEachIndexed
            }
        }


        return position
//        return currentList.indexOf(currentList[sectionIndex])
    }
}