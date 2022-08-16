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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.StatusBarParcelable
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.goToOutlook
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.openMessenger
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
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

//        val contactManager = ContactManager(this.applicationContext)

//        val contact = if (sbp?.appNotifier == "com.google.android.gm") {
//            contactManager.getContactFromMail(sbp?.statusBarNotificationInfo?.get("android.title") as String)
//        } else {
//            contactManager.getContact(sbp?.statusBarNotificationInfo?.get("android.title") as String)
//        }

//        if (contact != null) {
//            contact.contactDB?.apply {
//                currentNotificationSound = notificationSound
//                currentNotificationTone = notificationTone
//                currentIsCustomSound = isCustomSound == 1
//
//                soundRingtone()
//            }
//        }

        Log.i("appNotifier", "${sbp?.appNotifier}")

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
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_outlook)
            }
            "org.thoughtcrime.securesms" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_circular_signal)
            }
            "org.telegram.messenger" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_telegram)
            }
            "com.facebook.katana" -> {
                isSMS = false
                notification_alarm_ReceiveMessageImage?.setImageResource(R.drawable.ic_circular_messenger)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(6000)

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

//        sbp?.apply {
//            notification_alarm_ReceiveMessageLayout?.setOnClickListener {
//                if (isSMS) {
//                    if (contact != null) {
//                        openSms(contact.getFirstPhoneNumber(), this@NotificationAlarmActivity)
//                    } else {
//                        openSms(
//                            statusBarNotificationInfo["android.title"] as String,
//                            this@NotificationAlarmActivity
//                        )
//                    }
//                } else {
//                    when (appNotifier) {
//                        "com.whatsapp" -> {
//                            if (contact != null) {
//                                openWhatsapp(
//                                    contact.getFirstPhoneNumber(),
//                                    this@NotificationAlarmActivity
//                                )
//                            } else {
//                                openWhatsapp(
//                                    statusBarNotificationInfo["android.title"] as String,
//                                    this@NotificationAlarmActivity
//                                )
//                            }
//                        }
//                        "com.google.android.gm" -> {
//                            val appIntent = Intent(Intent.ACTION_VIEW)
//                            appIntent.setClassName(
//                                "com.google.android.gm",
//                                "com.google.android.gm.ConversationListActivityGmail"
//                            )
//                            try {
//                                startActivity(appIntent)
//                            } catch (e: ActivityNotFoundException) {
//                                startActivity(
//                                    Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse("https://gmail.com/")
//                                    )
//                                )
//                            }
//                        }
//                        "com.microsoft.office.outlook" -> {
//                            goToOutlook(this@NotificationAlarmActivity)
//                        }
//                        "org.thoughtcrime.securesms" -> {
//                            goToSignal(this@NotificationAlarmActivity)
//                        }
//                        "org.telegram.messenger" -> {
//                            if (contact != null) {
//                                Log.i("openuTeleguramu", "${contact.contactDB?.firstName} ${contact.contactDB?.lastName}")
////                                goToTelegram(
////                                    this@NotificationAlarmActivity,
////                                    "${contact.contactDB?.firstName} ${contact.contactDB?.lastName}"
////                                )
//                            } else {
//                                goToTelegram(this@NotificationAlarmActivity, "")
//                            }
//                        }
//                        "com.facebook.katana" -> {
//                            if (contact != null) {
//                                openMessenger(
//                                    contact.getMessengerID(),
//                                    this@NotificationAlarmActivity
//                                )
//                            } else {
//                                openMessenger("", this@NotificationAlarmActivity)
//                            }
//                        }
//                    }
//                }
//
//                if (alarmSound != null) {
//                    alarmSound?.stop()
//                }
//
//                finish()
//            }
//        }

        notification_Alarm_Button_ShutDown?.setOnClickListener {
            if (alarmSound != null) {
                alarmSound?.stop()
            }

            finish()
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
            MediaPlayer.create(this, currentNotificationSound)
        }
        alarmSound?.start()
    }
}