package com.yellowtwigs.knockin.ui.groups

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactAddToGroupBinding
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage

class AddContactToGroupAdapter(
    private val cxt: Context
) : ListAdapter<ContactWithAllInformation, AddContactToGroupAdapter.ViewHolder>(ContactComparator()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddContactToGroupAdapter.ViewHolder {
        val binding =
            ItemContactAddToGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddContactToGroupAdapter.ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact, cxt)
    }

    inner class ViewHolder(private val binding: ItemContactAddToGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactWithAllInformation, cxt: Context) {
            binding.apply {
                contactCheckbox.isChecked = (cxt as AddContactToGroupActivity).selectContact.contains(contact.contactDB)

                if (contact.contactDB?.profilePicture64 != "") {
                    val bitmap = contact.contactDB?.profilePicture64?.let { base64ToBitmap(it) }
                    contactImage.setImageBitmap(bitmap)
                } else {
                    contactImage.setImageResource(randomDefaultImage(contact.contactDB?.profilePicture!!, cxt))
                }

                var name = contact.contactDB?.firstName + " " + contact.contactDB?.lastName
                if (name.length > 15) {
                    val spanFistName: Spannable = SpannableString(name)
                    spanFistName.setSpan(
                        RelativeSizeSpan(1.0f),
                        0,
                        name.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    contactName.text = spanFistName
                    contactName.text = name.substring(0, 15) + ".."
                }

                contactCheckbox.setOnClickListener {
                    if (!cxt.selectContact.contains(contact.contactDB)) {
                        contact.contactDB?.let { it1 -> cxt.selectContact.add(it1) }
                        contactCheckbox.isChecked = true
                    } else {
                        cxt.selectContact.remove(contact.contactDB)
                        contactCheckbox.isChecked = false
                    }
                }
            }
        }
    }

    class ContactComparator : DiffUtil.ItemCallback<ContactWithAllInformation>() {
        override fun areItemsTheSame(
            oldItem: ContactWithAllInformation,
            newItem: ContactWithAllInformation
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactWithAllInformation,
            newItem: ContactWithAllInformation
        ): Boolean {
            return oldItem.contactDB == newItem.contactDB &&
                    oldItem.contactDetailList == newItem.contactDetailList &&
                    oldItem.groupList == newItem.groupList &&
                    oldItem.getContactId() == newItem.getContactId()
        }
    }
}