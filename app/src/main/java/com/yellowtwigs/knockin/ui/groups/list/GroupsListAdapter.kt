package com.yellowtwigs.knockin.ui.groups.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.HorizontalScrollView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactListBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isSignalInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.isTelegramInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.openMailApp
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class GroupsListAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit,
    private val onClickedCallbackMultiSelect: (Int, CircularImageView, ContactInGroupViewState) -> Unit
) : ListAdapter<ContactInGroupViewState, GroupsListAdapter.ViewHolder>(
    GroupsListViewStateComparator()
) {

    var listOfItemSelected = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContactListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactInGroupViewState) {
            binding.apply {
                val sp = cxt.getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)
                val appsSupportBought = sp.getBoolean("Apps_Support_Bought", false)

                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact, civ, cxt)

                name.text = contact.firstName + " " + contact.lastName

                itemLayout.setOnClickListener {
                    if ((cxt as GroupsListActivity).modeMultiSelect) {
                        onClickedCallbackMultiSelect(contact.id, civ, contact)
//                        listContactItemMenu.visibility = View.GONE
                    } else {
//                        if (listContactItemMenu.visibility == View.GONE) {
//                            val slideUp = AnimationUtils.loadAnimation(cxt, R.anim.slide_up)
//                            listContactItemMenu.startAnimation(slideUp)
//                            listContactItemMenu.visibility = View.VISIBLE
//                            if (lastSelectMenuLen1 != null) lastSelectMenuLen1?.visibility =
//                                View.GONE
//                            lastSelectMenuLen1 = listContactItemMenu
//                        } else {
//                            listContactItemMenu.visibility = View.GONE
//                            lastSelectMenuLen1 = null
//                        }
                    }
                }

                itemLayout.setOnLongClickListener {
                    onClickedCallbackMultiSelect(contact.id, civ, contact)
                    true
                }

                if (SectionGroupsListAdapter.isSectionClicked) {
                    civ.setImageResource(contact.profilePictureSelected)
                }
            }
        }

        fun contactProfilePicture(
            picture64: String,
            contact: ContactInGroupViewState,
            civ: CircularImageView,
            cxt: Context
        ) {
            if (contact.pictureMultiSelect != 0) {
                civ.setImageResource(contact.pictureMultiSelect)
            } else {
                if (picture64 != "") {
                    val bitmap = Converter.base64ToBitmap(picture64)
                    civ.setImageBitmap(bitmap)
                } else {
                    civ.setImageResource(
                        RandomDefaultImage.randomDefaultImage(
                            contact.profilePicture, cxt
                        )
                    )
                }
            }
        }
    }

    class GroupsListViewStateComparator : DiffUtil.ItemCallback<ContactInGroupViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactInGroupViewState, newItem: ContactInGroupViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactInGroupViewState, newItem: ContactInGroupViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName && oldItem.lastName == newItem.lastName && oldItem.profilePicture == newItem.profilePicture && oldItem.profilePicture64 == newItem.profilePicture64 && oldItem.listOfPhoneNumbers == newItem.listOfPhoneNumbers && oldItem.listOfMails == newItem.listOfMails
        }
    }
}