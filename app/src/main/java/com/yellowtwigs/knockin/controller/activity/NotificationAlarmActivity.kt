package com.yellowtwigs.knockin.controller.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_Alarm_Button_ShutDown: MaterialButton? = null

    private var sharedNbMessagesPreferences: SharedPreferences? = null

    private var sbp: StatusBarParcelable? = null

    private var notification_alarm_ReceiveMessageLayout: RelativeLayout? = null
    private var notification_alarm_ReceiveMessageContent: TextView? = null
    private var notification_alarm_ReceiveMessageSender: TextView? = null
    private var notification_alarm_ReceiveMessageImage: AppCompatImageView? = null

    private var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

    private var isSMS = false

    private var isOpen = true

    //endregion

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)

        //region ==================================== SharedPreferences =====================================

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

        //endregion

        //region ====================================== FindViewById ========================================

        notification_Alarm_Button_ShutDown = findViewById(R.id.notification_alarm_shut_down)

        notification_alarm_ReceiveMessageLayout = findViewById(R.id.notification_alarm_receive_sms_layout)
        notification_alarm_ReceiveMessageContent = findViewById(R.id.notification_alarm_receive_sms_layout)
        notification_alarm_ReceiveMessageSender = findViewById(R.id.notification_alarm_receive_sms_layout)
        notification_alarm_ReceiveMessageImage = findViewById(R.id.notification_alarm_receive_sms_layout)


        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras!!.get("notification") as StatusBarParcelable

        val sbp = intent.extras!!.get("notification") as StatusBarParcelable
        notification_alarm_ReceiveMessageSender!!.text = sbp.statusBarNotificationInfo["android.title"] as String
        notification_alarm_ReceiveMessageContent!!.text = sbp.statusBarNotificationInfo["android.text"] as String

        val gestionnaireContacts = ContactManager(this.applicationContext)
        val contact = gestionnaireContacts.getContact(sbp.statusBarNotificationInfo["android.title"] as String)

        when (sbp.appNotifier) {
            "com.google.android.apps.messaging",
            "com.android.mms", "com.samsung.android.messaging" -> {
                isSMS = true
                notification_alarm_ReceiveMessageImage!!.setImageResource(R.drawable.ic_sms_selector)
            }

            "com.whatsapp" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage!!.setImageResource(R.drawable.ic_circular_whatsapp)
            }
        }

        //endregion

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
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

        //region sound + vibration
        /* sound.start()
         sound.isLooping=true*/

        when (sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", 1)) {
            R.raw.bass_slap -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.bass_slap)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.xylophone_tone -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.xylophone_tone)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.piano_sms -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.piano_sms)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.electric_blues -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.electric_blues)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.caravan -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.caravan)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
            R.raw.sax_sms -> {
                if (notification_alarm_NotificationMessagesAlarmSound != null) {
                    notification_alarm_NotificationMessagesAlarmSound!!.stop()
                }
                notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, R.raw.sax_sms)
                notification_alarm_NotificationMessagesAlarmSound!!.start()
            }
        }

//        val vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val thread = Thread {
//                val timeWhenLaunch = System.currentTimeMillis()
//                while (isOpen && System.currentTimeMillis() - timeWhenLaunch < 20 * 1000) {
//                    vibration.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
//                    Thread.sleep(1000)
//                }
//                finish()
//
//            }
//            thread.start()
//        } else {
//            //deprecated in API 26
//            vibration.vibrate(500)
//        }

        //endregion

        notification_alarm_ReceiveMessageLayout!!.setOnClickListener {
            if (isSMS) {
                if (contact != null) {
                    openSms(contact.getFirstPhoneNumber())
                } else {
                    openSms(sbp.statusBarNotificationInfo["android.title"] as String)
                }
            } else {
                if (contact != null) {
                    openWhatsapp(contact.getFirstPhoneNumber())
                } else {
                    openWhatsapp(sbp.statusBarNotificationInfo["android.title"] as String)
                }
            }

            if (notification_alarm_NotificationMessagesAlarmSound != null) {
                notification_alarm_NotificationMessagesAlarmSound!!.stop()
            }

            finish()
        }

        notification_Alarm_Button_ShutDown!!.setOnClickListener {
            isOpen = false

            if (notification_alarm_NotificationMessagesAlarmSound != null) {
                notification_alarm_NotificationMessagesAlarmSound!!.stop()
            }

            finish()
        }
    }

    private fun openSms(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phoneNumber, null))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
        finish()
    }

    private fun openWhatsapp(phoneNumber: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message")

        startActivity(intent)
        finish()
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33 $phoneNumber"
        } else phoneNumber
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
