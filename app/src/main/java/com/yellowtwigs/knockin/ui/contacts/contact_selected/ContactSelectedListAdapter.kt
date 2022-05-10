package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.GridContactItemLayoutBinding
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactSelectedListAdapter(private val context: Context) :
    ListAdapter<ContactWithAllInformation, ContactSelectedListAdapter.ViewHolder>(
        ContactComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            GridContactItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), context)
    }

    inner class ViewHolder(private val binding: GridContactItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contactInfos: ContactWithAllInformation, context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val nbGrid = sharedPreferences.getInt("gridview", 1)
            val contact = contactInfos.contactDB
            contact?.let {
                binding.apply {
                    val layoutParamsTV =
                        gridAdapterContactFirstName.layoutParams as RelativeLayout.LayoutParams
                    val layoutParamsIV =
                        contactRoundedImageView.layoutParams as ConstraintLayout.LayoutParams
                    layoutParamsTV.topMargin = 10
                    layoutParamsIV.topMargin = 10

                    if (it.profilePicture64 != "") {
                        val bitmap = Converter.base64ToBitmap(it.profilePicture64)
                        contactRoundedImageView.setImageBitmap(bitmap)
                    } else {
                        contactRoundedImageView.setImageResource(
                            RandomDefaultImage.randomDefaultImage(
                                it.profilePicture,
                                context
                            )
                        )
                    }

                    val heightWidthImage = contactRoundedImageView.layoutParams.height

                    if (nbGrid == 4) {
                        contactRoundedImageView.layoutParams.height =
                            (heightWidthImage - heightWidthImage * 0.25).toInt()
                        contactRoundedImageView.layoutParams.width =
                            (heightWidthImage - heightWidthImage * 0.25).toInt()
                        layoutParamsTV.topMargin = 10
                        layoutParamsIV.topMargin = 10
                    } else {
                        contactRoundedImageView.layoutParams.height =
                            (heightWidthImage - heightWidthImage * 0.40).toInt()
                        contactRoundedImageView.layoutParams.width =
                            (heightWidthImage - heightWidthImage * 0.40).toInt()
                        layoutParamsTV.topMargin = 0
                        layoutParamsIV.topMargin = 0
                    }

                    var firstname = contact.firstName
                    var lastName = contact.lastName

                    if (nbGrid == 5) {
                        if (contact.firstName.length > 11) firstname =
                            contact.firstName.substring(0, 9) + ".."
                        gridAdapterContactFirstName.text = firstname
                        val span: Spannable = SpannableString(gridAdapterContactFirstName.text)
                        span.setSpan(
                            RelativeSizeSpan(0.9f),
                            0,
                            firstname.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        gridAdapterContactFirstName.text = span
                        if (contact.lastName.length > 11) lastName = contact.lastName.substring(0, 9) + ".."
                        val spanLastName: Spannable = SpannableString(lastName)
                        spanLastName.setSpan(
                            RelativeSizeSpan(0.9f),
                            0,
                            lastName.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        gridAdapterContactLastName.text = spanLastName
                    } else if (nbGrid == 4) {
                        if (contact.firstName.length > 12) firstname =
                            contact.firstName.substring(0, 10) + ".."
                        val spanFistName: Spannable = SpannableString(firstname)
                        spanFistName.setSpan(
                            RelativeSizeSpan(0.95f),
                            0,
                            firstname.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        gridAdapterContactFirstName.text = spanFistName
                        if (contact.lastName.length > 12) lastName = contact.lastName.substring(0, 10) + ".."
                        val spanLastName: Spannable = SpannableString(lastName)
                        spanLastName.setSpan(
                            RelativeSizeSpan(0.95f),
                            0,
                            lastName.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        gridAdapterContactLastName.text = spanLastName
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
            return oldItem.contactDB == newItem.contactDB
        }
    }
}