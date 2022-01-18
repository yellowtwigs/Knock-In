package com.yellowtwigs.knockin.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.controller.CircularImageView
import com.yellowtwigs.knockin.controller.activity.EditContactActivity
import com.yellowtwigs.knockin.controller.activity.MainActivity
import com.yellowtwigs.knockin.controller.activity.group.GroupManagerActivity
import com.yellowtwigs.knockin.databinding.GridContactItemLayoutBinding
import com.yellowtwigs.knockin.databinding.ListContactItemLayoutBinding
import com.yellowtwigs.knockin.model.ModelDB.ContactDB
import com.yellowtwigs.knockin.model.ModelDB.ContactWithAllInformation
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import de.hdodenhof.circleimageview.CircleImageView

object InitContactAdapter {

    fun initContact(
        contact: ContactDB,
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
                    spanNameTextView(firstName, lastName, 0.95f, contactFirstName, contactLastName)
                }
                3 -> {
                    height -= (h * 0.05).toInt()
                    width -= (w * 0.05).toInt()
                    paramsTV.topMargin = 30
                    paramsIV.topMargin = 10
                    spanNameTextView(firstName, lastName, 0.95f, contactFirstName, contactLastName)
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

                    spanNameTextView(firstName, lastName, 0.95f, contactFirstName, contactLastName)
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

                    spanNameTextView(firstName, lastName, 0.9f, contactFirstName, contactLastName)
                }
                6 -> {
                    height -= (h * 0.50).toInt()
                    width -= (w * 0.50).toInt()
                    paramsTV.topMargin = 0
                    paramsIV.topMargin = 0

                    if (firstName.length > 8)
                        firstName = firstName.substring(0, 7) + ".."

                    if (lastName.length > 8)
                        lastName = lastName.substring(0, 7) + ".."

                    spanNameTextView(firstName, lastName, 0.81f, contactFirstName, contactLastName)
                }
            }
        }
    }

    private fun spanNameTextView(
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

    fun contactPriorityBorder(contact: ContactDB, civ: CircularImageView, cxt: Context) {
        when (contact.contactPriority) {
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

    @SuppressLint("ResourceType")
    fun initMenuButton(
        civ: CircularImageView,
        position: Int,
        contact: ContactWithAllInformation,
        cxt: Context,
        len: Int,
        listCircularMenu: ArrayList<FloatingActionMenu>,
        listOfItemSelected: ArrayList<ContactWithAllInformation>,
        selectMenu: FloatingActionMenu,
        multiSelectBoolean: Boolean,
        binding: ViewBinding,
        menuStateChangeListener: FloatingActionMenu.MenuStateChangeListener
    ) {
        var modeMultiSelect = multiSelectBoolean

        val buttonCall = ImageView(cxt)
        val buttonWhatsApp = ImageView(cxt)
        val buttonSMS = ImageView(cxt)
        val buttonEdit = ImageView(cxt)
        val buttonMail = ImageView(cxt)
        buttonCall.id = 1
        buttonSMS.id = 2
        buttonWhatsApp.id = 3
        buttonEdit.id = 4
        buttonMail.id = 5
        buttonCall.setImageResource(R.drawable.ic_google_call)
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp)
        buttonSMS.setImageResource(R.drawable.ic_sms_selector)
        buttonEdit.setImageResource(R.drawable.ic_circular_edit)
        buttonMail.setImageResource(R.drawable.ic_circular_mail)

        val builderIcon = SubActionButton.Builder(cxt as Activity)
        builderIcon.setBackgroundDrawable(
            ResourcesCompat.getDrawable(cxt.resources, R.drawable.ic_circular, null)
        )
        builderIcon.setContentView(buttonCall)
        val startAngle: Int
        val endAngle: Int
        when {
            position % len == 0 -> {
                startAngle = 90
                endAngle = -90
            }
            position % len == len - 1 -> {
                startAngle = 90
                endAngle = 270
            }
            else -> {
                startAngle = 0
                endAngle = -180
            }
        }

        val metrics = DisplayMetrics()
        cxt.windowManager.defaultDisplay.getMetrics(metrics)
        val diameterButton = (0.38 * metrics.densityDpi).toInt()
        val radiusMenu = (0.50 * metrics.densityDpi).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(5, 5, 5, 5)
        val builder = FloatingActionMenu.Builder(cxt)
            .setStartAngle(startAngle)
            .setEndAngle(endAngle)
            .setRadius(radiusMenu)
            .addSubActionView(
                builderIcon.setContentView(buttonEdit, layoutParams).build(),
                diameterButton,
                diameterButton
            )
            .attachTo(civ)
            .setStateChangeListener(menuStateChangeListener)
            .disableAnimations()

        if (isWhatsappInstalled(cxt) && contact.getFirstPhoneNumber() != "" && contact.contactDB?.hasWhatsapp == 1) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonWhatsApp, layoutParams).build(),
                diameterButton,
                diameterButton
            )
        }
        if (contact.getFirstMail() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonMail, layoutParams).build(),
                diameterButton,
                diameterButton
            )
        }
        if (contact.getFirstPhoneNumber() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonSMS, layoutParams).build(),
                diameterButton,
                diameterButton
            )
                .addSubActionView(
                    builderIcon.setContentView(buttonCall, layoutParams).build(),
                    diameterButton,
                    diameterButton
                )
        }
        val quickMenu = builder.build()
        listCircularMenu.add(quickMenu)

        val buttonListener = View.OnClickListener { v: View ->
            when (v.id) {
                buttonWhatsApp.id -> {
                    ContactGesture.openWhatsapp(
                        ConverterPhoneNumber.converter06To33(contact.getFirstPhoneNumber()),
                        cxt
                    )
                }
                buttonEdit.id -> {
                    val intent = Intent(cxt, EditContactActivity::class.java)
                    intent.putExtra("ContactId", contact.contactDB?.id)
                    intent.putExtra("position", position)
                    if (cxt is GroupManagerActivity) {
                        intent.putExtra("fromGroupActivity", true)
                    }
                    cxt.startActivity(intent)
                }
                buttonCall.id -> {
                    ContactGesture.callPhone(contact.getFirstPhoneNumber(), cxt)
                }
                buttonSMS.id -> {
                    val phone = contact.getFirstPhoneNumber()
                    val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                    cxt.startActivity(i)
                }
                buttonMail.id -> {
                    val mail = contact.getFirstMail()
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "")
                    intent.putExtra(Intent.EXTRA_TEXT, "")
                    cxt.startActivity(intent)
                }
            }
            selectMenu.close(false)
        }

        val gridLongClick = View.OnLongClickListener { v: View? ->
            if (!modeMultiSelect) {
                selectMenu.close(true)
                modeMultiSelect = true
                listOfItemSelected.add(contact)
                if (cxt is MainActivity) {
                    cxt.gridMultiSelectItemClick(position)
                } else {
                    (cxt as GroupManagerActivity).gridMultiSelectItemClick(
                        len,
                        position
                    )
                }
                civ.setImageResource(R.drawable.ic_item_selected)
            }
            true
        }
        val gridItemClick = View.OnClickListener { v: View? ->
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(contact)) {
                    listOfItemSelected.remove(contact)
                    if (contact.contactDB?.profilePicture64 != "") {
                        val bitmap = contact.contactDB?.profilePicture64?.let {
                            ConvertBitmap.base64ToBitmap(
                                it
                            )
                        }
                        civ.setImageBitmap(bitmap)
                    } else {
                        contact.contactDB?.profilePicture?.let {
                            RandomDefaultImage.randomDefaultImage(
                                it, cxt
                            )
                        }?.let {
                            civ.setImageResource(it)
                        }
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false
                    }
                } else {
                    listOfItemSelected.add(contact)
                    civ.setImageResource(R.drawable.ic_item_selected)
                }
                (cxt as MainActivity).gridMultiSelectItemClick(position)
            } else {
                if (quickMenu.isOpen) {
                    quickMenu.close(false)
                } else {
                    quickMenu.open(false)
                }
            }
        }

        buttonCall.setOnLongClickListener { v: View? ->
            val phoneNumber = contact.getSecondPhoneNumber(contact.getFirstPhoneNumber())
            if (phoneNumber.isNotEmpty()) {
                ContactGesture.callPhone(phoneNumber, cxt)
            }
            true
        }
        binding.root.apply {
            setOnLongClickListener(gridLongClick)
            setOnClickListener(gridItemClick)
        }

        civ.apply {
            setOnLongClickListener(gridLongClick)
            setOnClickListener(gridItemClick)
        }
        buttonWhatsApp.setOnClickListener(buttonListener)
        buttonCall.setOnClickListener(buttonListener)
        buttonSMS.setOnClickListener(buttonListener)
        buttonEdit.setOnClickListener(buttonListener)
        buttonMail.setOnClickListener(buttonListener)
    }
}