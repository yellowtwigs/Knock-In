package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactSelectedWithAppsBinding
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkThemePreferences
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactSelectedWithAppsActivity : AppCompatActivity() {

    private var appsSupportPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkThemePreferences(this)

        val binding = ActivityContactSelectedWithAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appsSupportPref = getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

        val id = intent.getIntExtra("id", 0)
//        val currentContact = ContactManager(this).getContactById(id)

//        if (currentContact != null) {
//            initCircularMenu(binding, id, currentContact)
//        }

        initContactsList(binding)
//        currentContact?.contactDB?.let { initUserData(binding, it) }

        binding.backIcon.setOnClickListener {
//            openWhatsappGroup()
            onBackPressed()
            finish()
        }
    }

//    private val URL = "https://my.signal.server.com"
//    private val TRUST_STORE: TrustStore = MyTrustStoreImpl()
//    private val USERNAME = "+14151231234"
//    private val PASSWORD: String = generateRandomPassword()
//    private val USER_AGENT = "[FILL_IN]"
//
//    private fun setSignalMessage(){
//        val accountManager = SignalServiceAccountManager(
//            URL, TRUST_STORE,
//            USERNAME, PASSWORD, USER_AGENT
//        )
//
//        accountManager.requestSmsVerificationCode()
//        accountManager.verifyAccountWithCode(
//            receivedSmsVerificationCode, generateRandomSignalingKey(),
//            generateRandomInstallId(), false
//        )
//        accountManager.setGcmId(
//            Optional.of(
//                GoogleCloudMessaging.getInstance(this).register(REGISTRATION_ID)
//            )
//        )
//        accountManager.setPreKeys(
//            identityKey.getPublicKey(),
//            lastResortKey,
//            signedPreKeyRecord,
//            oneTimePreKeys
//        )
//
//        val messageSender = SignalServiceMessageSender(
//            URL, TRUST_STORE, USERNAME, PASSWORD,
//            MySignalProtocolStore(),
//            USER_AGENT, Optional.absent()
//        )
//
//        messageSender.sendMessage(
//            SignalServiceAddress("+14159998888"),
//            SignalServiceDataMessage.newBuilder()
//                .withBody("Hello, world!")
//                .build()
//        )
//    }

    private fun openWhatsappGroup() {
//        val url = "https://chat.whatsapp.com/HkVePIAkCLu60NUR5R6IPG&text=test"
        val url = "https://api.whatsapp.com/send?phone=&text=test"

        try {
            val pm = packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    private fun initContactsList(binding: ActivityContactSelectedWithAppsBinding) {
        val adapter = ContactSelectedListAdapter(this)

        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 1)

//        val contactManager = ContactManager(this)

        binding.recyclerView.apply {
//            contactManager.contactList.sortBy {
//                it.contactDB?.firstName + it.contactDB?.lastName
//            }
            this.adapter = adapter
//            adapter.submitList(contactManager.contactList)
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
                    randomDefaultImage(contact.profilePicture)
                )
            }

//            when (contact.contactPriority) {
//                0 -> {
//                    image.setBorderColor(
//                        resources.getColor(
//                            R.color.priorityZeroColor,
//                            null
//                        )
//                    )
//                }
//                1 -> {
//                    image.setBorderColor(
//                        resources.getColor(
//                            R.color.transparentColor,
//                            null
//                        )
//                    )
//                }
//                2 -> {
//                    image.setBorderColor(
//                        resources.getColor(
//                            R.color.priorityTwoColor,
//                            null
//                        )
//                    )
//                }
//            }
        }
    }

    private fun initCircularMenu(
        binding: ActivityContactSelectedWithAppsBinding,
        id: Int
    ) {
        val listApp = getAppOnPhone(this)

//        binding.apply {
//            currentContact.apply {
//                val reapparrition = AnimationUtils.loadAnimation(
//                    this@ContactSelectedWithAppsActivity,
//                    R.anim.reapparrition
//                )
//                CoroutineScope(Dispatchers.Main).launch {
//                    smsIcon.visibility = View.GONE
//                    callIcon.visibility = View.GONE
//                    mailIcon.visibility = View.GONE
//                    messengerIcon.visibility = View.GONE
//                    signalIcon.visibility = View.GONE
//                    editIcon.visibility = View.GONE
//                    telegramIcon.visibility = View.GONE
//                    whatsappIcon.visibility = View.GONE
//                    delay(500)
//                    binding.smsIcon.startAnimation(reapparrition)
//                    binding.callIcon.startAnimation(reapparrition)
//                    binding.mailIcon.startAnimation(reapparrition)
//                    binding.messengerIcon.startAnimation(reapparrition)
//                    binding.signalIcon.startAnimation(reapparrition)
//                    binding.editIcon.startAnimation(reapparrition)
//                    binding.telegramIcon.startAnimation(reapparrition)
//                    binding.whatsappIcon.startAnimation(reapparrition)
//                    smsIcon.isVisible = getFirstPhoneNumber() != ""
//                    callIcon.isVisible = getFirstPhoneNumber() != ""
//                    mailIcon.isVisible = getFirstMail() != ""
//                    messengerIcon.isVisible =
//                        getMessengerID() != "" && listApp.contains("com.facebook.orca")
//                    signalIcon.isVisible =
//                        listApp.contains("org.thoughtcrime.securesms") && currentContact.contactDB?.hasSignal == 1
//                    editIcon.isVisible = true
//                    telegramIcon.isVisible =
//                        listApp.contains("org.telegram.messenger") && currentContact.contactDB?.hasTelegram == 1
//                    whatsappIcon.isVisible =
//                        isWhatsappInstalled(this@ContactSelectedWithAppsActivity) && currentContact.contactDB?.hasWhatsapp == 1
//                }
//            }
//
//            if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
//                signalIcon.setImageResource(R.drawable.ic_signal_disable)
//                telegramIcon.setImageResource(R.drawable.ic_telegram_disable)
//                messengerIcon.setImageResource(R.drawable.ic_messenger_disable)
//            }
//
//            val buttonListener = View.OnClickListener { v: View ->
//                when (v.id) {
//                    whatsappIcon.id -> {
//                        val sendIntent = Intent()
//                        sendIntent.action = Intent.ACTION_SEND
//                        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
//                        sendIntent.setType("text/plain")
//                        sendIntent.setPackage("com.whatsapp");
//                        startActivity(sendIntent)
////                        ContactGesture.openWhatsapp(
////                            Converter.converter06To33(currentContact.getFirstPhoneNumber()),
////                            this@ContactSelectedWithAppsActivity
////                        )
//                    }
//                    editIcon.id -> {
//                        val intent = Intent(
//                            this@ContactSelectedWithAppsActivity,
//                            EditContactDetailsActivity::class.java
//                        )
//                        intent.putExtra("ContactId", id)
//                        startActivity(intent)
//                    }
//                    callIcon.id -> {
//                        ContactGesture.callPhone(
//                            currentContact.getFirstPhoneNumber(),
//                            this@ContactSelectedWithAppsActivity
//                        )
//                    }
//                    smsIcon.id -> {
//                        val phone = currentContact.getFirstPhoneNumber()
//                        val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
//                        startActivity(i)
//                    }
//                    mailIcon.id -> {
//                        val mail = currentContact.getFirstMail()
//                        val intent = Intent(Intent.ACTION_SENDTO)
//                        intent.data = Uri.parse("mailto:")
//                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
//                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
//                        intent.putExtra(Intent.EXTRA_TEXT, "")
//                        startActivity(intent)
//                    }
//                    messengerIcon.id -> {
//                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
//                            showInAppAlertDialog()
//                        } else {
//                            val intent = Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse("https://www.messenger.com/t/" + currentContact.getMessengerID())
//                            )
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(intent)
//                        }
//                    }
//                    signalIcon.id -> {
//                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
//                            showInAppAlertDialog()
//                        } else {
//                            goToSignal(this@ContactSelectedWithAppsActivity)
//                        }
//                    }
//                    telegramIcon.id -> {
//                        if (appsSupportPref?.getBoolean("Apps_Support_Bought", false) == false) {
//                            showInAppAlertDialog()
//                        } else {
//                            goToTelegram(
//                                this@ContactSelectedWithAppsActivity,
//                                currentContact.getFirstPhoneNumber()
//                            )
//                        }
//                    }
//                }
//            }
//
//            messengerIcon.setOnClickListener(buttonListener)
//            whatsappIcon.setOnClickListener(buttonListener)
//            callIcon.setOnClickListener(buttonListener)
//            smsIcon.setOnClickListener(buttonListener)
//            editIcon.setOnClickListener(buttonListener)
//            mailIcon.setOnClickListener(buttonListener)
//            signalIcon.setOnClickListener(buttonListener)
//            telegramIcon.setOnClickListener(buttonListener)
//        }
    }

    private fun randomDefaultImage(avatarId: Int): Int {
        val sharedPreferencesIsMultiColor =
            getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("isMultiColor", 0)
        val sharedPreferencesContactsColor =
            getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
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
}