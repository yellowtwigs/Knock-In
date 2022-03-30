package com.yellowtwigs.knockin.ui.notifications

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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_Alarm_Button_ShutDown: MaterialButton? = null

    private var sbp: StatusBarParcelable? = null

    private var notification_alarm_ReceiveMessageLayout: RelativeLayout? = null
    private var notification_alarm_ReceiveMessageContent: TextView? = null
    private var notification_alarm_ReceiveMessageSender: TextView? = null
    private var notification_alarm_ReceiveMessageImage: AppCompatImageView? = null

    private var alarmSound: MediaPlayer? = null

    private var isSMS = false
    private var currentIsCustomSound = false
    private var currentNotificationSound = 0
    private var currentNotificationTone = ""

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)

        //region ====================================== FindViewById ========================================

        notification_Alarm_Button_ShutDown = findViewById(R.id.notification_alarm_shut_down)

        notification_alarm_ReceiveMessageLayout =
            findViewById(R.id.notification_alarm_receive_message_layout)
        notification_alarm_ReceiveMessageContent =
            findViewById(R.id.notification_alarm_receive_message_content)
        notification_alarm_ReceiveMessageSender =
            findViewById(R.id.notification_alarm_receive_message_sender)
        notification_alarm_ReceiveMessageImage =
            findViewById(R.id.notification_alarm_receive_message_image)

        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras?.get("notification") as StatusBarParcelable

        sbp?.apply {
            notification_alarm_ReceiveMessageSender?.text =
                statusBarNotificationInfo["android.title"] as String
            notification_alarm_ReceiveMessageContent?.text =
                statusBarNotificationInfo["android.text"] as String

            if (notification_alarm_ReceiveMessageContent?.length()!! > 20)
                notification_alarm_ReceiveMessageContent?.text =
                    notification_alarm_ReceiveMessageContent?.text?.substring(0, 19) + ".."
        }

        val contactManager = ContactManager(this.applicationContext)

        val contact = if (sbp?.appNotifier == "com.google.android.gm") {
            contactManager.getContactFromMail(sbp?.statusBarNotificationInfo?.get("android.title") as String)
        } else {
            contactManager.getContact(sbp?.statusBarNotificationInfo?.get("android.title") as String)
        }

        Log.i(
            "getContact",
            "contact name 1 : ${sbp?.statusBarNotificationInfo?.get("android.title")}"
        )
        Log.i("getContact", "contact name 2 : ${contact?.contactDB?.firstName}")
        Log.i("getContact", "is custom sound : ${contact?.contactDB?.isCustomSound}")
        Log.i("getContact", "notification sound : ${contact?.contactDB?.notificationSound}")
        Log.i("getContact", "notification tone : ${contact?.contactDB?.notificationTone}")

        if (contact != null) {
            contact.contactDB?.apply {
                currentNotificationSound = notificationSound
                currentNotificationTone = notificationTone
                currentIsCustomSound = isCustomSound == 1

                soundRingtone()
            }
        }

        when (sbp?.appNotifier) {
            "com.google.android.apps.messaging", "com.android.mms", "com.samsung.android.messaging" -> {
                isSMS = true
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_sms_selector)
            }
            "com.whatsapp" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_circular_whatsapp)
            }
            "com.google.android.gm" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_circular_gmail)
            }
            "com.microsoft.office.outlook" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_circular_mail)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(3500)

            alarmSound?.stop()
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
        //endregion

        sbp?.apply {
            notification_alarm_ReceiveMessageLayout?.setOnClickListener {
                if (isSMS) {
                    if (contact != null) {
                        openSms(contact.getFirstPhoneNumber())
                    } else {
                        openSms(statusBarNotificationInfo["android.title"] as String)
                    }
                } else if (appNotifier == "com.whatsapp") {
                    if (contact != null) {
                        openWhatsapp(contact.getFirstPhoneNumber())
                    } else {
                        openWhatsapp(statusBarNotificationInfo["android.title"] as String)
                    }
                } else if (appNotifier == "com.google.android.gm") {
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
        }

        notification_Alarm_Button_ShutDown?.setOnClickListener {
            if (alarmSound != null) {
                alarmSound?.stop()
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

    private fun soundRingtone() {
        alarmSound?.stop()
        Log.i("getContact", "currentIsCustomSound : ${currentIsCustomSound}")
        Log.i("getContact", "currentNotificationSound : ${currentNotificationSound}")
        Log.i("getContact", "currentNotificationTone : ${currentNotificationTone}")
        alarmSound = if (currentIsCustomSound) {
            MediaPlayer.create(this, Uri.parse(currentNotificationTone))
        } else {
            MediaPlayer.create(this, currentNotificationSound)
        }
        alarmSound?.start()
    }
}
