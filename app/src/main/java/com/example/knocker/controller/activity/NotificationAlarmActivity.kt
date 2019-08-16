package com.example.knocker.controller.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.knocker.R
import com.example.knocker.model.StatusBarParcelable
import com.google.android.material.button.MaterialButton


class NotificationAlarmActivity : AppCompatActivity() {

    private var notification_Alarm_Sender_TextView: TextView? = null
    private var notification_Alarm_Content_TextView: TextView? = null
    private var notification_Alarm_Button_close: MaterialButton? = null
    private var notification_Alarm_Button_response: MaterialButton? = null
    private var isOpen = true

    private var notification_alarm_sender: String = ""
    private var notification_alarm_content: String = ""


    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)

        notification_Alarm_Content_TextView = findViewById(R.id.notification_alarm_description)
        notification_Alarm_Sender_TextView = findViewById(R.id.notification_alarm_contact)
        notification_Alarm_Button_close = findViewById(R.id.notification_alarm_floating_button_close)
        notification_Alarm_Button_response = findViewById(R.id.notification_alarm_floating_button_openMessage)

        val sbp = intent.extras.get("notification") as StatusBarParcelable
        notification_alarm_sender = sbp.statusBarNotificationInfo.get("android.title") as String
        notification_alarm_content = sbp.statusBarNotificationInfo.get("android.text") as String

        val notification_AlarmSound = MediaPlayer.create(this, R.raw.slap)

        notification_AlarmSound.start()

        notification_Alarm_Content_TextView!!.setText(notification_alarm_content)
        notification_Alarm_Sender_TextView!!.setText(notification_alarm_sender)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        println("sbp" + sbp.TAG)
        println("I'm into alarm")

        if (Build.VERSION.SDK_INT >= 27) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }

        val vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val thread = Thread {
                val timeWhenLaunch = System.currentTimeMillis()
                while (isOpen && System.currentTimeMillis() - timeWhenLaunch < 10 * 1000) {
                    vibration.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    Thread.sleep(1000)
                    println("test")
                }
                if (System.currentTimeMillis() - timeWhenLaunch > 10 * 1000) {
                    finish()
                }
            }
            thread.start()
        } else {
            //deprecated in API 26
            vibration.vibrate(500)
        }
        //endregion

        notification_Alarm_Button_close!!.setOnClickListener {
            this.finish()
            isOpen = false
        }

        notification_Alarm_Button_response!!.setOnClickListener {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val fullWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "Loneworker - FULL WAKE LOCK")
            val partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerManager.PARTIAL_WAKE_LOCK")

            println("app notifier " + sbp.appNotifier)
            val intent = packageManager.getLaunchIntentForPackage(sbp.appNotifier)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            if (fullWakeLock.isHeld) {
                fullWakeLock.release()
            }
            if (partialWakeLock.isHeld) {
                partialWakeLock.release()
            }
            isOpen = false

            notification_AlarmSound.stop()
            finish()
        }
    }
}
