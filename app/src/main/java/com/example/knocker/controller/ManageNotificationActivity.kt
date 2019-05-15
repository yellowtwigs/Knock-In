package com.example.knocker.controller

import android.app.AlertDialog
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.example.knocker.R

class ManageNotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_notification)
        val sharedPreferences: SharedPreferences = getSharedPreferences("Knocker_preferences", Context.MODE_PRIVATE)
        val switchPopupNotif = this.findViewById<Switch>(R.id.switch_stop_popup)
        val switchservice =this.findViewById<Switch>(R.id.switch_stop_service)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_manage_notification)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        //actionbar!!.setDisplayHomeAsUpEnabled(true)
        //actionbar.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        actionbar!!.title = " \t \t \t \t Notifications"
        var retour: ImageView = findViewById(R.id.imageView_notification_manager)
        retour.setOnClickListener(){
            this.finish()
        }
        switchPopupNotif.setChecked(sharedPreferences.getBoolean("popupNotif",false))
        switchservice.setChecked(sharedPreferences.getBoolean("serviceNotif",true))
        switchPopupNotif.setOnCheckedChangeListener{ _, _ ->
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            if(switchPopupNotif.isChecked){
                switchservice.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",false)
                edit.putBoolean("popupNotif",true)
                edit.commit()
                System.out.println("pop up true "+ sharedPreferences.getBoolean("popupNotif",false))
            }else{
                edit.remove("popupNotif")
                edit.putBoolean("popupNotif",false)
                edit.commit()
                System.out.println("pop up false"+ sharedPreferences.getBoolean("popupNotif",false))
            }
        }
        switchservice.setOnCheckedChangeListener{ _, _ ->
            val edit : SharedPreferences.Editor = sharedPreferences.edit()
            if(switchservice.isChecked){
                switchPopupNotif.setChecked(false)
                edit.remove("popupNotif")
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",true)
                edit.putBoolean("popupNotif",false)
                edit.commit()
                System.out.println("service economy true "+ sharedPreferences.getBoolean("serviceNotif",true))
            }else{
                if (!isNotificationServiceEnabled) {
                    buildNotificationServiceAlertDialog().show()
                }
                edit.remove("serviceNotif")
                edit.putBoolean("serviceNotif",false)
                edit.commit()
                System.out.println("service economy false "+ sharedPreferences.getBoolean("serviceNotif",true))
            }
        }
        
    }
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = this.getLayoutInflater()
        val alertView: View = inflater.inflate(R.layout.alert_dialog_notification,null);
        alertDialogBuilder.setView(alertView);
        val alertDialog = alertDialogBuilder.create()
        val tvNo= alertView.findViewById<TextView>(R.id.tv_alert_dialog)
        tvNo.setOnClickListener {
            alertDialog.cancel()
        };
        val btnYes = alertView.findViewById<Button>(R.id.button_alert_dialog)
        btnYes.setOnClickListener{
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.example.firsttestknocker.notificationExemple")
            alertDialog.cancel()
        }
        return alertDialog
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