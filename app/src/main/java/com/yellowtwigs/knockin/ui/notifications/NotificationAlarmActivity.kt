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
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewState
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.handleContactWithMultiplePhoneNumbers
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter
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
    private var currentContact: EditContactViewState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sbp = intent.extras?.get("notification") as StatusBarParcelable

        Log.i("NotificationsAlarmMsg", "sbp.contactId : ${sbp.contactId}")

        binding.apply {
            editContactViewModel.getContactById(sbp.contactId).observe(this@NotificationAlarmActivity) { contact ->
                currentContact = contact
                contact?.apply {
                    currentNotificationSound = notificationSound
                    currentNotificationTone = notificationTone
                    currentIsCustomSound = isCustomSound == 1

                    Log.i("GetNotificationSound", "notificationSound : $notificationSound")
                    Log.i("GetNotificationSound", "notificationTone : $notificationTone")
                    Log.i("GetNotificationSound", "isCustomSound : $isCustomSound")

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
                    val listOfPhoneNumbersWithSpinner = if (currentContact?.secondPhoneNumber?.phoneNumber == "") {
                        listOf(
                            PhoneNumberWithSpinner(
                                currentContact?.firstPhoneNumber?.flag, currentContact?.firstPhoneNumber?.phoneNumber ?: ""
                            )
                        )
                    } else {
                        listOf(
                            PhoneNumberWithSpinner(
                                currentContact?.firstPhoneNumber?.flag, currentContact?.firstPhoneNumber?.phoneNumber ?: ""
                            ), PhoneNumberWithSpinner(
                                currentContact?.secondPhoneNumber?.flag, currentContact?.secondPhoneNumber?.phoneNumber ?: ""
                            )
                        )
                    }






                    currentContact?.firstPhoneNumber ?: listOf(PhoneNumberWithSpinner(null, ""))

                    if (isSMS) {
                        if (currentContact != null) {
                            handleContactWithMultiplePhoneNumbers(
                                cxt = this@NotificationAlarmActivity,
                                phoneNumbers = listOfPhoneNumbersWithSpinner,
                                action = "telegram",
                                onClickedMultipleNumbers = { action, number1, number2 ->
                                    when (action) {
                                        "call" -> {
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle(R.string.notif_adapter_call)
                                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                                .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                    ContactGesture.callPhone(number1.phoneNumber, this@NotificationAlarmActivity)
                                                }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                    ContactGesture.callPhone(number2.phoneNumber, this@NotificationAlarmActivity)
                                                }.show()
                                        }
                                        "sms" -> {
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle(R.string.list_contact_item_sms)
                                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                                .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                    openSms(number1.phoneNumber, this@NotificationAlarmActivity)
                                                }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                    openSms(number2.phoneNumber, this@NotificationAlarmActivity)
                                                }.show()
                                        }
                                        "whatsapp" -> {
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor))
                                                .setTitle(R.string.list_contact_item_whatsapp)
                                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                                .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                    openWhatsapp(
                                                        Converter.converter06To33(number1.phoneNumber), this@NotificationAlarmActivity
                                                    )
                                                }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                    openWhatsapp(
                                                        Converter.converter06To33(number2.phoneNumber), this@NotificationAlarmActivity
                                                    )
                                                }.show()
                                        }
                                        "telegram" -> {
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("Telegram")
                                                .setMessage(getString(R.string.two_numbers_dialog_message))
                                                .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                    goToTelegram(this@NotificationAlarmActivity, number1.phoneNumber)
                                                }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                    goToTelegram(this@NotificationAlarmActivity, number2.phoneNumber)
                                                }.show()
                                        }
                                    }
                                },
                                onNotMobileFlagClicked = { action, phoneNumber, message ->
                                    MaterialAlertDialogBuilder(
                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                        .setTitle(getString(R.string.not_mobile_flag_title))
                                        .setMessage("Please check that this number can receive sms")
                                        .setPositiveButton(phoneNumber) { _, _ ->
                                            when (action) {
                                                "send_whatsapp" -> {
                                                    ContactGesture.sendMessageWithWhatsapp(
                                                        phoneNumber, message, this@NotificationAlarmActivity
                                                    )
                                                }
                                                "send_message" -> {
                                                    ContactGesture.sendMessageWithAndroidMessage(
                                                        phoneNumber, message, this@NotificationAlarmActivity
                                                    )
                                                }
                                                "sms" -> {
                                                    openSms(phoneNumber, this@NotificationAlarmActivity)
                                                }
                                                "whatsapp" -> {
                                                    openWhatsapp(phoneNumber, this@NotificationAlarmActivity)
                                                }
                                                "telegram" -> {
                                                    goToTelegram(this@NotificationAlarmActivity, phoneNumber)
                                                }
                                            }
                                        }.setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                                        }.show()
                                },
                                ""
                            )
                        } else {
                            openSms(
                                statusBarNotificationInfo["android.title"] as String, this@NotificationAlarmActivity
                            )
                        }
                    } else {
                        when (appNotifier) {
                            "com.whatsapp" -> {
                                if (currentContact != null) {
                                    handleContactWithMultiplePhoneNumbers(
                                        cxt = this@NotificationAlarmActivity,
                                        phoneNumbers = listOfPhoneNumbersWithSpinner,
                                        action = "telegram",
                                        onClickedMultipleNumbers = { action, number1, number2 ->
                                            when (action) {
                                                "call" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.notif_adapter_call)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            ContactGesture.callPhone(number1.phoneNumber, this@NotificationAlarmActivity)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            ContactGesture.callPhone(number2.phoneNumber, this@NotificationAlarmActivity)
                                                        }.show()
                                                }
                                                "sms" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.list_contact_item_sms)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            openSms(number1.phoneNumber, this@NotificationAlarmActivity)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            openSms(number2.phoneNumber, this@NotificationAlarmActivity)
                                                        }.show()
                                                }
                                                "whatsapp" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.list_contact_item_whatsapp)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            openWhatsapp(
                                                                Converter.converter06To33(number1.phoneNumber),
                                                                this@NotificationAlarmActivity
                                                            )
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            openWhatsapp(
                                                                Converter.converter06To33(number2.phoneNumber),
                                                                this@NotificationAlarmActivity
                                                            )
                                                        }.show()
                                                }
                                                "telegram" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("Telegram")
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            goToTelegram(this@NotificationAlarmActivity, number1.phoneNumber)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            goToTelegram(this@NotificationAlarmActivity, number2.phoneNumber)
                                                        }.show()
                                                }
                                            }
                                        },
                                        onNotMobileFlagClicked = { action, phoneNumber, message ->
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor))
                                                .setTitle(getString(R.string.not_mobile_flag_title))
                                                .setMessage("Please check that this number can receive sms")
                                                .setPositiveButton(phoneNumber) { _, _ ->
                                                    when (action) {
                                                        "send_whatsapp" -> {
                                                            ContactGesture.sendMessageWithWhatsapp(
                                                                phoneNumber, message, this@NotificationAlarmActivity
                                                            )
                                                        }
                                                        "send_message" -> {
                                                            ContactGesture.sendMessageWithAndroidMessage(
                                                                phoneNumber, message, this@NotificationAlarmActivity
                                                            )
                                                        }
                                                        "sms" -> {
                                                            openSms(phoneNumber, this@NotificationAlarmActivity)
                                                        }
                                                        "whatsapp" -> {
                                                            openWhatsapp(phoneNumber, this@NotificationAlarmActivity)
                                                        }
                                                        "telegram" -> {
                                                            goToTelegram(this@NotificationAlarmActivity, phoneNumber)
                                                        }
                                                    }
                                                }.setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                                                }.show()
                                        },
                                        ""
                                    )
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
                                    handleContactWithMultiplePhoneNumbers(
                                        cxt = this@NotificationAlarmActivity,
                                        phoneNumbers = listOfPhoneNumbersWithSpinner,
                                        action = "telegram",
                                        onClickedMultipleNumbers = { action, number1, number2 ->
                                            when (action) {
                                                "call" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.notif_adapter_call)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            ContactGesture.callPhone(number1.phoneNumber, this@NotificationAlarmActivity)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            ContactGesture.callPhone(number2.phoneNumber, this@NotificationAlarmActivity)
                                                        }.show()
                                                }
                                                "sms" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.list_contact_item_sms)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            openSms(number1.phoneNumber, this@NotificationAlarmActivity)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            openSms(number2.phoneNumber, this@NotificationAlarmActivity)
                                                        }.show()
                                                }
                                                "whatsapp" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor))
                                                        .setTitle(R.string.list_contact_item_whatsapp)
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            openWhatsapp(
                                                                Converter.converter06To33(number1.phoneNumber),
                                                                this@NotificationAlarmActivity
                                                            )
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            openWhatsapp(
                                                                Converter.converter06To33(number2.phoneNumber),
                                                                this@NotificationAlarmActivity
                                                            )
                                                        }.show()
                                                }
                                                "telegram" -> {
                                                    MaterialAlertDialogBuilder(
                                                        this@NotificationAlarmActivity, R.style.AlertDialog
                                                    ).setBackground(getDrawable(R.color.backgroundColor)).setTitle("Telegram")
                                                        .setMessage(getString(R.string.two_numbers_dialog_message))
                                                        .setPositiveButton(number1.phoneNumber) { _, _ ->
                                                            goToTelegram(this@NotificationAlarmActivity, number1.phoneNumber)
                                                        }.setNegativeButton(number2.phoneNumber) { _, _ ->
                                                            goToTelegram(this@NotificationAlarmActivity, number2.phoneNumber)
                                                        }.show()
                                                }
                                            }
                                        },
                                        onNotMobileFlagClicked = { action, phoneNumber, message ->
                                            MaterialAlertDialogBuilder(
                                                this@NotificationAlarmActivity, R.style.AlertDialog
                                            ).setBackground(getDrawable(R.color.backgroundColor))
                                                .setTitle(getString(R.string.not_mobile_flag_title))
                                                .setMessage("Please check that this number can receive sms")
                                                .setPositiveButton(phoneNumber) { _, _ ->
                                                    when (action) {
                                                        "send_whatsapp" -> {
                                                            ContactGesture.sendMessageWithWhatsapp(
                                                                phoneNumber, message, this@NotificationAlarmActivity
                                                            )
                                                        }
                                                        "send_message" -> {
                                                            ContactGesture.sendMessageWithAndroidMessage(
                                                                phoneNumber, message, this@NotificationAlarmActivity
                                                            )
                                                        }
                                                        "sms" -> {
                                                            openSms(phoneNumber, this@NotificationAlarmActivity)
                                                        }
                                                        "whatsapp" -> {
                                                            openWhatsapp(phoneNumber, this@NotificationAlarmActivity)
                                                        }
                                                        "telegram" -> {
                                                            goToTelegram(this@NotificationAlarmActivity, phoneNumber)
                                                        }
                                                    }
                                                }.setNegativeButton(getString(R.string.alert_dialog_no)) { _, _ ->
                                                }.show()
                                        },
                                        ""
                                    )
//                                    goToTelegram(
//                                        this@NotificationAlarmActivity, "${currentContact?.firstName} ${currentContact?.lastName}"
//                                    )
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