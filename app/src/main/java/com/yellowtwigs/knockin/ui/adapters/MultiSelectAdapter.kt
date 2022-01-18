package com.yellowtwigs.knockin.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.databinding.MultiSelectItemBinding
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.ConvertBitmap.base64ToBitmap
import com.yellowtwigs.knockin.utils.InitContactAdapter.initContact
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.util.*

class MultiSelectAdapter(
    private val cxt: Activity, private val len: Int,
    private val onClicked: (Int) -> Unit
) :
    ListAdapter<ContactWithAllInformation, MultiSelectAdapter.ViewHolder>(
        ContactWithAllInformationComparator()
    ) {

    val listContactSelect: ArrayList<ContactWithAllInformation> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            MultiSelectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact, position)
    }

    inner class ViewHolder(private val binding: MultiSelectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contactWithAllInformation: ContactWithAllInformation, position: Int) {
            binding.apply {
                val contact = contactWithAllInformation.contactDB
                if (contact != null)
                    initContact(contact, contactFirstName, contactLastName, contactImage, len)

                if (contact?.profilePicture64 != "") {
                    val bitmap = contact?.profilePicture64?.let { base64ToBitmap(it) }
                    contactImage.setImageBitmap(bitmap)
                } else {
                    contactImage.setImageResource(randomDefaultImage(contact.profilePicture, cxt))
                }

                if (cxt is MainActivity || cxt is GroupManagerActivity) {
                    if (listContactSelect.contains(contactWithAllInformation)) {
                        contactImage.setImageResource(R.drawable.ic_item_selected)
                    } else {
                        if (contact?.profilePicture64 != "") {
                            val bitmap = contact?.profilePicture64?.let { base64ToBitmap(it) }
                            contactImage.setImageBitmap(bitmap)
                        } else {
                            contactImage.setImageResource(
                                randomDefaultImage(
                                    contact.profilePicture,
                                    cxt
                                )
                            )
                        }
                    }
                } else {
                    if (listContactSelect.contains(contactWithAllInformation)) {
                        contactImage.setBorderColor(
                            ResourcesCompat.getColor(
                                cxt.resources,
                                R.color.priorityTwoColor,
                                null
                            )
                        )
                    } else {
                        contactImage.setBorderColor(
                            ResourcesCompat.getColor(
                                cxt.resources,
                                R.color.lightColor,
                                null
                            )
                        )
                    }
                }

                root.setOnClickListener {
                    onClicked(position)
                }
            }
        }
    }

    class ContactWithAllInformationComparator : DiffUtil.ItemCallback<ContactWithAllInformation>() {
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
                    oldItem.contactDetailList == newItem.contactDetailList
        }
    }

    fun itemSelected(position: Int) {
        val contact = getItem(position)
        if (listContactSelect.contains(contact)) {
            listContactSelect.remove(contact)
        } else {
            if (listContactSelect.size <= 5) {
                listContactSelect.add(contact)
            }
        }
    }

    fun itemDeselected() {
        listContactSelect.clear()
    }
}