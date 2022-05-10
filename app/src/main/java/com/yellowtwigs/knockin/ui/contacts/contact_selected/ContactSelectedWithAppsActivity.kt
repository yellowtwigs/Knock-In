package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactSelectedWithAppsBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.contacts.ContactGridViewAdapter
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactSelectedWithAppsActivity : AppCompatActivity(),
    FloatingActionMenu.MenuStateChangeListener {

    private val listCircularMenu = ArrayList<FloatingActionMenu>()
    private lateinit var selectMenu: FloatingActionMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityContactSelectedWithAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", 0)
        val currentContact = ContactManager(this).getContactById(id)

        initRecyclerView(binding)
        if (currentContact != null) {
            initCircularMenu(binding, id, currentContact)
        }

        currentContact?.contactDB?.let { initUserData(binding, it) }
    }

    private fun initUserData(binding: ActivityContactSelectedWithAppsBinding, contact: ContactDB) {
        binding.apply {
            firstName.text = "${contact?.firstName}"
            lastName.text = "${contact?.lastName}"

            if (contact.profilePicture64 != "") {
                val bitmap = Converter.base64ToBitmap(contact.profilePicture64)
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(
                    RandomDefaultImage.randomDefaultImage(
                        contact.profilePicture,
                        this@ContactSelectedWithAppsActivity
                    )
                )
            }
        }
    }

    private fun initRecyclerView(binding: ActivityContactSelectedWithAppsBinding) {
        val contactManager = ContactManager(this.applicationContext)
        contactManager.sortContactByFirstNameAZ()
        val contactAdapter = ContactSelectedListAdapter(this)

        binding.apply {
            val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val nbGrid = sharedPreferences.getInt("gridview", 1)
            listApps.apply {
                adapter = contactAdapter
                layoutManager = GridLayoutManager(this@ContactSelectedWithAppsActivity, nbGrid)
                contactAdapter.submitList(contactManager.contactList.sortedByDescending {
                    it.contactDB?.contactPriority
                })
                setHasFixedSize(true)
                recycledViewPool.setMaxRecycledViews(0, 0)
            }
            listApps.isEnabled = false
        }
    }

    private fun initCircularMenu(
        binding: ActivityContactSelectedWithAppsBinding,
        id: Int,
        currentContact: ContactWithAllInformation
    ) {
        val listApp = getAppOnPhone(this)

        val buttonCall = ImageView(this)
        val buttonWhatsApp = ImageView(this)
        val buttonSMS = ImageView(this)
        val buttonEdit = ImageView(this)
        val buttonMail = ImageView(this)
        val buttonMessenger = ImageView(this)
        val buttonSignal = ImageView(this)
        val buttonTelegram = ImageView(this)

        buttonCall.id = 1
        buttonSMS.id = 2
        buttonWhatsApp.id = 3
        buttonEdit.id = 4
        buttonMail.id = 5
        buttonMessenger.id = 6
        buttonSignal.id = 7
        buttonTelegram.id = 8

        buttonCall.setImageResource(R.drawable.ic_google_call)
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp)
        buttonSMS.setImageResource(R.drawable.ic_sms_selector)
        buttonEdit.setImageResource(R.drawable.ic_circular_edit)
        buttonMail.setImageResource(R.drawable.ic_circular_mail)
        buttonMessenger.setImageResource(R.drawable.ic_circular_messenger)
        buttonSignal.setImageResource(R.drawable.ic_circular_signal)
        buttonTelegram.setImageResource(R.drawable.ic_circular_telegram)

        val builderIcon = SubActionButton.Builder(this)
        builderIcon.setBackgroundDrawable(getDrawable(R.drawable.ic_circular))
        builderIcon.setContentView(buttonCall)
        val diametreButton: Int
        val radiusMenu: Int
        val startAngle = 0
        val endAngle = -360
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        diametreButton = (0.35 * metrics.densityDpi).toInt()
        radiusMenu = (0.65 * metrics.densityDpi).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(5, 5, 5, 5)
        val builder = FloatingActionMenu.Builder(this)
            .setStartAngle(startAngle)
            .setEndAngle(endAngle)
            .setRadius(radiusMenu)
            .addSubActionView(
                builderIcon.setContentView(buttonEdit, layoutParams).build(),
                diametreButton,
                diametreButton
            )
            .attachTo(binding.contactLayout)
            .setStateChangeListener(this)
            .disableAnimations()

        if (currentContact.getFirstMail() != "") {
            builder.addSubActionView(
                builderIcon.setContentView(buttonMail, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }
        if (currentContact.getFirstPhoneNumber() != "") {
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
        if (currentContact.getMessengerID() != "" && listApp.contains("com.facebook.katana")) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonMessenger, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }

        if (isWhatsappInstalled(this) && currentContact.contactDB?.hasWhatsapp == 1) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonWhatsApp, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }

        if (listApp.contains("org.thoughtcrime.securesms") && currentContact.contactDB?.hasSignal == 1) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonSignal, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }

        if (listApp.contains("org.telegram.messenger") && currentContact.contactDB?.hasTelegram == 1) {
            builder.addSubActionView(
                builderIcon.setContentView(buttonTelegram, layoutParams).build(),
                diametreButton,
                diametreButton
            )
        }

        val quickMenu = builder.build()
        listCircularMenu.add(quickMenu)

        val buttonListener = View.OnClickListener { v: View ->
            when (v.id) {
                buttonWhatsApp.id -> {
                    ContactGesture.openWhatsapp(
                        Converter.converter06To33(currentContact.getFirstPhoneNumber()),
                        this
                    )
                }
                buttonEdit.id -> {
                    val intent = Intent(this, EditContactDetailsActivity::class.java)
                    intent.putExtra("ContactId", id)
                    startActivity(intent)
                }
                buttonCall.id -> {
                    ContactGesture.callPhone(currentContact.getFirstPhoneNumber(), this)
                }
                buttonSMS.id -> {
                    val phone = currentContact.getFirstPhoneNumber()
                    val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                    startActivity(i)
                }
                buttonMail.id -> {
                    val mail = currentContact.getFirstMail()
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "")
                    intent.putExtra(Intent.EXTRA_TEXT, "")
                    startActivity(intent)
                }
                buttonMessenger.id -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.messenger.com/t/" + currentContact.getMessengerID())
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                buttonSignal.id -> {
                    goToSignal(this)
                }
                buttonTelegram.id -> {
                    goToTelegram(this, currentContact.getFirstPhoneNumber())
                }
            }
        }

        binding.image.setOnClickListener {
            if (quickMenu.isOpen) {
                quickMenu.close(true)
            } else {
                quickMenu.open(true)
            }
        }

        buttonMessenger.setOnClickListener(buttonListener)
        buttonWhatsApp.setOnClickListener(buttonListener)
        buttonCall.setOnClickListener(buttonListener)
        buttonSMS.setOnClickListener(buttonListener)
        buttonEdit.setOnClickListener(buttonListener)
        buttonMail.setOnClickListener(buttonListener)
        buttonSignal.setOnClickListener(buttonListener)
        buttonTelegram.setOnClickListener(buttonListener)
    }

    override fun onMenuOpened(floatingActionMenu: FloatingActionMenu?) {
        if (floatingActionMenu != null) {
            selectMenu = floatingActionMenu
        }
    }

    override fun onMenuClosed(p0: FloatingActionMenu?) {
    }
}