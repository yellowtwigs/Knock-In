package com.yellowtwigs.knockin.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.models.data.Contact

object SetupContactsList {
    fun initContact(
        contact: Contact,
        contactFirstName: TextView,
        contactLastName: TextView,
        contactImage: ImageView,
        len: Int
    ) {
        val h = contactImage.layoutParams.height
        val w = contactImage.layoutParams.width

        var firstName = contact.firstName
        var lastName = contact.lastName

        if (firstName.isEmpty()) {
            contactFirstName.visibility = View.GONE
        }

        val paramsTV = contactFirstName.layoutParams as RelativeLayout.LayoutParams
        val paramsIV = contactImage.layoutParams as ConstraintLayout.LayoutParams

        contactImage.layoutParams.apply {
            when (len) {
                1 -> {
                    height -= (h * 0.05).toInt()
                    width -= (w * 0.05).toInt()
                    paramsTV.topMargin = 30
                    paramsIV.topMargin = 10
                    spanNameTextView(
                        firstName,
                        lastName,
                        0.95f,
                        contactFirstName,
                        contactLastName
                    )
                }
                4 -> {
                    height -= (h * 0.25).toInt()
                    width -= (w * 0.25).toInt()
                    paramsTV.topMargin = 10
                    paramsIV.topMargin = 10
                    if (firstName.length > 12)
                        firstName = firstName.substring(0, 10) + ".."

                    if (lastName.length > 12)
                        lastName = lastName.substring(0, 10) + ".."

                    spanNameTextView(
                        firstName,
                        lastName,
                        0.95f,
                        contactFirstName,
                        contactLastName
                    )
                }
                5 -> {
                    height -= (h * 0.40).toInt()
                    width -= (w * 0.40).toInt()
                    paramsTV.topMargin = 0
                    paramsIV.topMargin = 0

                    if (firstName.length > 11)
                        firstName = firstName.substring(0, 9) + ".."

                    if (lastName.length > 11)
                        lastName = lastName.substring(0, 9) + ".."

                    spanNameTextView(
                        firstName,
                        lastName,
                        0.9f,
                        contactFirstName,
                        contactLastName
                    )
                }
            }
        }
    }

    fun spanNameTextView(
        firstName: String,
        lastName: String,
        proportion: Float,
        contactFirstName: TextView,
        contactLastName: TextView
    ) {
        val spanFistName = SpannableString(firstName)
        val spanLastName = SpannableString(lastName)

        spanFistName.setSpan(
            RelativeSizeSpan(proportion), 0, firstName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanLastName.setSpan(
            RelativeSizeSpan(proportion), 0, lastName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        contactFirstName.text = spanFistName
        contactLastName.text = spanLastName
    }

    fun contactPriorityBorder(priority: Int, civ: CircularImageView, cxt: Context) {
        when (priority) {
            0 -> {
                setBorderContactImage(R.color.priorityZeroColor, civ, cxt)
            }
            1 -> {
                setBorderContactImage(R.color.transparentColor, civ, cxt)
            }
            2 -> {
                setBorderContactImage(R.color.priorityTwoColor, civ, cxt)
            }
        }
    }

    private fun setBorderContactImage(id: Int, civ: CircularImageView, cxt: Context) {
        civ.setBorderColor(ResourcesCompat.getColor(cxt.resources, id, null))
    }

    private fun randomDefaultImage(avatarId: Int, context: Context): Int {
        val sharedPreferencesIsMultiColor =
            context.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0)
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