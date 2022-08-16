package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.databinding.MultiSelectItemBinding
import com.yellowtwigs.knockin.model.ContactsDatabase
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListViewState
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.spanNameTextView
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage

class FirstVipSelectionAdapter(
    private val cxt: Activity,
    private val contactUnlimited: Boolean,
    private val onClicked: (Int) -> Unit
) :
    ListAdapter<ContactsListViewState, FirstVipSelectionAdapter.ViewHolder>(
        ContactWithAllInformationComparator()
    ) {

    val listContactSelect = arrayListOf<ContactsListViewState>()
    val contactsDatabase = ContactsDatabase.getDatabase(cxt)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            MultiSelectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact, position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: MultiSelectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactsListViewState, position: Int) {
            binding.apply {
                if (contact != null)
                    spanNameTextView(
                        contact.firstName,
                        contact.lastName,
                        0.9f,
                        contactFirstName,
                        contactLastName
                    )
                if (contact?.profilePicture64 != "") {
                    val bitmap = contact?.profilePicture64?.let { base64ToBitmap(it) }
                    contactImage.setImageBitmap(bitmap)
                } else {
                    contactImage.setImageResource(randomDefaultImage(contact.profilePicture, cxt))
                }

//                root.setOnClickListener {
//                    onClicked(position)
//
//                    if (cxt is Main2Activity || cxt is GroupManagerActivity) {
//                        if (listContactSelect.contains(contactWithAllInformation)) {
//                            contactImage.setImageResource(R.drawable.ic_item_selected)
//                        } else {
//                            if (contact?.profilePicture64 != "") {
//                                val bitmap = contact?.profilePicture64?.let { base64ToBitmap(it) }
//                                contactImage.setImageBitmap(bitmap)
//                            } else {
//                                contactImage.setImageResource(
//                                    randomDefaultImage(
//                                        contact.profilePicture,
//                                        cxt
//                                    )
//                                )
//                            }
//                        }
//                    } else {
//                        if (listContactSelect.contains(contactWithAllInformation)) {
//                            setBorderColor(contactImage, R.color.priorityTwoColor)
//                        } else {
//                            setBorderColor(contactImage, R.color.lightColor)
//                        }
//                    }
//                }

//                if (contact?.contactPriority == 2) {
//                    setBorderColor(contactImage, R.color.priorityTwoColor)
//                }
            }
        }

        private fun setBorderColor(cv: CircularImageView, res: Int) {
            cv.setBorderColor(ResourcesCompat.getColor(cxt.resources, res, null))
        }
    }

    class ContactWithAllInformationComparator : DiffUtil.ItemCallback<ContactsListViewState>() {
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
            return oldItem.id == newItem.id
        }
    }

    fun itemSelected(position: Int) {
        val contact = getItem(position)

        if (listContactSelect.contains(contact)) {
            listContactSelect.remove(contact)
//            contact.setPriority(contactsDatabase, 1)
        } else {
            if (listContactSelect.size < 5 || contactUnlimited) {
                listContactSelect.add(contact)
            }
        }
    }

    fun itemDeselected() {
        listContactSelect.clear()
    }
}