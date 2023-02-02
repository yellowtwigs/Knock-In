package com.yellowtwigs.knockin.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage

object InitContactsForListAdapter {

    object InitContactAdapter {
        fun initContact(
            contact: ContactDB, contactFirstName: TextView, contactLastName: TextView, contactImage: ImageView, len: Int
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
                            firstName, lastName, 0.95f, contactFirstName, contactLastName
                        )
                    }
                    4 -> {
                        height -= (h * 0.25).toInt()
                        width -= (w * 0.25).toInt()
                        paramsTV.topMargin = 10
                        paramsIV.topMargin = 10
                        if (firstName.length > 12) firstName = firstName.substring(0, 10) + ".."

                        if (lastName.length > 12) lastName = lastName.substring(0, 10) + ".."

                        spanNameTextView(
                            firstName, lastName, 0.95f, contactFirstName, contactLastName
                        )
                    }
                    5 -> {
                        height -= (h * 0.40).toInt()
                        width -= (w * 0.40).toInt()
                        paramsTV.topMargin = 0
                        paramsIV.topMargin = 0

                        if (firstName.length > 11) firstName = firstName.substring(0, 9) + ".."

                        if (lastName.length > 11) lastName = lastName.substring(0, 9) + ".."

                        spanNameTextView(
                            firstName, lastName, 0.9f, contactFirstName, contactLastName
                        )
                    }
                }
            }
        }

        fun spanNameTextView(
            firstName: String, lastName: String, proportion: Float, contactFirstName: TextView, contactLastName: TextView
        ) {
            val spanFistName = SpannableString(firstName)
            val spanLastName = SpannableString(lastName)

            spanFistName.setSpan(
                RelativeSizeSpan(proportion), 0, firstName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spanLastName.setSpan(
                RelativeSizeSpan(proportion), 0, lastName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            contactFirstName.text = spanFistName
            contactLastName.text = spanLastName
        }

        fun contactPriorityBorder(priority: Int, civ: CircularImageView, cxt: Context) {
            when (priority) {
                0 -> {
                    civ.setBorderColor(
                        ResourcesCompat.getColor(cxt.resources, R.color.priorityZeroColor, null)
                    )
                }
                1 -> {
                    civ.setBorderColor(
                        ResourcesCompat.getColor(cxt.resources, R.color.transparentColor, null)
                    )
                }
                2 -> {
                    civ.setBorderColor(
                        ResourcesCompat.getColor(cxt.resources, R.color.priorityTwoColor, null)
                    )
                }
            }
        }

        fun contactProfilePicture(
            picture64: String, picture: Int, civ: CircularImageView, cxt: Context
        ) {
            if (picture64 != "") {
                val bitmap = Converter.base64ToBitmap(picture64)
                civ.setImageBitmap(bitmap)
            } else {
                civ.setImageResource(randomDefaultImage(picture, cxt))
            }
        }

        fun initContactNameFromGrid(
            firstName: String, lastName: String, len: Int, firstNameTv: TextView, lastNameTv: TextView, civ: CircularImageView, imageHeight: Int
        ) {
            var firstnameData = firstName
            var lastnameData = lastName

            val layoutParamsTV = firstNameTv.layoutParams as ConstraintLayout.LayoutParams
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

                        firstNameTv.text = spanFirstName
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

                        lastNameTv.text = spanLastName
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

                        firstNameTv.text = spanFirstName
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

                        lastNameTv.text = spanLastName
                    } catch (e: IndexOutOfBoundsException) {
                        Log.i("GetContacts", "$e")
                    }
                }
            }
        }
    }
}