package com.yellowtwigs.knockin.ui.groups.list

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class GroupsGridAdapter(private val cxt: Context, private val onClickedCallback: (Int) -> Unit) :
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
                var firstname = contact.firstName
                var lastName = contact.lastName

                val len = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
                    .getInt("gridview", 4)

                val layoutParamsTV = name.layoutParams as ConstraintLayout.LayoutParams
                val layoutParamsIV = civ.layoutParams as ConstraintLayout.LayoutParams

                if (len == 4) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.25).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.25).toInt()
                    layoutParamsTV.topMargin = 10
                    layoutParamsIV.topMargin = 10

                    if (contact.firstName.length > 12)
                        firstname = contact.firstName.substring(0, 10) + ".."

                    if (contact.lastName.length > 12)
                        lastName = contact.lastName.substring(0, 10) + ".."

                    val size = "$firstname $lastName"
                    val span = SpannableString("$firstname $lastName")
                    span.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        size.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    name.text = span
                } else if (len == 5) {
                    civ.layoutParams.height = (imageHeight - imageHeight * 0.40).toInt()
                    civ.layoutParams.width = (imageHeight - imageHeight * 0.40).toInt()
                    layoutParamsTV.topMargin = 0
                    layoutParamsIV.topMargin = 0

                    if (contact.firstName.length > 11)
                        firstname = contact.firstName.substring(0, 9) + ".."

                    if (contact.lastName.length > 11)
                        lastName = contact.lastName.substring(0, 9) + ".."

                    val size = "$firstname $lastName"
                    val span = SpannableString("$firstname $lastName")
                    span.setSpan(
                        RelativeSizeSpan(0.9f),
                        0,
                        size.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    name.text = span
                }

                name.text = contact.firstName + " " + contact.lastName

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