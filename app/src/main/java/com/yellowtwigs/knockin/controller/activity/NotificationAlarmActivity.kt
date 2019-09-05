package com.yellowtwigs.knockin.controller.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.model.StatusBarParcelable


class NotificationAlarmActivity : AppCompatActivity() {

    //region ========================================== Val or Var ==========================================

    private var notification_Alarm_Button_ShutDown: MaterialButton? = null

    private var notification_alarm_RecyclerView: RecyclerView? = null
    private var sharedNbMessagesPreferences: SharedPreferences? = null

    private var sbp: StatusBarParcelable? = null

    private var notification_alarm_ReceiveSMSLayout: RelativeLayout? = null
    private var notification_alarm_ReceiveWhatsappMessageLayout: RelativeLayout? = null
    private var notification_alarm_ReceiveWhatsappMessageLayout_2: RelativeLayout? = null

    private var notification_alarm_ReceiveSMSTextView: TextView? = null
    private var notification_alarm_ReceiveWhatsappMessageTextView: TextView? = null
    private var notification_alarm_ReceiveWhatsappMessageTextView_2: TextView? = null

    private var notification_alarm_NotificationMessagesAlarmSound: MediaPlayer? = null

    private var cptSMS: Int? = null
    private var cptWhatsappMSG: Int? = null

    private var isOpen = true

    private val ADMIN_INTENT = 15

