package com.yellowtwigs.knockin.ui.groups.manage_group

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactListBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactManageGroupListAdapter(
    private val cxt: Context, private val listOfItemSelected: ArrayList<String>, private val onClickedCallback: (String) -> Unit
) : ListAdapter<ContactManageGroupViewState, ContactManageGroupListAdapter.ViewHolder>(
    ContactManageGroupViewStateComparator()
) {

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

    inner class ViewHolder(private val binding: ItemContactListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactManageGroupViewState) {
            binding.apply {
                InitContactsForListAdapter.InitContactAdapter.contactProfilePicture(
                    contact.profilePicture64, contact.profilePicture, civ, cxt
                )

                name.text = contact.firstName + " " + contact.lastName

                if (contact.firstName.isBlank() || contact.firstName.isEmpty()) {
                    itemSelected(contact.id, contact.lastName, civ, contact)
                } else if (contact.lastName.isBlank() || contact.lastName.isEmpty()) {
                    itemSelected(contact.id, contact.firstName, civ, contact)
                } else {
                    itemSelected(contact.id, name.text.toString(), civ, contact)
                }

                root.setOnClickListener {
                    onClickedCallback(contact.id.toString())
                    itemSelected(contact.id, name.text.toString(), civ, contact)
                }
            }
        }
    }

    class ContactManageGroupViewStateComparator : DiffUtil.ItemCallback<ContactManageGroupViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactManageGroupViewState, newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactManageGroupViewState, newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName && oldItem.lastName == newItem.lastName && oldItem.profilePicture == newItem.profilePicture && oldItem.profilePicture64 == newItem.profilePicture64
        }
    }

    private fun itemSelected(id: Int, name: String, image: CircularImageView, contact: ContactManageGroupViewState) {
        if (listOfItemSelected.contains(id.toString()) || listOfItemSelected.contains(name)) {
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