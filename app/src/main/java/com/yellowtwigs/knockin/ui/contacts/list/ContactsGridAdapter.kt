package com.yellowtwigs.knockin.ui.contacts.list

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class ContactsGridAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit,
    private val onClickedCallbackMultiSelect: (Int, CircularImageView, ContactsListViewState) -> Unit
) :
    ListAdapter<ContactsListViewState, ContactsGridAdapter.ViewHolder>(
        ContactsListViewStateComparator()
    ) {

    private var imageHeight = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        imageHeight = binding.civ.layoutParams.height
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactsListViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)
                var firstname = contact.firstName
                var lastname = contact.lastName

                val len = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    .getInt("gridview", 4)

                val layoutParamsTV = firstName.layoutParams as ConstraintLayout.LayoutParams
                val layoutParamsIV = civ.layoutParams as ConstraintLayout.LayoutParams

                if (len == 4) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.25).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.25).toInt()
                    layoutParamsTV.topMargin = 10
                    layoutParamsIV.topMargin = 10

                    if (contact.firstName.length > 12)
                        firstname = contact.firstName.substring(0, 10) + ".."

                    if (contact.lastName.length > 12)
                        lastname = contact.lastName.substring(0, 10) + ".."

                    val sizeFirstName = "$firstname"
                    val spanFirstName = SpannableString("$firstname")
                    spanFirstName.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        sizeFirstName.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val sizeLastName = "$lastName"
                    val spanLastName = SpannableString("$lastName")
                    spanLastName.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        sizeLastName.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    firstName.text = spanFirstName
                    lastName.text = spanLastName
                } else if (len == 5) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.40).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.40).toInt()
                    layoutParamsTV.topMargin = 0
                    layoutParamsIV.topMargin = 0

                    if (contact.firstName.length > 11)
                        firstname = contact.firstName.substring(0, 9) + ".."

                    val sizeFirstName = "$firstname"
                    val spanFirstName = SpannableString("$firstname")
                    spanFirstName.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        sizeFirstName.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val sizeLastName = "$lastName"
                    val spanLastName = SpannableString("$lastName")
                    spanLastName.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        sizeLastName.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    firstName.text = spanFirstName
                    lastName.text = spanLastName
                }

                firstName.text = firstname
                lastName.text = lastname

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
            oldItem: ContactsListViewState,
            newItem: ContactsListViewState
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ContactsListViewState,
            newItem: ContactsListViewState
        ): Boolean {
            return oldItem == newItem
        }
    }
}