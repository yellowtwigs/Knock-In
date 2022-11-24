package com.yellowtwigs.knockin.ui.groups.manage_group

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
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactManageGroupGripAdapter(
    private val cxt: Context,
    private val listOfItemSelected: ArrayList<String>,
    private val onClickedCallback: (String) -> Unit
) :
    ListAdapter<ContactManageGroupViewState, ContactManageGroupGripAdapter.ViewHolder>(
        ContactManageGroupViewStateComparator()
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

        fun onBind(contact: ContactManageGroupViewState) {
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