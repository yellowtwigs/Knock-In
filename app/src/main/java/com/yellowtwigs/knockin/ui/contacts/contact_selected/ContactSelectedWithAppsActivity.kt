package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.FirstLaunchActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactSelectedWithAppsBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.RandomDefaultImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactSelectedWithAppsActivity : AppCompatActivity() {

    private var appsSupportPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //region ======================================== Theme Dark ========================================

        val sharedThemePreferences = getSharedPreferences("Knockin_Theme", Context.MODE_PRIVATE)
        if (sharedThemePreferences.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }

        //endregion

        val binding = ActivityContactSelectedWithAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animationAppsIcons(binding)

        appsSupportPref = getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

        val id = intent.getIntExtra("id", 0)
        val currentContact = ContactManager(this).getContactById(id)

        if (currentContact != null) {
            initCircularMenu(binding, id, currentContact)
        }

        initContactsList(binding)
        currentContact?.contactDB?.let { initUserData(binding, it) }

        binding.backIcon.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun initContactsList(binding: ActivityContactSelectedWithAppsBinding) {
        val adapter = ContactSelectedListAdapter(this)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)

        val contactManager = ContactManager(this)

        binding.recyclerView.apply {
            contactManager.contactList.sortBy {
                it.contactDB?.firstName + it.contactDB?.lastName
            }
            this.adapter = adapter
            adapter.submitList(contactManager.contactList)
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ContactSelectedWithAppsActivity, len)
            recycledViewPool.setMaxRecycledViews(0, 0)
        }
    }

    private fun initUserData(binding: ActivityContactSelectedWithAppsBinding, contact: ContactDB) {
        binding.apply {
            name.text = "${contact.firstName} ${contact.lastName}"

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

    private fun initCircularMenu(
        binding: ActivityContactSelectedWithAppsBinding,
        id: Int,
        currentContact: ContactWithAllInformation
    ) {
        val listApp = getAppOnPhone(this)

        binding.apply {
            currentContact.apply {
                mailIcon.isVisible = getFirstMail() != ""
                smsIcon.isVisible = getFirstPhoneNumber() != ""
                messengerIcon.isVisible =
                    getMessengerID() != "" && listApp.contains("com.facebook.orca")

                if (isWhatsappInstalled(this@ContactSelectedWithAppsActivity) && currentContact.contactDB?.hasWhatsapp == 1) {
                    whatsappIcon.visibility = View.VISIBLE
                } else {
                    whatsappIcon.visibility = View.INVISIBLE
                }
                signalIcon.isVisible =
                    listApp.contains("org.thoughtcrime.securesms") && currentContact.contactDB?.hasSignal == 1

                telegramIcon.isVisible =
                    listApp.contains("org.telegram.messenger") && currentContact.contactDB?.hasTelegram == 1
            }

            if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                signalIcon.setImageResource(R.drawable.ic_signal_disable)
                telegramIcon.setImageResource(R.drawable.ic_telegram_disable)
                messengerIcon.setImageResource(R.drawable.ic_messenger_disable)
            }

            val buttonListener = View.OnClickListener { v: View ->
                when (v.id) {
                    whatsappIcon.id -> {
                        ContactGesture.openWhatsapp(
                            Converter.converter06To33(currentContact.getFirstPhoneNumber()),
                            this@ContactSelectedWithAppsActivity
                        )
                    }
                    editIcon.id -> {
                        val intent = Intent(
                            this@ContactSelectedWithAppsActivity,
                            EditContactDetailsActivity::class.java
                        )
                        intent.putExtra("ContactId", id)
                        startActivity(intent)
                    }
                    callIcon.id -> {
                        ContactGesture.callPhone(
                            currentContact.getFirstPhoneNumber(),
                            this@ContactSelectedWithAppsActivity
                        )
                    }
                    smsIcon.id -> {
                        val phone = currentContact.getFirstPhoneNumber()
                        val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                        startActivity(i)
                    }
                    mailIcon.id -> {
                        val mail = currentContact.getFirstMail()
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = Uri.parse("mailto:")
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        startActivity(intent)
                    }
                    messengerIcon.id -> {
                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                            showInAppAlertDialog()
                        } else {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.messenger.com/t/" + currentContact.getMessengerID())
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    signalIcon.id -> {
                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                            showInAppAlertDialog()
                        } else {
                            goToSignal(this@ContactSelectedWithAppsActivity)
                        }
                    }
                    telegramIcon.id -> {
                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
                            showInAppAlertDialog()
                        } else {
                            goToTelegram(
                                this@ContactSelectedWithAppsActivity,
                                currentContact.getFirstPhoneNumber()
                            )
                        }
                    }
                }
            }

            messengerIcon.setOnClickListener(buttonListener)
            whatsappIcon.setOnClickListener(buttonListener)
            callIcon.setOnClickListener(buttonListener)
            smsIcon.setOnClickListener(buttonListener)
            editIcon.setOnClickListener(buttonListener)
            mailIcon.setOnClickListener(buttonListener)
            signalIcon.setOnClickListener(buttonListener)
            telegramIcon.setOnClickListener(buttonListener)
        }
    }

    private fun showInAppAlertDialog() {
        MaterialAlertDialogBuilder(this, R.style.AlertDialog)
            .setTitle(getString(R.string.in_app_popup_apps_support_title))
            .setMessage(getString(R.string.in_app_popup_apps_support_message))
            .setPositiveButton(R.string.alert_dialog_yes) { _, _ ->
                startActivity(Intent(this, PremiumActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.alert_dialog_later) { dialog, _ ->
                dialog.dismiss()
                dialog.cancel()
            }
            .show()
    }

    private fun animationAppsIcons(binding: ActivityContactSelectedWithAppsBinding) {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val reapparrition = AnimationUtils.loadAnimation(this, R.anim.reapparrition)
        val slideToTop = AnimationUtils.loadAnimation(this, R.anim.slide_to_top)
        val slideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top)
        val slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom)
        val slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left)
        val slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right)

        CoroutineScope(Dispatchers.Main).launch {
//            binding.smsIcon.visibility = View.GONE
//            binding.smsIcon.startAnimation(slideOutBottom)
            binding.smsFakeIcon.visibility = View.VISIBLE
            binding.smsIcon.visibility = View.GONE
            binding.smsFakeIcon.startAnimation(slideToTop)
//            binding.smsIcon.startAnimation(slideOutTop)
            delay(700)
            binding.smsIcon.startAnimation(reapparrition)
            binding.smsIcon.visibility = View.VISIBLE
            binding.smsFakeIcon.visibility = View.GONE
//            binding.smsIcon.visibility = View.VISIBLE
        }
    }
}