package com.yellowtwigs.knockin.ui.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu.MenuStateChangeListener
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.contacts.contact_selected.ContactSelectedWithAppsActivity
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.group.GroupManagerActivity
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter.base64ToBitmap
import com.yellowtwigs.knockin.utils.Converter.converter06To33
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList

/**
 * La Classe qui permet de remplir la convertView avec les bon éléments
 *
 * @author Florian Striebel, Kenzy Suon, Ryan Granet
 */
class ContactGridViewAdapter(
    private val context: Context,
    private var contactManager: ContactManager,
    private val len: Int
) : RecyclerView.Adapter<ContactGridViewAdapter.ViewHolder>(), MenuStateChangeListener {
    private val listCircularMenu = ArrayList<FloatingActionMenu>()
    var selectMenu: FloatingActionMenu? = null
        private set
    var phonePermission = ""
        private set
    private var modeMultiSelect = false
    private var heightWidthImage = 0
    private lateinit var listApp: ArrayList<String>

    var listOfItemSelected = ArrayList<ContactWithAllInformation>()

    fun setContactManager(contactManager: ContactManager) {
        this.contactManager = contactManager
    }

    fun getItem(position: Int): ContactWithAllInformation {
        return contactManager.contactList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        listApp = getAppOnPhone(context as MainActivity)
        val view =
            LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false)
        val holder = ViewHolder(view)
        heightWidthImage = holder.contactRoundedImageView.layoutParams.height
        return holder
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactManager.contactList[position].contactDB
        val layoutParamsTV = holder.contactFirstNameView.layoutParams as RelativeLayout.LayoutParams
        val layoutParamsIV =
            holder.contactRoundedImageView.layoutParams as ConstraintLayout.LayoutParams
        if (!modeMultiSelect || !listOfItemSelected.contains(contactManager.contactList[position])) {
            assert(contact != null)
            if (contact!!.profilePicture64 != "") {
                val bitmap = base64ToBitmap(contact.profilePicture64)
                holder.contactRoundedImageView.setImageBitmap(bitmap)
            } else {
                holder.contactRoundedImageView.setImageResource(
                    randomDefaultImage(
                        contact.profilePicture,
                        context
                    )
                )
            }
        } else {
            holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
        }
        if (len == 4) {
            holder.contactRoundedImageView.layoutParams.height =
                (heightWidthImage - heightWidthImage * 0.25).toInt()
            holder.contactRoundedImageView.layoutParams.width =
                (heightWidthImage - heightWidthImage * 0.25).toInt()
            layoutParamsTV.topMargin = 10
            layoutParamsIV.topMargin = 10
        } else if (len == 5) {
            holder.contactRoundedImageView.layoutParams.height =
                (heightWidthImage - heightWidthImage * 0.40).toInt()
            holder.contactRoundedImageView.layoutParams.width =
                (heightWidthImage - heightWidthImage * 0.40).toInt()
            layoutParamsTV.topMargin = 0
            layoutParamsIV.topMargin = 0
        }
        assert(contact != null)
        when (contact?.contactPriority) {
            0 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.priorityZeroColor,
                        null
                    )
                )
            }
            1 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.transparentColor,
                        null
                    )
                )
            }
            2 -> {
                holder.contactRoundedImageView.setBorderColor(
                    context.resources.getColor(
                        R.color.priorityTwoColor,
                        null
                    )
                )
            }
        }
        var firstname = contact?.firstName
        var lastName = contact?.lastName
        if (len == 5) {
            if (contact?.firstName!!.length > 11) firstname =
                contact.firstName.substring(0, 9) + ".."
            holder.contactFirstNameView.text = firstname
            val span: Spannable = SpannableString(holder.contactFirstNameView.text)
            span.setSpan(
                RelativeSizeSpan(0.9f),
                0,
                firstname?.length!!,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactFirstNameView.text = span
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
            if (contact.lastName.length > 11) lastName = contact.lastName.substring(0, 9) + ".."
            val spanLastName: Spannable = SpannableString(lastName)
            spanLastName.setSpan(
                RelativeSizeSpan(0.9f),
                0,
                lastName!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactLastNameView.text = spanLastName
        } else if (len == 4) {
            if (contact?.firstName?.length!! > 12) firstname =
                contact.firstName.substring(0, 10) + ".."
            val spanFistName: Spannable = SpannableString(firstname)
            spanFistName.setSpan(
                RelativeSizeSpan(0.95f),
                0,
                firstname!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactFirstNameView.text = spanFistName
            if (contact.lastName.length > 12) lastName = contact.lastName.substring(0, 10) + ".."
            val spanLastName: Spannable = SpannableString(lastName)
            spanLastName.setSpan(
                RelativeSizeSpan(0.95f),
                0,
                lastName!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.contactLastNameView.text = spanLastName
        }
        if (firstname?.isEmpty() == true) {
            holder.contactFirstNameView.visibility = View.GONE
        }

        val buttonCall = ImageView(context)
        val buttonWhatsApp = ImageView(context)
        val buttonSMS = ImageView(context)
        val buttonEdit = ImageView(context)
        val buttonMail = ImageView(context)
        val buttonMessenger = ImageView(context)
        val buttonSignal = ImageView(context)

        buttonCall.id = 1
        buttonSMS.id = 2
        buttonWhatsApp.id = 3
        buttonEdit.id = 4
        buttonMail.id = 5
        buttonMessenger.id = 6
        buttonSignal.id = 7

        if (contact?.favorite == 1) {
            holder.gridAdapterFavoriteShine.visibility = View.VISIBLE
        } else {
            holder.gridAdapterFavoriteShine.visibility = View.GONE
        }

        buttonCall.setImageResource(R.drawable.ic_google_call)
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp)
        buttonSMS.setImageResource(R.drawable.ic_sms_selector)
        buttonEdit.setImageResource(R.drawable.ic_circular_edit)
        buttonMail.setImageResource(R.drawable.ic_circular_mail)
        buttonMessenger.setImageResource(R.drawable.ic_circular_messenger)
        buttonSignal.setImageResource(R.drawable.ic_circular_signal)

        val builderIcon = SubActionButton.Builder(context as Activity)
        builderIcon.setBackgroundDrawable(context.getDrawable(R.drawable.ic_circular))
        builderIcon.setContentView(buttonCall)
        val startAngle: Int
        val endAngle: Int
        val diametreButton: Int
        val radiusMenu: Int
        if (Resources.getSystem().configuration.locale.language == "ar") {
            when {
                position % len == 0 -> {
                    startAngle = 90
                    endAngle = 270
                }
                position % len == len - 1 -> {
                    startAngle = 90
                    endAngle = -90
                }
                else -> {
                    startAngle = 0
                    endAngle = -360
                }
            }
            val metrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(metrics)
            diametreButton = (0.38 * metrics.densityDpi).toInt()
            radiusMenu = (0.50 * metrics.densityDpi).toInt()
        } else {
            when {
                position % len == 0 -> {
                    startAngle = 110
                    endAngle = -110
                }
                position % len == len - 1 -> {
                    startAngle = 77
                    endAngle = 285
                }
                else -> {
                    startAngle = 0
                    endAngle = -360
                }
            }
            val metrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(metrics)
            diametreButton = (0.32 * metrics.densityDpi).toInt()
            radiusMenu = (0.50 * metrics.densityDpi).toInt()
        }
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(5, 5, 5, 5)
        val builder = FloatingActionMenu.Builder(context)
            .setStartAngle(startAngle)
            .setEndAngle(endAngle)
            .setRadius(radiusMenu)
            .addSubActionView(
                builderIcon.setContentView(buttonEdit, layoutParams).build(),
                diametreButton,
                diametreButton
            )
            .attachTo(holder.contactRoundedImageView)
            .setStateChangeListener(this)
            .disableAnimations()
        if (appIsInstalled() && getItem(position).getFirstPhoneNumber() != "" && contact?.hasWhatsapp == 1) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonWhatsApp, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }
        if (getItem(position).getFirstMail() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonMail, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }
        if (getItem(position).getFirstPhoneNumber() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonSMS, layoutParams).build(),
                diametreButton,
                diametreButton
            )
                .addSubActionView(
                    builderIcon.setContentView(buttonCall, layoutParams).build(),
                    diametreButton,
                    diametreButton
                )
        }
        if (getItem(position).getMessengerID() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonMessenger, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }
        if (listApp.contains("org.thoughtcrime.securesms")) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonSignal, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }
        /*if (!getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber()).equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreButton, diametreButton);
        }*/

        /* if( appIsInstalled( "com.facebook.orca")){
            builder.addSubActionView(builderIcon.setContentView(buttonMessenger,layoutParams).build(),diametreButton,diametreButton);
        }*/
        val quickMenu = builder.build()
        listCircularMenu.add(quickMenu)

        //quickMenu.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton);
        val buttonListener = View.OnClickListener { v: View ->
            when (v.id) {
                buttonWhatsApp.id -> {
                    val contactWithAllInformation = getItem(position)
                    openWhatsapp(
                        converter06To33(contactWithAllInformation.getFirstPhoneNumber()),
                        context
                    )
                }
                buttonEdit.id -> {
                    val intent = Intent(context, EditContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", contact!!.id)
                    intent.putExtra("position", position)
                    if (context is GroupManagerActivity) {
                        intent.putExtra("fromGroupActivity", true)
                    }
                    context.startActivity(intent)
                }
                buttonCall.id -> {
                    callPhone(getItem(position).getFirstPhoneNumber(), context)
                }
                buttonSMS.id -> {
                    val phone = getItem(position).getFirstPhoneNumber()
                    val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                    context.startActivity(i)
                }
                buttonMail.id -> {
                    val mail = getItem(position).getFirstMail()
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    //intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "")
                    intent.putExtra(Intent.EXTRA_TEXT, "")
                    context.startActivity(intent)
                }
                buttonMessenger.id -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.messenger.com/t/" + getItem(position).getMessengerID())
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                buttonSignal.id -> {
                    goToSignal(context as MainActivity)
                }
            }
            selectMenu?.close(false)
        }
        val gridlongClick = OnLongClickListener { v: View? ->
            if (!modeMultiSelect) {
                val firstPosVis: Int
                closeMenu()
                modeMultiSelect = true
                listOfItemSelected.add(contactManager.contactList[position])
                firstPosVis = 0
                if (context is MainActivity) {
                    context.gridMultiSelectItemClick(position)
                } else {
                    (context as GroupManagerActivity).gridMultiSelectItemClick(
                        len,
                        position,
                        firstPosVis
                    )
                }
                holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
            }
            true
        }
        val gridItemClick = View.OnClickListener { v: View? ->
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(contactManager.contactList[position])) {
                    listOfItemSelected.remove(contactManager.contactList[position])
                    if (contact?.profilePicture64 != "") {
                        val bitmap = base64ToBitmap(contact?.profilePicture64.toString())
                        holder.contactRoundedImageView.setImageBitmap(bitmap)
                    } else {
                        holder.contactRoundedImageView.setImageResource(
                            randomDefaultImage(
                                contact.profilePicture,
                                context
                            )
                        )
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false
                    }
                } else {
                    listOfItemSelected.add(contactManager.contactList[position])
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_item_selected)
                }
                (context as MainActivity).gridMultiSelectItemClick(position)
            } else {
                val contactGreyPreferences =
                    context.getSharedPreferences("contactGrey", Context.MODE_PRIVATE)
//                (context as MainActivity).startActivity(Intent(context, ContactSelectedWithAppsActivity::class.java).putExtra("id", getItem(position).getContactId()))
                if (quickMenu.isOpen) {
                    quickMenu.close(true)
                } else {
//                    val edit = contactGreyPreferences.edit()
//                    edit.putInt("contactGrey", position)
//                    edit.apply()
                    quickMenu.open(true)
                }

//                if (contactGreyPreferences.getInt("contactGrey", 0) == 0) {
//                    holder.gridContactItemLayout.setBackgroundColor(R.color.greyColor)
//                    holder.gridContactItemLayout.isEnabled = false
//                } else {
//                }
            }
        }
