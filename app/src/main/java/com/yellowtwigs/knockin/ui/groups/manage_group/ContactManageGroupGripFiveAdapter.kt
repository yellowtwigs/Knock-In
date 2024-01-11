package com.yellowtwigs.knockin.ui.groups.manage_group

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactGrid5Binding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactManageGroupGripFiveAdapter(
    private val cxt: Context,
    private val listOfItemSelected: ArrayList<String>,
    private val onClickedCallback: (String) -> Unit
) :
    ListAdapter<ContactManageGroupViewState, ContactManageGroupGripFiveAdapter.ViewHolder>(
        ContactManageGroupViewStateComparator()
    ) {

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

        fun onBind(contact: ContactManageGroupViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                firstName.text = contact.firstName
                lastName.text = contact.lastName

                itemSelected(contact.id, civ, contact)

                root.setOnClickListener {
                    onClickedCallback(contact.id.toString())
                    itemSelected(contact.id, civ, contact)
                }
            }
        }
    }

    class ContactManageGroupViewStateComparator :
        DiffUtil.ItemCallback<ContactManageGroupViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactManageGroupViewState,
            newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactManageGroupViewState,
            newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64
        }
    }

    private fun itemSelected(
        id: Int,
        image: CircularImageView,
        contact: ContactManageGroupViewState
    ) {
        if (listOfItemSelected.contains(id.toString())) {
            image.setImageResource(R.drawable.ic_item_selected)
        } else {
            if (contact.profilePicture64 != "") {
                val bitmap = Converter.base64ToBitmap(contact.profilePicture64)
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(
                    RandomDefaultImage.randomDefaultImage(
                        contact.profilePicture, cxt
                    )
                )
            }
        }
    }

    fun itemDeselected() {
        listOfItemSelected.clear()
    }
}