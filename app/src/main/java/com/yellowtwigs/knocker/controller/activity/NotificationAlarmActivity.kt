package com.yellowtwigs.knocker.controller.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.yellowtwigs.knocker.R
import android.view.WindowManager
import android.widget.TextView
import com.yellowtwigs.knocker.model.StatusBarParcelable
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yellowtwigs.knocker.controller.NotificationAlarmRecyclerViewAdapter
import com.yellowtwigs.knocker.model.ModelDB.NotificationDB


class NotificationAlarmActivity : AppCompatActivity() {

    private var notification_Alarm_Button_ShutDown: MaterialButton? = null

    private var notification_alarm_RecyclerView: RecyclerView? = null
    private var notification_alarm_RecyclerViewAdapter: NotificationAlarmRecyclerViewAdapter? = null
    private var sharedThemePreferences: SharedPreferences? = null

    private var cpt : Int? = null

    private var isOpen = true

//    private var sound : SoundPool

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_alarm)

        sharedThemePreferences = getSharedPreferences("nbOfMessages", Context.MODE_PRIVATE)
        cpt = sharedThemePreferences!!.getInt("nbOfMessages", 1)

        notification_Alarm_Button_ShutDown = findViewById(R.id.notification_alarm_shut_down)
        notification_alarm_RecyclerView = findViewById(R.id.notification_alarm_recycler_view)

        val sbp = intent.extras!!.get("notification") as StatusBarParcelable

        val notification_alarm_ListOfNotification: ArrayList<StatusBarParcelable> = ArrayList()
        notification_alarm_ListOfNotification.add(sbp)

        notification_alarm_RecyclerViewAdapter = NotificationAlarmRecyclerViewAdapter(this, notification_alarm_ListOfNotification, cpt!!)
        notification_alarm_RecyclerView!!.layoutManager = LinearLayoutManager(applicationContext)
        notification_alarm_RecyclerView!!.adapter = notification_alarm_RecyclerViewAdapter

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

        notification_Alarm_Button_ShutDown!!.setOnClickListener {
            this.finish()
            isOpen = false
            // sound.stop()
        }
    }

    override fun onStop() {
        super.onStop()

        cpt = cpt!!.plus(1)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        val edit: SharedPreferences.Editor = sharedThemePreferences!!.edit()
        edit.putInt("nbOfMessages", 1)
        edit.apply()
    }
}
