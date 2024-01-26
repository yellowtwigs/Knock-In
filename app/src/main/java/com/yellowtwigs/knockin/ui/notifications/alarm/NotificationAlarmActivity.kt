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
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
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
    private var adapter: NotificationsAlarmListAdapter? = null

    private var googleMail = false
    private var comeback = false
    private var ringTheSound = true

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val editContactViewModel: EditContactViewModel by viewModels()
            val sbp = intent.extras?.get("notification") as StatusBarParcelable

            Log.i("OpenGmail", "NotificationsListenerService.notificationsList : ${NotificationsListenerService.notificationsList}")

            editContactViewModel.getSingleContactByIdLiveData(sbp.contactId).observe(this@NotificationAlarmActivity) { contact ->
                NotificationsListenerService.notificationsList.distinctBy { notificationAlarmViewState ->
                    PopupNotificationParams(
                        contactName = notificationAlarmViewState.title!!,
                        description = notificationAlarmViewState.content!!,
                        platform = notificationAlarmViewState.platform!!,
                        date = notificationAlarmViewState.dateTime,
                    )
                }.let {
                    adapter = NotificationsAlarmListAdapter(this@NotificationAlarmActivity) { isSMS, sender, appNotifier, newSbp ->
                        contact?.firstPhoneNumber ?: listOf(PhoneNumberWithSpinner(null, ""))

                        Log.i("OpenGmail", "NotificationsListenerService.notificationsList : ${NotificationsListenerService.notificationsList}")

                        NotificationsListenerService.notificationsList.remove(newSbp)

                        adapter?.submitList(null)
                        adapter?.submitList(NotificationsListenerService.notificationsList.toList())

                        if (isSMS) {
                            if (contact != null) {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", contact.firstPhoneNumber, null))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                                startActivity(intent)
                                if (NotificationsListenerService.notificationsList.size == 0) {
                                    finish()
                                }
                            } else {
                                openSms(sender, this@NotificationAlarmActivity)
                            }
                        } else {
                            when (appNotifier) {
                                "com.whatsapp" -> {
                                    if (contact != null) {
                                        val url = "https://api.whatsapp.com/send?phone=${converter06To33(converter06To33(contact.firstPhoneNumber))}"

                                        val i = Intent(Intent.ACTION_VIEW)
                                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        i.data = Uri.parse(url)
                                        startActivity(i)

                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            Log.i("AlarmVIP", "Passe par là")
                                            finish()
                                        }
                                    } else {
                                        openWhatsapp(sender, this@NotificationAlarmActivity)
                                    }
                                }

                                "com.google.android.gm" -> {
                                    googleMail = true
                                    val packageName = "com.google.android.gm"
                                    val intent = packageManager.getLaunchIntentForPackage(packageName)

                                    if (intent != null) {
                                        startActivityForResult(intent, REQUEST_GMAIL)
                                    } else {
                                        val playStoreIntent = Intent(Intent.ACTION_VIEW)
                                        playStoreIntent.data = Uri.parse("market://details?id=$packageName")

                                        try {
                                            startActivity(playStoreIntent)
                                        } catch (e: ActivityNotFoundException) {
                                        }
                                    }

                                }

                                "com.microsoft.office.outlook" -> {
                                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("ms-outlook://emails"))
                                    try {
                                        startActivity(appIntent)
                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    } catch (e: ActivityNotFoundException) {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://outlook.com/")))

                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    }
                                }

                                "org.thoughtcrime.securesms" -> {
                                    val appIntent = packageManager.getLaunchIntentForPackage("org.thoughtcrime.securesms")
                                    try {
                                        startActivity(appIntent)

                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    } catch (e: ActivityNotFoundException) {
                                        Log.i("resolveInfoList", "$e")
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.org/")))
                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    }
                                    ContactGesture.goToSignal(this@NotificationAlarmActivity)
                                }

                                "org.telegram.messenger" -> {
                                    if (contact != null) {
                                        ContactGesture.goToTelegram(this@NotificationAlarmActivity, contact.firstPhoneNumber)

                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    } else {
                                        ContactGesture.goToTelegram(this@NotificationAlarmActivity, "")

                                        if (NotificationsListenerService.notificationsList.size == 0) {
                                            finish()
                                        }
                                    }
                                }

                                "com.facebook.katana" -> {
                                    ContactGesture.openMessenger(contact?.messengerId ?: "", this@NotificationAlarmActivity)
                                }
                            }
                        }

                        if (alarmSound != null) {
                            alarmSound?.stop()
                        }
                    }
                    adapter?.submitList(null)
                    adapter?.submitList(it.toList())
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

                            if (NotificationsListenerService.notificationsList.size >= 1) {
                                startActivity(Intent(this@NotificationAlarmActivity, NotificationsHistoryActivity::class.java))
                            }

                            NotificationsListenerService.notificationsList.clear()
                            finish()
                        }

                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_GMAIL) {
            Log.i("OpenGmail", "REQUEST_GMAIL")
        }
    }

    override fun onResume() {
        super.onResume()

        Log.i("OpenGmail", "comeback : $googleMail")
        Log.i("OpenGmail", "NotificationsListenerService.notificationsList.size : ${NotificationsListenerService.notificationsList.size}")

        if (NotificationsListenerService.notificationsList.size == 0 && !googleMail) {
            Log.i("OpenGmail", "Passe par là 2")
            startActivity(Intent(this, ContactsListActivity::class.java))
            finish()
        }

        if (comeback) {
            startActivity(Intent(this, ContactsListActivity::class.java))
            finish()
        }

        if (NotificationsListenerService.notificationsList.size == 0 && googleMail) {
            comeback = true
        }
    }

    private fun soundRingtone() {
        Log.i("SoundRingtone", "ringTheSound : $ringTheSound")
        if (ringTheSound) {
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
            ringTheSound = false
        }
    }

    companion object {
        const val REQUEST_GMAIL = 123
    }
}