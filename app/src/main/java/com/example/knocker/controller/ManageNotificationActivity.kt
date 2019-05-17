package com.example.knocker.controller

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.example.knocker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManageNotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_notification)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val switchPopupNotif = this.findViewById<Switch>(R.id.switch_stop_popup)
        val switchservice = this.findViewById<Switch>(R.id.switch_stop_service)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_manage_notification)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        //actionbar!!.setDisplayHomeAsUpEnabled(true)
        //actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar!!.title = " \t \t \t \t Notifications"
        var retour: ImageView = findViewById(R.id.imageView_notification_manager)
        retour.setOnClickListener() {
            this.finish()
        }
        switchPopupNotif.setChecked(sharedPreferences.getBoolean("popupNotif", false))
        switchservice.setChecked(sharedPreferences.getBoolean("serviceNotif", true))
        switchPopupNotif.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchPopupNotif.isChecked) {
                switchservice.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", false)
                edit.putBoolean("popupNotif", true)
                edit.apply()
                System.out.println("pop up true " + sharedPreferences.getBoolean("popupNotif", false))
            } else {
                edit.remove("popupNotif")
                edit.putBoolean("popupNotif", false)
                edit.apply()
                System.out.println("pop up false" + sharedPreferences.getBoolean("popupNotif", false))
            }
        }
        switchservice.setOnCheckedChangeListener { _, _ ->
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            if (switchservice.isChecked) {
                switchPopupNotif.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", true)
                edit.putBoolean("popupNotif", false)
                edit.apply()
                System.out.println("service economy true " + sharedPreferences.getBoolean("serviceNotif", true))
            } else {
                if (!isNotificationServiceEnabled) {
                    buildNotificationServiceAlertDialog().show()
                }
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif", false)
                edit.commit()
                System.out.println("service economy false " + sharedPreferences.getBoolean("serviceNotif", true))
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun buildNotificationServiceAlertDialog(): androidx.appcompat.app.AlertDialog {
        val inflater: LayoutInflater = this.layoutInflater
        val alertView: View = inflater.inflate(R.layout.alert_dialog_notification, null)

        val manage_notif_ButtonAlertDialogAllow = alertView.findViewById<Button>(R.id.button_alert_dialog_allow_it)
        manage_notif_ButtonAlertDialogAllow.setOnClickListener { positiveFloatingDeleteButtonClick() }

        val manage_notif_ButtonAlertDialogDismiss = alertView.findViewById<Button>(R.id.tv_alert_dialog_dismiss)
        manage_notif_ButtonAlertDialogDismiss.setOnClickListener { negativeFloatingDeleteButtonClick() }

        return MaterialAlertDialogBuilder(this)
                .setView(alertView)
                .show()
    }

    private fun positiveFloatingDeleteButtonClick() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.firsttestknocker.notificationExemple")
    }

    private fun negativeFloatingDeleteButtonClick() {

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
}