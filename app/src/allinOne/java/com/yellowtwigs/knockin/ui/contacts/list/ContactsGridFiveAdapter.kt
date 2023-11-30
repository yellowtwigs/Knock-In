package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGrid4Binding
import com.yellowtwigs.knockin.databinding.ItemContactGrid5Binding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class ContactsGridFiveAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit,
    private val onClickedCallbackMultiSelect: (Int, CircularImageView, ContactsListViewState) -> Unit
) : ListAdapter<ContactsListViewState, ContactsGridFiveAdapter.ViewHolder>(
    ContactsListViewStateComparator()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGrid5Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGrid5Binding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(contact: ContactsListViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                firstName.text = contact.firstName
                lastName.text = contact.lastName

                favoriteIcon.isVisible = contact.isFavorite

                if (cxt is ContactsListActivity) {
                    root.setOnClickListener {
                        if (cxt.modeMultiSelect) {
                            onClickedCallbackMultiSelect(contact.id, civ, contact)
                        } else {
                            onClickedCallback(contact.id)
                        }
                    }

                    root.setOnLongClickListener {
                        onClickedCallbackMultiSelect(contact.id, civ, contact)
                        true
                    }
                }
            }
        }
    }

    class ContactsListViewStateComparator : DiffUtil.ItemCallback<ContactsListViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactsListViewState, newItem: ContactsListViewState
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ContactsListViewState, newItem: ContactsListViewState
        ): Boolean {
            return oldItem == newItem
        }
    }
}