//        buttonCall.setOnLongClickListener { v: View? ->
//            val phoneNumber =
//                getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber())
//            if (phoneNumber.isNotEmpty()) {
//                callPhone(phoneNumber, context)
//            }
//            true
//        }
        holder.gridContactItemLayout.setOnLongClickListener(gridlongClick)
        holder.contactRoundedImageView.setOnLongClickListener(gridlongClick)
        holder.gridContactItemLayout.setOnClickListener(gridItemClick)
        holder.contactRoundedImageView.setOnClickListener(gridItemClick)

        buttonMessenger.setOnClickListener(buttonListener)
        buttonWhatsApp.setOnClickListener(buttonListener)
        buttonCall.setOnClickListener(buttonListener)
        buttonSMS.setOnClickListener(buttonListener)
        buttonEdit.setOnClickListener(buttonListener)
        buttonMail.setOnClickListener(buttonListener)
        buttonSignal.setOnClickListener(buttonListener)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return contactManager.contactList.size
    }

    override fun onMenuOpened(floatingActionMenu: FloatingActionMenu) {
        if (selectMenu != null) {
            selectMenu!!.close(false)
        }
        if (multiSelectMode()) {
            floatingActionMenu.close(false)
        }
        selectMenu = floatingActionMenu
    }

    /**
     * @param floatingActionMenu
     */
    override fun onMenuClosed(floatingActionMenu: FloatingActionMenu) {
        selectMenu = null
    }

    /**
     * Ferme le menu qui est ouvert
     */
    fun closeMenu() {
        if (selectMenu != null) selectMenu!!.close(true)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var contactFirstNameView: TextView
        var contactLastNameView: TextView
        var contactRoundedImageView: CircularImageView
        var gridAdapterFavoriteShine: AppCompatImageView
        var gridContactItemLayout: ConstraintLayout

        init {
            contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName)
            contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName)
            gridContactItemLayout = view.findViewById(R.id.grid_contact_item_layout)
            contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView)
            gridAdapterFavoriteShine = view.findViewById(R.id.grid_adapter_favorite_shine)
            //            heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        }
    }

    private fun appIsInstalled(): Boolean {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun multiSelectMode(): Boolean {
        return modeMultiSelect
    }

}