package com.yellowtwigs.knockin.ui.notifications.alarm

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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.contacts.SingleContactViewState
import com.yellowtwigs.knockin.utils.ContactGesture
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

    private var currentIsCustomSound = false
    private var currentNotificationSound = 0
    private var currentNotificationTone = ""

    private var alarmSound: MediaPlayer? = null

    private val editContactViewModel: EditContactViewModel by viewModels()
    private var currentContact: SingleContactViewState? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        val notificationsList = extras?.getParcelableArrayList<StatusBarParcelable>("ListOfNotifications")

//        val sbp = intent.extras?.get("notification") as StatusBarParcelable

        notificationsList?.let {
            val adapter = NotificationsAlarmListAdapter(this@NotificationAlarmActivity) { isSMS, sender, appNotifier ->
                currentContact?.firstPhoneNumber ?: listOf(PhoneNumberWithSpinner(null, ""))

                if (isSMS) {
                    if (currentContact != null) {
                        currentContact?.let { contact ->
                            openSms(contact.firstPhoneNumber, this@NotificationAlarmActivity)
                        }
                    } else {
                        openSms(sender, this@NotificationAlarmActivity)
                    }
                } else {
                    when (appNotifier) {
                        "com.whatsapp" -> {
                            if (currentContact != null) {
                                currentContact?.let { contact ->
                                    openWhatsapp(
                                        converter06To33(contact.firstPhoneNumber),
                                        this@NotificationAlarmActivity
                                    )
                                }
                            } else {
                                openWhatsapp(sender, this@NotificationAlarmActivity)
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
                            ContactGesture.goToOutlook(this@NotificationAlarmActivity)
                        }

                        "org.thoughtcrime.securesms" -> {
                            ContactGesture.goToSignal(this@NotificationAlarmActivity)
                        }

                        "org.telegram.messenger" -> {
                            if (currentContact != null) {
                                currentContact?.let { contact ->
                                    ContactGesture.goToTelegram(this@NotificationAlarmActivity, contact.firstPhoneNumber)
                                }
                            } else {
                                ContactGesture.goToTelegram(this@NotificationAlarmActivity, "")
                            }
                        }

                        "com.facebook.katana" -> {
                            ContactGesture.openMessenger(
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
            adapter.submitList(it)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this@NotificationAlarmActivity)

            it.forEach { sbp ->
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

                    shutDown.setOnClickListener {
                        if (alarmSound != null) {
                            alarmSound?.stop()
                        }

                        finish()
                    }

                }
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