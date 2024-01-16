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
import com.yellowtwigs.knockin.background.service.NotificationsListenerService
import com.yellowtwigs.knockin.background.service.PopupNotificationParams
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.database.StatusBarParcelable
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.SingleContactViewState
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter.converter06To33
import com.yellowtwigs.knockin.utils.NotificationsGesture
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sbp = intent.extras?.get("notification") as StatusBarParcelable

        editContactViewModel.getSingleContactByIdLiveData(sbp.contactId).observe(this@NotificationAlarmActivity) { contact ->
            Log.i("AlarmVIP", "contact : $contact")
            Log.i("AlarmVIP", "NotificationsListenerService.notificationsList : ${NotificationsListenerService.notificationsList}")

            NotificationsListenerService.notificationsList.distinctBy {
                val name = it.statusBarNotificationInfo["android.title"].toString()
                val message = it.statusBarNotificationInfo["android.text"].toString()
                PopupNotificationParams(
                    contactName = name,
                    description = message,
                    platform = it.appNotifier!!,
                    date = it.dateTime,
                )
            }.let {
                val adapter = NotificationsAlarmListAdapter(this@NotificationAlarmActivity) { isSMS, sender, appNotifier ->
                    contact?.firstPhoneNumber ?: listOf(PhoneNumberWithSpinner(null, ""))
                    NotificationsListenerService.notificationsList.clear()

                    if (isSMS) {
                        if (contact != null) {
                            openSms(contact.firstPhoneNumber, this@NotificationAlarmActivity)
                            finish()
                        } else {
                            openSms(sender, this@NotificationAlarmActivity)
                            finish()
                        }
                    } else {
                        when (appNotifier) {
                            "com.whatsapp" -> {
                                if (contact != null) {
                                    openWhatsapp(
                                        converter06To33(contact.firstPhoneNumber),
                                        this@NotificationAlarmActivity
                                    )
                                    finish()
                                } else {
                                    openWhatsapp(sender, this@NotificationAlarmActivity)
                                    finish()
                                }
                            }

                            "com.google.android.gm" -> {
                                val appIntent = Intent(Intent.ACTION_VIEW)
                                appIntent.setClassName(
                                    "com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail"
                                )
                                try {
                                    startActivity(appIntent)
                                    finish()
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
                                finish()
                            }

                            "org.thoughtcrime.securesms" -> {
                                ContactGesture.goToSignal(this@NotificationAlarmActivity)
                                finish()
                            }

                            "org.telegram.messenger" -> {
                                if (contact != null) {
                                    ContactGesture.goToTelegram(this@NotificationAlarmActivity, contact.firstPhoneNumber)
                                    finish()
                                } else {
                                    ContactGesture.goToTelegram(this@NotificationAlarmActivity, "")
                                    finish()
                                }
                            }

                            "com.facebook.katana" -> {
                                ContactGesture.openMessenger(
                                    contact?.messengerId ?: "", this@NotificationAlarmActivity
                                )
                                finish()
                            }
                        }
                    }

                    if (alarmSound != null) {
                        alarmSound?.stop()
                    }
                }
                adapter.submitList(it.toList())
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(this@NotificationAlarmActivity)

                binding.apply {
                    contact?.apply {
                        currentNotificationSound = notificationSound
                        currentNotificationTone = notificationTone
                        currentIsCustomSound = isCustomSound == 1

                        soundRingtone()
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(6000)

                        alarmSound?.stop()
                    }

                    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

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

                        if (NotificationsListenerService.notificationsList.size == 1) {
                            NotificationsListenerService.notificationsList.clear()
                            finish()
                        } else {
                            NotificationsListenerService.notificationsList.clear()
                            startActivity(Intent(this@NotificationAlarmActivity, NotificationsHistoryActivity::class.java))
                            finish()
                        }
                    }

                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
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