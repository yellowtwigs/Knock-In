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
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactSelectedLayoutBinding
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.utils.Converter

class ContactSelectedListAdapter(private val context: Context) :
    ListAdapter<ContactDB, ContactSelectedListAdapter.ViewHolder>(
        ContactComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContactSelectedLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), context)
    }

    inner class ViewHolder(private val binding: ItemContactSelectedLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactDB, context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val nbGrid = sharedPreferences.getInt("gridview", 1)
            contact.let {
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
                        contactRoundedImageView.setImageResource(randomDefaultImage(it.profilePicture))
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

                    contactRoundedImageView.setBorderColor(
                        context.resources.getColor(
                            R.color.transparentColor,
                            null
                        )
                    )

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
                        if (contact.lastName.length > 11) lastName =
                            contact.lastName.substring(0, 9) + ".."
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
                        if (contact.lastName.length > 12) lastName =
                            contact.lastName.substring(0, 10) + ".."
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

    class ContactComparator : DiffUtil.ItemCallback<ContactDB>() {
        override fun areItemsTheSame(
            oldItem: ContactDB,
            newItem: ContactDB
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactDB,
            newItem: ContactDB
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    /**
     * Renvoie l'image du contact sous forme de ressource
     *
     * @param avatarId [Int]
     * @return [Int]
     */
    private fun randomDefaultImage(avatarId: Int): Int {
        val sharedPreferencesIsMultiColor =
            context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("IsMultiColor", 0)
        val sharedPreferencesContactsColor =
            context.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColorPosition = sharedPreferencesContactsColor.getInt("contactsColor", 0)
        return if (multiColor == 0) {
            when (avatarId) {
                0 -> R.drawable.ic_user_purple
                1 -> R.drawable.ic_user_blue
                2 -> R.drawable.ic_user_cyan_teal
                3 -> R.drawable.ic_user_green
                4 -> R.drawable.ic_user_om
                5 -> R.drawable.ic_user_orange
                6 -> R.drawable.ic_user_red
                else -> R.drawable.ic_user_blue
            }
        } else {
            when (contactsColorPosition) {
                0 -> when (avatarId) {
                    0 -> R.drawable.ic_user_blue
                    1 -> R.drawable.ic_user_blue_indigo1
                    2 -> R.drawable.ic_user_blue_indigo2
                    3 -> R.drawable.ic_user_blue_indigo3
                    4 -> R.drawable.ic_user_blue_indigo4
                    5 -> R.drawable.ic_user_blue_indigo5
                    6 -> R.drawable.ic_user_blue_indigo6
                    else -> R.drawable.ic_user_om
                }
                1 -> when (avatarId) {
                    0 -> R.drawable.ic_user_green
                    1 -> R.drawable.ic_user_green_lime1
                    2 -> R.drawable.ic_user_green_lime2
                    3 -> R.drawable.ic_user_green_lime3
                    4 -> R.drawable.ic_user_green_lime4
                    5 -> R.drawable.ic_user_green_lime5
                    else -> R.drawable.ic_user_green_lime6
                }
                2 -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_purple_grape1
                    2 -> R.drawable.ic_user_purple_grape2
                    3 -> R.drawable.ic_user_purple_grape3
                    4 -> R.drawable.ic_user_purple_grape4
                    5 -> R.drawable.ic_user_purple_grape5
                    else -> R.drawable.ic_user_purple
                }
                3 -> when (avatarId) {
                    0 -> R.drawable.ic_user_red
                    1 -> R.drawable.ic_user_red1
                    2 -> R.drawable.ic_user_red2
                    3 -> R.drawable.ic_user_red3
                    4 -> R.drawable.ic_user_red4
                    5 -> R.drawable.ic_user_red5
                    else -> R.drawable.ic_user_red
                }
                4 -> when (avatarId) {
                    0 -> R.drawable.ic_user_grey
                    1 -> R.drawable.ic_user_grey1
                    2 -> R.drawable.ic_user_grey2
                    3 -> R.drawable.ic_user_grey3
                    4 -> R.drawable.ic_user_grey4
                    else -> R.drawable.ic_user_grey1
                }
                5 -> when (avatarId) {
                    0 -> R.drawable.ic_user_orange
                    1 -> R.drawable.ic_user_orange1
                    2 -> R.drawable.ic_user_orange2
                    3 -> R.drawable.ic_user_orange3
                    4 -> R.drawable.ic_user_orange4
                    else -> R.drawable.ic_user_orange3
                }
                6 -> when (avatarId) {
                    0 -> R.drawable.ic_user_cyan_teal
                    1 -> R.drawable.ic_user_cyan_teal1
                    2 -> R.drawable.ic_user_cyan_teal2
                    3 -> R.drawable.ic_user_cyan_teal3
                    4 -> R.drawable.ic_user_cyan_teal4
                    else -> R.drawable.ic_user_cyan_teal
                }
                else -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_blue
                    2 -> R.drawable.ic_user_cyan_teal
                    3 -> R.drawable.ic_user_green
                    4 -> R.drawable.ic_user_om
                    5 -> R.drawable.ic_user_orange
                    6 -> R.drawable.ic_user_red
                    else -> R.drawable.ic_user_blue
                }
            }
        }
    }
}