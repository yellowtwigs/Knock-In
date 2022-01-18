package com.yellowtwigs.knockin.controller.activity

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var sbp: StatusBarParcelable? = null

    private var alarmSound: MediaPlayer? = null

    private var durationPreferences: SharedPreferences? = null
    private var ringtonePreferences: SharedPreferences? = null
    private var canRingtone = false
    private var duration = 0

    private var isSMS = false

    private lateinit var binding: ActivityNotificationAlarmBinding
    private val cxt = this

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ==================================== SharedPreferences =====================================

        val sharedAlarmNotifTonePreferences: SharedPreferences =
            getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

        durationPreferences =
            getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        duration = durationPreferences!!.getInt("Alarm_Notif_Duration", 0)

        ringtonePreferences =
            getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        canRingtone = ringtonePreferences!!.getBoolean("Can_RingTone", false)

        if (canRingtone) {
            val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)
            alertNotificationTone(sound)
        }

        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras?.get("notification") as StatusBarParcelable

        val title = sbp?.statusBarNotificationInfo?.get("android.title") as String
        val content = sbp?.statusBarNotificationInfo?.get("android.text") as String

        val contactManager = ContactManager(this.applicationContext)
        val contact = contactManager.getContact(title)

        binding.apply {
            receiveMessageSender.text = title
            receiveMessageContent.text = content

            if (receiveMessageContent.length() > 20)
                receiveMessageContent.text = receiveMessageContent.text.substring(0, 19) + ".."

            when (sbp?.appNotifier) {
                "com.google.android.apps.messaging",
                "com.android.mms", "com.samsung.android.messaging" -> {
                    isSMS = true
                    receiveMessageImage.setImageResource(R.drawable.ic_sms_selector)
                }

                "com.whatsapp" -> {
                    isSMS = false
                    receiveMessageImage.setImageResource(R.drawable.ic_circular_whatsapp)
                }

                "com.google.android.gm" -> {
                    isSMS = false
                    receiveMessageImage.setImageResource(R.drawable.ic_circular_gmail)
                }
            }
        }

        //endregion

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(duration.toLong())
            canRingtone = true

            val edit: SharedPreferences.Editor = ringtonePreferences!!.edit()
            edit.putBoolean("Can_RingTone", canRingtone)
            edit.apply()
        }

        binding.let {
            it.receiveMessageLayout.setOnClickListener {
                if (isSMS) {
                    if (contact != null) {
                        openSms(contact.getFirstPhoneNumber(), this)
                    } else {
                        openSms(title, this)
                    }
                } else if (sbp?.appNotifier == "com.whatsapp") {
                    if (contact != null) {
                        openWhatsapp(contact.getFirstPhoneNumber(), this)
                    } else {
                        openWhatsapp(title, cxt)
                    }
                } else if (sbp?.appNotifier == "com.google.android.gm") {
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

                if (alarmSound != null) {
                    alarmSound?.stop()
                }

                finish()
            }
            it.shutDown.setOnClickListener {
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

    fun alertNotificationTone(sound: Int) {
        alarmSound?.stop()
        alarmSound = MediaPlayer.create(this, sound)
        alarmSound?.start()
    }
}
