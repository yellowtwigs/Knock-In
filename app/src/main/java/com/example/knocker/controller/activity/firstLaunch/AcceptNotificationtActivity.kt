package com.example.knocker.controller.activity.firstLaunch

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.knocker.R
import com.example.knocker.controller.activity.MainActivity
import kotlin.concurrent.thread

class AcceptNotificationActivity : AppCompatActivity() {
    var activityVisible: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_notification)

        val ButtonManage: Button = findViewById(R.id.accept_notification_allow_it_button)
        val ButtonUnAuthorized: Button = findViewById(R.id.accept_notification_dismiss_button)

        ButtonManage.setOnClickListener {
            println("test")
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.example.knocker.notificationExemple")
            activityVisible = false
            screenGesture()
        }


        ButtonUnAuthorized.setOnClickListener {
            startActivity(Intent(this@AcceptNotificationActivity, MultiSelectActivity::class.java))
            finish()
        }

    }

    private val isNotificationServiceEnabled: Boolean
        get() {
            val pkgName = packageName
            val str = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(str)) {
                val names = str.split(":")
                for (i in names.indices) {
                    val cn = ComponentName.unflattenFromString(names[i])
                    if (cn != null) {
                        if (TextUtils.equals(pkgName, cn.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

    override fun onResume() {
        super.onResume()
        activityVisible = true
        println("test resume")
    }

    override fun onPause() {
        super.onPause()
        activityVisible = false
        println("test pause")
    }

    override fun onStart() {
        super.onStart()
        activityVisible = true
        println("test start")
    }

    fun screenGesture() {
        val thread = Thread {

            while (!activityVisible && !isNotificationServiceEnabled) {
                println("activity is visible " + activityVisible)
            }
            val sharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            edit.putBoolean("serviceNotif", true)
            edit.apply()
            startActivity(Intent(this@AcceptNotificationActivity, MultiSelectActivity::class.java))
            finish()
        }
        thread.start()
    }

    override fun onBackPressed() {

    }
}
