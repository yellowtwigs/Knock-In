package com.yellowtwigs.knockin.ui.notifications

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.ui.contacts.SingleContactViewState
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewState
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter.converter06To33
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NotificationAlarmActivity : AppCompatActivity() {

    private var isSMS = false
    private var currentIsCustomSound = false
    private var currentNotificationSound = 0
    private var currentNotificationTone = ""

    private var alarmSound: MediaPlayer? = null

    private val editContactViewModel: EditContactViewModel by viewModels()
    private var currentContact: SingleContactViewState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sbp = intent.extras?.get("notification") as StatusBarParcelable

        binding.apply {
            editContactViewModel.getSingleContactViewStateById(sbp.contactId).observe(this@NotificationAlarmActivity) { contact ->
                currentContact = contact
                contact?.apply {
                    currentNotificationSound = notificationSound
                    currentNotificationTone = notificationTone
                    currentIsCustomSound = isCustomSound == 1

                    soundRingtone()
                }
            }

            sbp.apply {
                val sender = statusBarNotificationInfo["android.title"] as String
                messageContent.text = getString(R.string.message_from, sender)
            }

            when (sbp.appNotifier) {
                "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {
                    isSMS = true
                    messageImage.setImageResource(R.drawable.ic_micon)
                }
                "com.whatsapp" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_circular_whatsapp)
                }
                "com.google.android.gm" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_circular_gmail)
                }
                "com.microsoft.office.outlook" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_outlook)
                }
                "org.thoughtcrime.securesms" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_circular_signal)
                }
                "org.telegram.messenger" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_telegram)
                }
                "com.facebook.katana" -> {
                    isSMS = false
                    messageImage.setImageResource(R.drawable.ic_circular_messenger)
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                delay(6000)

                alarmSound?.stop()
            }

            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            if (Build.VERSION.SDK_INT >= 27) {
                setTurnScreenOn(true)
                setShowWhenLocked(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this@NotificationAlarmActivity, null)
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }

            sbp.apply {
                receiveMessageLayout.setOnClickListener {

                    currentContact?.firstPhoneNumber ?: listOf(PhoneNumberWithSpinner(null, ""))

                    if (isSMS) {
                        if (currentContact != null) {
                            currentContact?.let { contact ->
                                openSms(contact.firstPhoneNumber, this@NotificationAlarmActivity)
                            }
                        } else {
                            openSms(
                                statusBarNotificationInfo["android.title"] as String, this@NotificationAlarmActivity
                            )
                        }
                    } else {
                        when (appNotifier) {
                            "com.whatsapp" -> {
                                if (currentContact != null) {
                                    currentContact?.let { contact ->
                                        openWhatsapp(converter06To33(contact.firstPhoneNumber), this@NotificationAlarmActivity)
                                    }
                                } else {
                                    openWhatsapp(
                                        statusBarNotificationInfo["android.title"] as String, this@NotificationAlarmActivity
                                    )
                                }
                            }
                            "com.google.android.gm" -> {
                                val appIntent = Intent(Intent.ACTION_VIEW)
                                appIntent.setClassName(
                                    "com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail"
                                )
                                try {
                                    startActivity(appIntent)
                                } catch (e: ActivityNotFoundException) {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW, Uri.parse("https://gmail.com/")
                                        )
                                    )
                                }
                            }
                            "com.microsoft.office.outlook" -> {
                                goToOutlook(this@NotificationAlarmActivity)
                            }
                            "org.thoughtcrime.securesms" -> {
                                goToSignal(this@NotificationAlarmActivity)
                            }
                            "org.telegram.messenger" -> {
                                if (currentContact != null) {
                                    currentContact?.let { contact ->
                                        goToTelegram(this@NotificationAlarmActivity, contact.firstPhoneNumber)
                                    }
                                } else {
                                    goToTelegram(this@NotificationAlarmActivity, "")
                                }
                            }
                            "com.facebook.katana" -> {
                                openMessenger(
                                    currentContact?.messengerId ?: "", this@NotificationAlarmActivity
                                )
                            }
                        }
                    }

                    if (alarmSound != null) {
                        alarmSound?.stop()
                    }

                    finish()
                }
            }

            shutDown.setOnClickListener {
                if (alarmSound != null) {
                    alarmSound?.stop()
                }

                finish()
            }

        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun soundRingtone() {
        alarmSound?.stop()
        alarmSound = if (currentIsCustomSound) {
            MediaPlayer.create(this, Uri.parse(currentNotificationTone))
        } else {
            Log.i("GetNotificationSound", "currentNotificationSound : $currentNotificationSound")
            if (currentNotificationSound == -1) {
                MediaPlayer.create(this, R.raw.sms_ring)
            } else {
                MediaPlayer.create(this, currentNotificationSound)
            }
        }
        alarmSound?.start()
    }
}