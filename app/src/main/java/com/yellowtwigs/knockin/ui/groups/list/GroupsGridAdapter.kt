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
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class GroupsGridAdapter(
    private val cxt: Context,
    private val isMultiSelect: Boolean,
    private val onClickedCallback: (Int) -> Unit
) :
    ListAdapter<ContactInGroupViewState, GroupsGridAdapter.ViewHolder>(
        GroupsListViewStateComparator()
    ) {

    private var modeMultiSelect = false
    private var isScrolling = false
    var listOfItemSelected = ArrayList<ContactInGroupViewState>()

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

        fun onBind(contact: ContactInGroupViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)
                var firstnameData = contact.firstName
                var lastnameData = contact.lastName

                val len = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    .getInt("gridview", 4)

                val layoutParamsTV = firstName.layoutParams as ConstraintLayout.LayoutParams
                val layoutParamsIV = civ.layoutParams as ConstraintLayout.LayoutParams

                if (len == 4) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.25).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.25).toInt()
                    layoutParamsTV.topMargin = 10
                    layoutParamsIV.topMargin = 10

                    if (firstnameData.isNotEmpty() || firstnameData.isNotBlank() || firstnameData != " ") {
                        if (firstnameData.length > 12) firstnameData = firstnameData.substring(0, 10) + ".."

                        val sizeFirstName = "$firstnameData"
                        val spanFirstName = SpannableString("$firstnameData")

                        try {
                            spanFirstName.setSpan(
                                RelativeSizeSpan(0.9f), 0, sizeFirstName.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            firstName.text = spanFirstName
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("GetContacts", "$e")
                        }
                    }

                    if (lastnameData.isNotEmpty() || lastnameData.isNotBlank() || lastnameData != " ") {
                        if (lastnameData.length > 12) lastnameData = lastnameData.substring(0, 10) + ".."

                        val sizeLastName = "$lastnameData"
                        val spanLastName = SpannableString("$lastnameData")

                        try {
                            spanLastName.setSpan(
                                RelativeSizeSpan(0.9f), 0, sizeLastName.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            lastName.text = spanLastName
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("GetContacts", "$e")
                        }
                    }
                } else if (len == 5) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.40).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.40).toInt()
                    layoutParamsTV.topMargin = 0
                    layoutParamsIV.topMargin = 0

                    if (firstnameData.isNotEmpty() || firstnameData.isNotBlank() || firstnameData != " ") {
                        if (firstnameData.length > 11) firstnameData = firstnameData.substring(0, 9) + ".."

                        val sizeFirstName = "$firstnameData"
                        val spanFirstName = SpannableString("$firstnameData")

                        try {
                            spanFirstName.setSpan(
                                RelativeSizeSpan(0.9f), 0, sizeFirstName.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            firstName.text = spanFirstName
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("GetContacts", "$e")
                        }
                    }

                    if (lastnameData.isNotEmpty() || lastnameData.isNotBlank() || lastnameData != " ") {
                        if (lastnameData.length > 12) lastnameData = lastnameData.substring(0, 10) + ".."

                        val sizeLastName = "$lastnameData"
                        val spanLastName = SpannableString("$lastnameData")

                        try {
                            spanLastName.setSpan(
                                RelativeSizeSpan(0.9f), 0, sizeLastName.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            lastName.text = spanLastName
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("GetContacts", "$e")
                        }
                    }
                }

                root.setOnClickListener {
                    onClickedCallback(contact.id)
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
                    oldItem.listOfPhoneNumbers == newItem.listOfPhoneNumbers &&
                    oldItem.listOfMails == newItem.listOfMails
        }
    }
}