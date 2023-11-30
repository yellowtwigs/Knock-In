package com.yellowtwigs.knockin.ui.groups.list

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGrid4Binding
import com.yellowtwigs.knockin.databinding.ItemContactGrid5Binding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class GroupsGridFiveAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit,
    private val onClickedCallbackMultiSelect: (Int, CircularImageView, ContactInGroupViewState) -> Unit
) :
    ListAdapter<ContactInGroupViewState, GroupsGridFiveAdapter.ViewHolder>(GroupsListViewStateComparator()) {

    var listOfItemSelected = ArrayList<ContactInGroupViewState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGrid5Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGrid5Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactInGroupViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                firstName.text = contact.firstName
                lastName.text = contact.lastName

                root.setOnClickListener {
                    if ((cxt as GroupsListActivity).modeMultiSelect) {
                        onClickedCallbackMultiSelect(contact.id, civ, contact)
                    } else {
                        onClickedCallback(contact.id)
                    }
                }

                root.setOnLongClickListener {
                    onClickedCallbackMultiSelect(contact.id, civ, contact)
                    true
                }

                if (SectionGroupsListAdapter.isSectionClicked) {
                    civ.setImageResource(contact.profilePictureSelected)
                }
            }
        }
    }

    class GroupsListViewStateComparator : DiffUtil.ItemCallback<ContactInGroupViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactInGroupViewState,
            newItem: ContactInGroupViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactInGroupViewState,
            newItem: ContactInGroupViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64 &&
                    oldItem.firstPhoneNumber == newItem.firstPhoneNumber &&
                    oldItem.secondPhoneNumber == newItem.secondPhoneNumber &&
                    oldItem.listOfMails == newItem.listOfMails
        }
    }
}