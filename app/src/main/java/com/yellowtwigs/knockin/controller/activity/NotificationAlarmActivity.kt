package com.yellowtwigs.knockin.controller.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_Alarm_Button_ShutDown: MaterialButton? = null

    private var sbp: StatusBarParcelable? = null

    private var notification_alarm_ReceiveMessageLayout: RelativeLayout? = null
    private var notification_alarm_ReceiveMessageContent: TextView? = null
    private var notification_alarm_ReceiveMessageSender: TextView? = null
    private var notification_alarm_ReceiveMessageImage: AppCompatImageView? = null

    private var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

    private var sharedAlarmNotifDurationPreferences: SharedPreferences? = null
    private var duration = 0

    private var sharedAlarmNotifCanRingtonePreferences: SharedPreferences? = null
    private var canRingtone = false

    private var isSMS = false

    //endregion

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)
        println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKOOOOOOKKKKKKKKKKKKK")

        //region ==================================== SharedPreferences =====================================

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

        sharedAlarmNotifDurationPreferences = getSharedPreferences("Alarm_Notif_Duration", Context.MODE_PRIVATE)
        duration = sharedAlarmNotifDurationPreferences!!.getInt("Alarm_Notif_Duration", 0)

        sharedAlarmNotifCanRingtonePreferences = getSharedPreferences("Can_RingTone", Context.MODE_PRIVATE)
        canRingtone = sharedAlarmNotifCanRingtonePreferences!!.getBoolean("Can_RingTone", false)
/////////
        if (canRingtone) {
            val sound = sharedAlarmNotifTonePreferences.getInt("Alarm_Notif_Tone", R.raw.sms_ring)
            alartNotifTone(sound)
        }

        //endregion

        //region ====================================== FindViewById ========================================

        notification_Alarm_Button_ShutDown = findViewById(R.id.notification_alarm_shut_down)

        notification_alarm_ReceiveMessageLayout = findViewById(R.id.notification_alarm_receive_message_layout)
        notification_alarm_ReceiveMessageContent = findViewById(R.id.notification_alarm_receive_message_content)
        notification_alarm_ReceiveMessageSender = findViewById(R.id.notification_alarm_receive_message_sender)
        notification_alarm_ReceiveMessageImage = findViewById(R.id.notification_alarm_receive_message_image)

        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras!!.get("notification") as StatusBarParcelable

        val sbp = intent.extras!!.get("notification") as StatusBarParcelable

        notification_alarm_ReceiveMessageSender!!.text = sbp.statusBarNotificationInfo["android.title"] as String
        notification_alarm_ReceiveMessageContent!!.text = sbp.statusBarNotificationInfo["android.text"] as String

        if (notification_alarm_ReceiveMessageContent!!.length() > 20)
            notification_alarm_ReceiveMessageContent!!.text = notification_alarm_ReceiveMessageContent!!.text.substring(0, 19) + ".."

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

            "com.google.android.gm" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage!!.setImageResource(R.drawable.ic_circular_gmail)
            }
        }

        //endregion

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        println("I'm into alarm")

        if (Build.VERSION.SDK_INT >= 27) {
            println("2222222222222222222777777777777777777777777")
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            println("NOOOOOOOOOT INTOOOOOOOOO THE GOOOOOOOOOOOOOOOOOD")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }

        //region sound + vibration
        /* sound.start()
         sound.isLooping=true*/

//        val vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val thread = Thread {
//                val timeWhenLaunch = System.currentTimeMillis()
//                while (System.currentTimeMillis() - timeWhenLaunch < 20 * 1000) {
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

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                canRingtone = true

                val edit: SharedPreferences.Editor = sharedAlarmNotifCanRingtonePreferences!!.edit()
                edit.putBoolean("Can_RingTone", canRingtone)
                edit.apply()

                handler.postDelayed(this, duration.toLong())
            }
        }, duration.toLong())

        //endregion

        notification_alarm_ReceiveMessageLayout!!.setOnClickListener {
            if (isSMS) {
                if (contact != null) {
                    openSms(contact.getFirstPhoneNumber())
                } else {
                    openSms(sbp.statusBarNotificationInfo["android.title"] as String)
                }
            } else if (sbp.appNotifier == "com.whatsapp") {
                if (contact != null) {
                    openWhatsapp(contact.getFirstPhoneNumber())
                } else {
                    openWhatsapp(sbp.statusBarNotificationInfo["android.title"] as String)
                }
            } else if (sbp.appNotifier == "com.google.android.gm") {
                val appIntent = Intent(Intent.ACTION_VIEW)
                appIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
                try {
                    startActivity(appIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://gmail.com/")))
                }
            }

            if (notification_alarm_NotificationMessagesAlarmSound != null) {
                notification_alarm_NotificationMessagesAlarmSound!!.stop()
            }

            finish()
        }

        notification_Alarm_Button_ShutDown!!.setOnClickListener {

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

    fun alartNotifTone(sound : Int){
        notification_alarm_NotificationMessagesAlarmSound?.stop()
        notification_alarm_NotificationMessagesAlarmSound = MediaPlayer.create(this, sound)
        notification_alarm_NotificationMessagesAlarmSound!!.start()
    }
}