    //endregion

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)

        //region ==================================== SharedPreferences =====================================

        sharedNbMessagesPreferences = getSharedPreferences("nbOfSMS", Context.MODE_PRIVATE)
        sharedNbMessagesPreferences = getSharedPreferences("nbOfWhatsappMsg", Context.MODE_PRIVATE)
        cptSMS = sharedNbMessagesPreferences!!.getInt("nbOfSMS", 0)
        cptWhatsappMSG = sharedNbMessagesPreferences!!.getInt("nbOfWhatsappMsg", 0)

        val sharedAlarmNotifTonePreferences: SharedPreferences = getSharedPreferences("Alarm_Notif_Tone", Context.MODE_PRIVATE)

        //endregion

        //region ====================================== FindViewById ========================================

        notification_Alarm_Button_ShutDown = findViewById(R.id.notification_alarm_shut_down)

        notification_alarm_ReceiveSMSLayout = findViewById(R.id.notification_alarm_receive_sms_layout)
        notification_alarm_ReceiveWhatsappMessageLayout = findViewById(R.id.notification_alarm_receive_whatsapp_layout)
        notification_alarm_ReceiveWhatsappMessageLayout_2 = findViewById(R.id.notification_alarm_receive_whatsapp_layout_2)

        notification_alarm_ReceiveSMSTextView = findViewById(R.id.notification_alarm_receive_sms_message)
        notification_alarm_ReceiveWhatsappMessageTextView = findViewById(R.id.notification_alarm_receive_whatsapp_message)
        notification_alarm_ReceiveWhatsappMessageTextView_2 = findViewById(R.id.notification_alarm_receive_whatsapp_message_2)

        //endregion

        //region ================================== StatusBarParcelable =====================================

        sbp = intent.extras!!.get("notification") as StatusBarParcelable
        val contact_id = intent.extras!!.get("contact_id") as Int

        val gestionnaireContacts = ContactManager(this)
        val contact = gestionnaireContacts.getContactById(contact_id)

        if (sbp!!.appNotifier == "com.google.android.apps.messaging"
                || sbp!!.appNotifier == "com.android.mms" || sbp!!.appNotifier == "com.samsung.android.messaging") {

            notification_alarm_ReceiveSMSLayout!!.visibility = View.VISIBLE

            if (cptWhatsappMSG!! > 0) {

                if (cptWhatsappMSG!! == 1) {
                    notification_alarm_ReceiveWhatsappMessageTextView!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_message_received)
                } else {
                    notification_alarm_ReceiveWhatsappMessageTextView!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_messages_received)
                }
                notification_alarm_ReceiveWhatsappMessageLayout!!.visibility = View.VISIBLE
            }

            when (cptSMS) {
                0 -> {
                    cptSMS = cptSMS!!.plus(1)

                    notification_alarm_ReceiveSMSTextView!!.text = cptSMS!!.toString() + " " + getString(R.string.notification_alarm_message_received)

                    val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()

                    edit.putInt("nbOfSMS", cptSMS!!)
                    edit.apply()
                }
                cptSMS -> {
                    cptSMS = cptSMS!!.plus(1)

                    notification_alarm_ReceiveSMSTextView!!.text = cptSMS!!.toString() + " " + getString(R.string.notification_alarm_messages_received)

                    val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()

                    edit.putInt("nbOfSMS", cptSMS!!)
                    edit.apply()
                }
            }
        }

        if (sbp!!.appNotifier == "com.whatsapp") {

            if (cptSMS!! > 0) {
                notification_alarm_ReceiveSMSLayout!!.visibility = View.VISIBLE

                if (cptSMS!! == 1) {
                    notification_alarm_ReceiveSMSTextView!!.text = cptSMS!!.toString() + " " + getString(R.string.notification_alarm_message_received)
                } else {
                    notification_alarm_ReceiveSMSTextView!!.text = cptSMS!!.toString() + " " + getString(R.string.notification_alarm_messages_received)
                }
                notification_alarm_ReceiveWhatsappMessageLayout!!.visibility = View.VISIBLE
            } else {
                notification_alarm_ReceiveWhatsappMessageLayout_2!!.visibility = View.VISIBLE
            }

            when (cptWhatsappMSG) {
                0 -> {
                    cptWhatsappMSG = cptWhatsappMSG!!.plus(1)

                    if (notification_alarm_ReceiveSMSLayout!!.visibility == View.GONE) {
                        notification_alarm_ReceiveWhatsappMessageTextView_2!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_message_received)
                    } else {
                        notification_alarm_ReceiveWhatsappMessageTextView!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_message_received)
                    }

                    val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()

                    edit.putInt("nbOfWhatsappMsg", cptWhatsappMSG!!)
                    edit.apply()
                }
                cptWhatsappMSG -> {
                    cptWhatsappMSG = cptWhatsappMSG!!.plus(1)

                    if (notification_alarm_ReceiveSMSLayout!!.visibility == View.GONE) {
                        notification_alarm_ReceiveWhatsappMessageTextView_2!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_messages_received)
                    } else {
                        notification_alarm_ReceiveWhatsappMessageTextView!!.text = cptWhatsappMSG!!.toString() + " " + getString(R.string.notification_alarm_messages_received)
                    }

                    val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()

                    edit.putInt("nbOfWhatsappMsg", cptWhatsappMSG!!)
                    edit.apply()
                }
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

        notification_alarm_ReceiveSMSLayout!!.setOnClickListener {
            if (contact != null) {
                openSms(contact.getFirstPhoneNumber())
            } else {
                openSms(sbp!!.statusBarNotificationInfo["android.title"] as String)
            }

            val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
            edit.putInt("nbOfSMS", 0)
            edit.putInt("nbOfWhatsappMsg", 0)
            edit.apply()
            isOpen = false
            // sound.stop()

            finish()
        }

        notification_alarm_ReceiveWhatsappMessageLayout!!.setOnClickListener {
            if (contact != null) {
                openWhatsapp(contact.getFirstPhoneNumber())
            } else {
                openWhatsapp(sbp!!.statusBarNotificationInfo["android.title"] as String)
            }

            val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
            edit.putInt("nbOfSMS", 0)
            edit.putInt("nbOfWhatsappMsg", 0)
            edit.apply()
            isOpen = false
            // sound.stop()

            finish()
        }

        notification_alarm_ReceiveWhatsappMessageLayout_2!!.setOnClickListener {
            if (contact != null) {
                openWhatsapp(contact.getFirstPhoneNumber())
            } else {
                openWhatsapp(sbp!!.statusBarNotificationInfo["android.title"] as String)
            }

            val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
            edit.putInt("nbOfSMS", 0)
            edit.putInt("nbOfWhatsappMsg", 0)
            edit.apply()
            isOpen = false
            // sound.stop()

            finish()
        }

        notification_Alarm_Button_ShutDown!!.setOnClickListener {
            val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
            edit.putInt("nbOfSMS", 0)
            edit.putInt("nbOfWhatsappMsg", 0)
            edit.apply()
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

        val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
        edit.putInt("nbOfSMS", 0)
        edit.putInt("nbOfWhatsappMsg", 0)
        edit.apply()

        startActivity(intent)
        finish()
    }

    private fun openWhatsapp(phoneNumber: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        val message = "phone=" + converter06To33(phoneNumber)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$message")

        val edit: SharedPreferences.Editor = sharedNbMessagesPreferences!!.edit()
        edit.putInt("nbOfSMS", 0)
        edit.putInt("nbOfWhatsappMsg", 0)
        edit.apply()

        startActivity(intent)
        finish()
    }

    private fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33 $phoneNumber"
        } else phoneNumber
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADMIN_INTENT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(applicationContext, "Registered As Admin", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Failed to register as Admin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        finish()
    }
}
