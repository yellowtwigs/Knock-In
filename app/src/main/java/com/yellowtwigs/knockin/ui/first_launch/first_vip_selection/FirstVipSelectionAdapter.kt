package com.yellowtwigs.knockin.ui.first_launch.first_vip_selection

import android.app.Activity
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemFirstVipSelectionBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage

class FirstVipSelectionAdapter(
    private val cxt: Activity,
    private val listOfItemSelected: ArrayList<Int>,
    private val onClicked: (Int, FirstVipSelectionViewState) -> Unit
) :
    ListAdapter<FirstVipSelectionViewState, FirstVipSelectionAdapter.ViewHolder>(
        ContactWithAllInformationComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFirstVipSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.onBind(contact)
    }

    inner class ViewHolder(private val binding: ItemFirstVipSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: FirstVipSelectionViewState) {
            binding.apply {
                val spanFistName = SpannableString(contact.firstName)
                val spanLastName = SpannableString(contact.lastName)

                spanFistName.setSpan(
                    RelativeSizeSpan(0.9f), 0, contact.firstName.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spanLastName.setSpan(
                    RelativeSizeSpan(0.9f), 0, contact.lastName.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                name.text = "$spanFistName $spanLastName"

                if (contact.profilePicture64 != "") {
                    val bitmap = base64ToBitmap(contact.profilePicture64)
                    contactImage.setImageBitmap(bitmap)
                } else {
                    contactImage.setImageResource(randomDefaultImage(contact.profilePicture, cxt))
                }

                root.setOnClickListener {
                    onClicked(contact.id, contact)
                    itemSelected(contact.id, contactImage, contact)
                }

                if (contact.priority == 2) {
                    listOfItemSelected.add(contact.id)
                    itemSelected(contact.id, contactImage, contact)
                }
            }
        }
    }

    class ContactWithAllInformationComparator :
        DiffUtil.ItemCallback<FirstVipSelectionViewState>() {
        override fun areItemsTheSame(
            oldItem: FirstVipSelectionViewState,
            newItem: FirstVipSelectionViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: FirstVipSelectionViewState,
            newItem: FirstVipSelectionViewState
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64 &&
                    oldItem.priority == newItem.priority
        }
    }

    private fun itemSelected(
        id: Int,
        image: CircularImageView,
        contact: FirstVipSelectionViewState
    ) {
        if (listOfItemSelected.contains(id)) {
            image.setImageResource(R.drawable.ic_item_selected)
        } else {
            if (contact.profilePicture64 != "") {
                val bitmap = base64ToBitmap(contact.profilePicture64)
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(
                    randomDefaultImage(
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