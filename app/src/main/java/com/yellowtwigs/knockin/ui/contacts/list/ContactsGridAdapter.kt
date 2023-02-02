package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.initContactNameFromGrid

class ContactsGridAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit,
    private val onClickedCallbackMultiSelect: (Int, CircularImageView, ContactsListViewState) -> Unit
) : ListAdapter<ContactsListViewState, ContactsGridAdapter.ViewHolder>(
    ContactsListViewStateComparator()
) {

    private var imageHeight = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        imageHeight = binding.civ.layoutParams.height
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGridBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactsListViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                val len = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE).getInt("gridview", 4)

                initContactNameFromGrid(contact.firstName, contact.lastName, len, firstName, lastName, civ, imageHeight)

                favoriteIcon.isVisible = contact.isFavorite

                root.setOnClickListener {
                    if ((cxt as ContactsListActivity).modeMultiSelect) {
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