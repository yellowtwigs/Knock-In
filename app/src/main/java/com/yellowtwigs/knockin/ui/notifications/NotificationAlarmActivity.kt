package com.yellowtwigs.knockin.ui.notifications

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityNotificationAlarmBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.utils.ContactGesture.openSmsNoMessage
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var sbp: StatusBarParcelable? = null

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var durationPreferences: SharedPreferences
    private var duration = 0

    private lateinit var tonePreferences: SharedPreferences
    private lateinit var alarmCustomTonePreferences: SharedPreferences
    private var customSound = ""

    private lateinit var schedulePreferences: SharedPreferences

    private lateinit var canRingtonePreferences: SharedPreferences
    private var canRingtone = false

    private var isSMS = false

    private lateinit var binding: ActivityNotificationAlarmBinding

    //endregion

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //region ==================================== SharedPreferences =====================================

        sharedPreferences = getSharedPreferences("Knockin_preferences", Context.MODE_PRIVATE)
        canRingtonePreferences = getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        durationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        tonePreferences = getSharedPreferences("Alarm_Tone", Context.MODE_PRIVATE)
        alarmCustomTonePreferences = getSharedPreferences("Alarm_Custom_Tone", Context.MODE_PRIVATE)
        schedulePreferences = getSharedPreferences("Schedule_VIP", Context.MODE_PRIVATE)

        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras?.get("notification") as StatusBarParcelable

        val contactManager = ContactManager(this.applicationContext)
        val contact =
            contactManager.getContact(sbp?.statusBarNotificationInfo?.get("android.title") as String)

//        soundRingtone(contact?.contactDB?.notificationSound)

        binding.apply {
            senderName.text = sbp?.statusBarNotificationInfo?.get("android.title") as String
            messageContent.text = sbp?.statusBarNotificationInfo?.get("android.text") as String

            if (messageContent.length() > 20)
                messageContent.text = messageContent.text.substring(0, 19) + ".."

            when (sbp?.appNotifier) {
                "com.google.android.apps.messaging",
                "com.android.mms", "com.samsung.android.messaging" -> {
                    isSMS = true
                    messageIcon.setImageResource(R.drawable.ic_sms_selector)
                }

                "com.whatsapp" -> {
                    isSMS = false
                    messageIcon.setImageResource(R.drawable.ic_circular_whatsapp)
                }

                "com.google.android.gm" -> {
                    isSMS = false
                    messageIcon.setImageResource(R.drawable.ic_circular_gmail)
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

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                canRingtone = true

                val edit: SharedPreferences.Editor = canRingtonePreferences.edit()
                edit.putBoolean("Can_RingTone", canRingtone)
                edit.apply()

                handler.postDelayed(this, duration.toLong())
                mediaPlayer?.stop()
            }
        }, duration.toLong())

        binding.messageContent.setOnClickListener {
            if (isSMS) {
                if (contact != null) {
                    openSmsNoMessage(contact.getFirstPhoneNumber(), this)
                } else {
                    openSmsNoMessage(sbp?.statusBarNotificationInfo?.get("android.title") as String, this)
                }
            } else if (sbp?.appNotifier == "com.whatsapp") {
                if (contact != null) {
                    openWhatsapp(contact.getFirstPhoneNumber(), this)
                } else {
                    openWhatsapp(sbp?.statusBarNotificationInfo?.get("android.title") as String, this)
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

            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
            }

            finish()
        }

        binding.shutdownButton.setOnClickListener {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
            }
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun soundRingtone(idSound: Int?) {
        duration = durationPreferences.getInt("Alarm_Notif_Duration", 4000)
        canRingtone = canRingtonePreferences.getBoolean("Can_RingTone", true)
        customSound = alarmCustomTonePreferences.getString("Alarm_Custom_Tone", "").toString()

        if (canRingtone) {
            if (idSound != null) {
                if (idSound != R.raw.sms_ring) {
                    if (idSound == -1 && customSound.isNotEmpty()) {
                        alertCustomNotificationTone(customSound)
                    } else {
                        alertNotificationTone(idSound)
                    }
                } else {
                    alertNotificationTone(R.raw.sms_ring)
                }
            }
        }
    }

    private fun alertNotificationTone(sound: Int) {
        mediaPlayer?.stop()
        mediaPlayer = if (sound == -1) {
            MediaPlayer.create(this, R.raw.sms_ring)
        } else {
            MediaPlayer.create(this, sound)
        }
        mediaPlayer?.start()

        val editDuration = durationPreferences.edit()
        NotificationListener.alarmSound?.duration?.let {
            editDuration.putInt(
                "Alarm_Notif_Duration",
                it
            )
        }
        editDuration.apply()

        val editCanRingtone = canRingtonePreferences.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()
    }

    private fun alertCustomNotificationTone(customSound: String) {
        mediaPlayer?.stop()
        mediaPlayer = MediaPlayer.create(applicationContext, Uri.parse(customSound))
        mediaPlayer?.start()

        val editDuration = durationPreferences.edit()
        NotificationListener.alarmSound?.duration?.let {
            editDuration.putInt(
                "Alarm_Notif_Duration",
                it
            )
        }
        editDuration.apply()

        val editCanRingtone = canRingtonePreferences.edit()
        editCanRingtone.putBoolean("Can_RingTone", canRingtone)
        editCanRingtone.apply()
    }
}
