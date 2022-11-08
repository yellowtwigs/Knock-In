package com.yellowtwigs.knockin.ui.notifications

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.model.database.data.ContactDB
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
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
    private var currentContact: ContactDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            editContactViewModel.getContactById(intent.getIntExtra("ContactId", 1))
                .observe(this@NotificationAlarmActivity) { contact ->
                    currentContact = contact
                    contact?.apply {
                        currentNotificationSound = notificationSound
                        currentNotificationTone = notificationTone
                        currentIsCustomSound = isCustomSound == 1

                        soundRingtone()
                    }
                }

            val sbp = intent.extras?.get("notification") as StatusBarParcelable

            sbp.apply {
                val sender = statusBarNotificationInfo["android.title"] as String
                val content = statusBarNotificationInfo["android.text"] as String

                messageContent.text = "$sender : $content"
            }

            when (sbp.appNotifier) {
                "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {
                    isSMS = true
                    messageImage.setImageResource(R.drawable.ic_sms_selector)
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
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            if (Build.VERSION.SDK_INT >= 27) {
                setTurnScreenOn(true)
                setShowWhenLocked(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this@NotificationAlarmActivity, null)
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }

            sbp.apply {
                receiveMessageLayout.setOnClickListener {
                    if (isSMS) {
                        if (currentContact != null) {
                            openSms(
                                currentContact?.listOfPhoneNumbers?.get(0) ?: "",
                                this@NotificationAlarmActivity
                            )
                        } else {
                            openSms(
                                statusBarNotificationInfo["android.title"] as String,
                                this@NotificationAlarmActivity
                            )
                        }
                    } else {
                        when (appNotifier) {
                            "com.whatsapp" -> {
                                if (currentContact != null) {
                                    openWhatsapp(
                                        currentContact?.listOfPhoneNumbers?.get(0) ?: "",
                                        this@NotificationAlarmActivity
                                    )
                                } else {
                                    openWhatsapp(
                                        statusBarNotificationInfo["android.title"] as String,
                                        this@NotificationAlarmActivity
                                    )
                                }
                            }
                            "com.google.android.gm" -> {
                                val appIntent = Intent(Intent.ACTION_VIEW)
                                appIntent.setClassName(
                                    "com.google.android.gm",
                                    "com.google.android.gm.ConversationListActivityGmail"
                                )
                                try {
                                    startActivity(appIntent)
                                } catch (e: ActivityNotFoundException) {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://gmail.com/")
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
                                    goToTelegram(
                                        this@NotificationAlarmActivity,
                                        "${currentContact?.firstName} ${currentContact?.lastName}"
                                    )
                                } else {
                                    goToTelegram(
                                        this@NotificationAlarmActivity,
                                        currentContact?.listOfPhoneNumbers?.get(0) ?: ""
                                    )
                                }
                            }
                            "com.facebook.katana" -> {
                                openMessenger(
                                    currentContact?.messengerId ?: "",
                                    this@NotificationAlarmActivity
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
            MediaPlayer.create(this, currentNotificationSound)
        }
        alarmSound?.start()
    }
}